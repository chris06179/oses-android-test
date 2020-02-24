package de.stm.oses.verwendung;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.FileProvider;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import de.stm.oses.R;
import de.stm.oses.arbeitsauftrag.ArbeitsauftragBuilder;
import de.stm.oses.arbeitsauftrag.ArbeitsausftragIntentService;
import de.stm.oses.dialogs.DilocInfoDialogFragment;
import de.stm.oses.dialogs.IndexInfoDialog;
import de.stm.oses.dialogs.NoPdfReaderInstalledDialog;
import de.stm.oses.dialogs.ZeitraumDialogFragment;
import de.stm.oses.fax.FaxActivity;
import de.stm.oses.fcm.MessagingService;
import de.stm.oses.helper.FileDownload;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.SwipeRefreshListFragment;
import de.stm.oses.index.IndexJobIntentService;


public class VerwendungFragment extends SwipeRefreshListFragment implements ActionMode.Callback, DilocInfoDialogFragment.DilocInfoDialogListener {

    private static final int PERMISSION_REQUEST_STORAGE_DILOC = 5600;

    public String query;
    public int selectedyear;
    public int selectedmonth;
    public AsyncTask<String, Void, VerwendungAdapter> task;
    public ActionMode mActionMode;
    private boolean first = true;
    private OSESBase OSES;

    private FirebaseAnalytics mFirebaseAnalytics;

    public VerwendungFragment() {
        // Required empty public constructor
    }

    private static final int MOVE_DURATION = 150;

    private Handler DeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            final int lid = msg.arg2;
            int StatusCode = msg.arg1;

            Bundle extra = new Bundle();
            extra.putString("status", String.valueOf(StatusCode));
            mFirebaseAnalytics.logEvent("OSES_verwendung_delete", extra);

            if (StatusCode == 200) {

                int firstViewItemIndex = getListView().getFirstVisiblePosition();
                int lastViewItemIndex = getListView().getLastVisiblePosition();
                int viewIndex = lid - firstViewItemIndex;

                if (lid < firstViewItemIndex || lid > lastViewItemIndex) {
                    getListAdapter().remove(getListAdapter().getItem(lid));
                    getListView().setEnabled(true);
                    return;
                }

                final View v = getListView().getChildAt(viewIndex);
                v.setTag("DELETED");

                final int initialHeight = v.getMeasuredHeight();

                final Animation anim = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        if (interpolatedTime == 1) {
                            v.setVisibility(View.GONE);
                        } else {
                            v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                            v.requestLayout();
                        }
                    }

                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }
                };

                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }

                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        getListAdapter().remove(getListAdapter().getItem(lid));
                        getListView().setEnabled(true);
                        setRefreshing(false);
                    }
                });

                v.animate().setDuration(MOVE_DURATION).translationX(v.getWidth()).withEndAction(new Runnable() {
                    public void run() {
                        anim.setDuration(MOVE_DURATION);
                        v.startAnimation(anim);
                    }
                });


            }

            if (StatusCode == 400) {

                Toast.makeText(getActivity().getApplicationContext(), "Verwendung konnte nicht entfernt werden!", Toast.LENGTH_SHORT).show();
            }

            if (StatusCode == 999) {

                Toast.makeText(getActivity().getApplicationContext(), "Verwendung konnte nicht entfernt werden! Fehlerhafte Antwort vom Server!", Toast.LENGTH_SHORT).show();

            }

        }
    };

    // Called when the action mode is created; startActionMode() was called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        getActivity().getMenuInflater().inflate(R.menu.verwendung_action_menu, menu);
        mode.setTitle("Optionen");
        return true;
    }

    // Called each lastModified the action mode is shown. Always called after onCreateActionMode, but
    // may be called multiple times if the mode is invalidated.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        VerwendungClass item = getListAdapter().getSelectedItem();

        if (item == null) {
            mode.finish();
            return false;
        }

        String fpla = item.getFpla();

        if (fpla.equals("0"))
            fpla = "";
        else
            fpla = " Fpl/N/" + fpla;

        mode.setTitle(item.getBezeichner() + fpla);
        mode.setSubtitle(item.getDatumFormatted("EE, dd. MMMM yyyy"));

        if (item.isAllowSDL()) {
            menu.findItem(R.id.action_download_sdl).setVisible(true);
            menu.findItem(R.id.action_fax_sdl).setVisible(true);
        } else {
            menu.findItem(R.id.action_download_sdl).setVisible(false);
            menu.findItem(R.id.action_fax_sdl).setVisible(false);
        }

        if (item.getArbeitsauftragType() != ArbeitsauftragBuilder.TYPE_NONE && item.getArbeitsauftragType() != ArbeitsauftragBuilder.TYPE_EXTRACTING) {
            menu.findItem(R.id.action_show_arbeitsauftrag).setVisible(true);
        } else {
            menu.findItem(R.id.action_show_arbeitsauftrag).setVisible(false);
        }

        return true;
    }

    // Called when the user selects a contextual menu item
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        if (getListAdapter() == null)
            return false;

        VerwendungClass schicht = getListAdapter().getSelectedItem();

        if (schicht == null)
            return false;

        switch (item.getItemId()) {
            case R.id.action_delete_verwendung:
                DeleteVerwendung(getListAdapter().getSelection());
                return true;
            case R.id.action_download_sdl:
                DLSDL(String.valueOf(schicht.getId()));
                return true;
            case R.id.action_fax_sdl:
                FaxSDL(String.valueOf(schicht.getId()), schicht.getDatumFormatted("EE, dd. MMMM yyyy"));
                return true;
            case R.id.action_edit_verwendung:
                EditVerwendung(schicht);
                return true;
            case R.id.action_show_arbeitsauftrag:
                showArbeitsauftrag(schicht);
                return true;
            default:
                return false;
        }
    }


    @Override
    public VerwendungAdapter getListAdapter() {
        return ((VerwendungAdapter) super.getListAdapter());
    }

    // Called when the user exits the action mode
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        getListAdapter().setSelection(-1);
        mActionMode = null;
    }

    private void startFileIndexer() {
        if (IndexJobIntentService.enqueueWork(getActivity(), OSES)) {
            if (!OSES.getSession().getSessionIndexReminder()) {
                IndexInfoDialog dialog = IndexInfoDialog.newInstance();
                dialog.show(getChildFragmentManager(), "indexInfoDialog");
                OSES.getSession().setSessionIndexReminder(true);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OSES = new OSESBase(requireContext());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        setHasOptionsMenu(true);
        setRetainInstance(true);

        // Datum initialisieren
        Calendar c = Calendar.getInstance();
        selectedyear = c.get(Calendar.YEAR);
        selectedmonth = c.get(Calendar.MONTH) + 1;

        query = "https://oses.mobi/api.php?request=verwendung_show&json=true&session=" + OSES.getSession().getIdentifier();
        task = new GetVerwendung().execute(query);

        if (!OSES.getSession().getSessionDilocReminder() && OSES.getDilocStatus() == OSESBase.STATUS_INSTALLED && !OSES.hasStoragePermission()) {

            DilocInfoDialogFragment dilocDialog = DilocInfoDialogFragment.newInstance();
            dilocDialog.setTargetFragment(this, 0);
            dilocDialog.show(getFragmentManager(), "dilocInfoDialog");

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE_DILOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setRefreshing(true);
                    startFileIndexer();
                    task = new GetVerwendung().execute(query);
                    OSES.rebuildWorkingDirectory();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActionMode != null)
            mActionMode.finish();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Verwendung");

        if (mActionMode != null) {

            if (getListAdapter().getSelection() > -1) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);

            } else
                mActionMode.finish();
        }

        if (getListAdapter() != null && getListAdapter().isEmpty())
            setEmptyText("Keine Verwendung im angegebenen Zeitraum gefunden!");
        if (getListAdapter() == null)
            setEmptyText("Keine Verbindung zu OSES!");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        task.cancel(true);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyText("");

        getListView().setPadding(6, 8, 6, 8);
        getListView().setClipToPadding(false);
        getListView().setDivider(new ColorDrawable(Color.TRANSPARENT));
        getListView().setDividerHeight(6);
        getListView().setDrawSelectorOnTop(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getListView().setSelector(new ColorDrawable(Color.TRANSPARENT));
        }

        setColorScheme(R.color.oses_green, R.color.oses_green_dark, R.color.oses_green, R.color.oses_green_dark);

        setOnRefreshListener(() -> {
            startFileIndexer();
            task = new GetVerwendung().execute(query);

        });

        if (savedInstanceState == null) {

            String lastSync = OSES.getSession().getSessionLastVerwendung();

            if (lastSync != null) {

                ArrayList<VerwendungClass> list = null;

                try {
                    list = VerwendungClass.getNewListFromJSON(lastSync, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (list != null) {
                    setListAdapter(new VerwendungAdapter(getActivity(), list));

                    if (first && getListAdapter() != null) {
                        first = false;
                        getListView().setSelectionFromTop(getListAdapter().getTodayPos(), 200);
                    }

                    setListShown(true);
                    setRefreshing(true);
                }

            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100 || requestCode == 101) && resultCode == 200) {
            if (mActionMode != null)
                mActionMode.finish();
            setRefreshing(true);
            task = new GetVerwendung().execute(query);
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        getListAdapter().setSelection(position);

        if (mActionMode == null)
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        else
            mActionMode.invalidate();

        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.verwendung_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_verwendung:
                VerwendungAddOnClick();
                return true;
            case R.id.action_set_date:
                ChangeDateOnClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void VerwendungAddOnClick() {

        Intent intent = new Intent(getActivity(), VerwendungAddActivity.class);
        startActivityForResult(intent, 100);


    }

    public void EditVerwendung(VerwendungClass item) {

        Intent intent = new Intent(getActivity(), VerwendungAddActivity.class);
        intent.putExtra("item", item);
        startActivityForResult(intent, 100);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessagingService.RefreshVerwendungEvent event) {
        setRefreshing(true);
        task = new GetVerwendung().execute(query);
    }


    public void DeleteVerwendung(final long sid) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final VerwendungClass item = getListAdapter().getItem((int) sid);

        if (item == null)
            return;

        String fpla = item.getFpla();

        if (fpla.equals("0"))
            fpla = "";
        else
            fpla = " Fpl/N/" + fpla;

        builder.setMessage("Verwendung " + item.getBezeichner() + fpla + " (" + item.getDatumFormatted("EEEE, dd. MMMM yyyy") + ") wirklich entfernen?")
                .setCancelable(false)
                .setTitle("Verwendung entfernen")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                        setRefreshing(true);
                        getListView().setEnabled(false);
                        new Thread(new DeleteVerwendungRun(item.getId(), (int) sid)).start();
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }

    public void ChangeDateOnClick() {

        ZeitraumDialogFragment zeitraum = ZeitraumDialogFragment.newInstance(new ZeitraumDialogFragment.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear,
                                  int dayOfMonth) {
                if (selectedyear == year && selectedmonth == monthOfYear + 1)
                    return;

                first = true;
                selectedyear = year;
                selectedmonth = monthOfYear + 1;
                setListShown(false);
                query = "https://oses.mobi/api.php?request=verwendung_show&json=true&monat=" + selectedmonth + "&jahr=" + selectedyear + "&session=" + OSES.getSession().getIdentifier();
                task = new GetVerwendung().execute(query);
            }

        }, selectedyear, selectedmonth - 1, 1);

        zeitraum.show(getActivity().getSupportFragmentManager(), "zeitraumdialog");
    }

    public void showCurrentMonth() {

        Calendar c = Calendar.getInstance();
        selectedyear = c.get(Calendar.YEAR);
        selectedmonth = c.get(Calendar.MONTH) + 1;

        first = true;
        setListShown(false);
        query = "https://oses.mobi/api.php?request=verwendung_show&json=true&monat=" + selectedmonth + "&jahr=" + selectedyear + "&session=" + OSES.getSession().getIdentifier();
        task = new GetVerwendung().execute(query);

    }

    private void showArbeitsauftrag(VerwendungClass schicht) {

        Bundle details = new Bundle();
        details.putString("bezeichner", schicht.getBezeichner());
        details.putString("datum", schicht.getDatumFormatted("dd.MM.yyyy"));
        details.putString("est", schicht.getEst());
        details.putInt("type", schicht.getArbeitsauftragType());
        mFirebaseAnalytics.logEvent("OSES_show_arbeitsauftrag", details);

        if (schicht.getArbeitsauftragType() == ArbeitsauftragBuilder.TYPE_CACHED) {
            File cache = schicht.getArbeitsauftragCacheFile();
            showPdfFile(cache);

        } else if (schicht.getArbeitsauftragType() == ArbeitsauftragBuilder.TYPE_ONLINE) {
            downloadArbeitsauftrag(schicht);
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ArbeitsausftragIntentService.ArbeitsauftragResultEvent event) {

        File result = event.result;

        if (result != null) {
            getListAdapter().getItemByID(event.schicht.getId()).setArbeitsauftragCacheFile(result);
        } else {
            getListAdapter().getItemByID(event.schicht.getId()).setArbeitsauftragType(event.schicht.getArbeitsauftragType());
        }

        getListAdapter().notifyDataSetChanged();

        if (mActionMode != null) {
            mActionMode.invalidate();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(IndexJobIntentService.IndexFinishedEvent event) {
        ArbeitsausftragIntentService.startService(requireActivity(),OSES, getListAdapter().getArrayList());
    }

    private void showPdfFile(File file) {

        Uri fileUri = FileProvider.getUriForFile(requireContext(), "de.stm.oses.FileProvider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // Falls keine entsprechende Activity existiert (kein PDF-Reader installiert)
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            new NoPdfReaderInstalledDialog().show(getChildFragmentManager(), "no_pdf_dialog");
        }

    }

    @Override
    public void onDilocInfoDialogRequestPermission() {

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE_DILOC);

    }

    @Override
    public void onDilocInfoDialogDecline() {
        OSES.getSession().setSessionDilocReminder(true);
    }

    public class GetVerwendung extends AsyncTask<String, Void, VerwendungAdapter> {


        protected VerwendungAdapter doInBackground(String... params) {

            String response = OSES.getJSON(params[0], 60000);

            try {
                if (getActivity() != null && response != null) {
                    ArrayList<VerwendungClass> list = VerwendungClass.getNewListFromJSON(response, getActivity());

                    ArbeitsausftragIntentService.startService(requireActivity(), OSES, list);

                    Calendar c = Calendar.getInstance();
                    if (selectedyear == c.get(Calendar.YEAR) && selectedmonth == c.get(Calendar.MONTH) + 1)
                        OSES.getSession().setSessionLastVerwendung(response);

                    return new VerwendungAdapter(getActivity(), list);
                } else
                    return null;

            } catch (JSONException e) {
                return null;
            }

        }

        protected void onPostExecute(VerwendungAdapter adapter) {

            try {
                if (getActivity() == null || getListView() == null)
                    return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (adapter != null && adapter.isEmpty())
                setEmptyText("Keine Verwendung im angegebenen Zeitraum gefunden!");

            if (getListAdapter() == null && adapter == null)
                setEmptyText("Keine Verbindung zu OSES!");

            if (getListAdapter() != null && adapter == null) {
                Toast.makeText(getActivity(), "Keine Verbindung zu OSES, angezeigte Daten ggf. nicht aktuell!", Toast.LENGTH_LONG).show();
                setRefreshing(false);
                return;
            }

            int index = getListView().getFirstVisiblePosition();
            View v = getListView().getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();

            setListAdapter(adapter);

            if (index != 0)
                getListView().setSelectionFromTop(index, top - getListView().getDividerHeight() - 2);


            setRefreshing(false);
            setListShown(true);

            if (mActionMode != null) {
                mActionMode.finish();
            }

            if (first && getListAdapter() != null) {
                first = false;
                getListView().setSelectionFromTop(getListAdapter().getTodayPos(), 200);

            }
        }

    }

    public class DeleteVerwendungRun implements Runnable {
        private int vid;
        private int lid;

        DeleteVerwendungRun(int vid, int lid) {
            this.lid = lid;
            this.vid = vid;
        }

        public void run() {

            String response = OSES.getJSON("https://oses.mobi/api.php?request=verwendung_show&action=delete_json&id=" + vid + "&session=" + OSES.getSession().getIdentifier(), 60000);

            String StatusCode;

            JSONObject json;

            try {

                json = new JSONObject(response);

                StatusCode = json.getString("StatusCode");


            } catch (Exception e) {
                StatusCode = "999";
            }

            Message result = new Message();

            result.arg1 = Integer.parseInt(StatusCode);
            result.arg2 = lid;

            DeleteHandler.sendMessage(result);

        }
    }

    public void FaxSDL(String sid, String datum) {

        Intent intent = new Intent(getActivity(), FaxActivity.class);
        intent.putExtra("type", "SDL");
        intent.putExtra("id", sid);
        intent.putExtra("date", datum);
        startActivity(intent);

    }

    public void downloadArbeitsauftrag(final VerwendungClass verwendung) {

        String url = "https://oses.mobi/api.php?request=arbeitsauftrag&command=download&id=" + verwendung.getId() + "&session=" + OSES.getSession().getIdentifier();
        FileDownload download = new FileDownload(getActivity());
        download.setTitle("Arbeitsauftrag");
        download.setMessage("Das Dokument wird heruntergeladen, dieser Vorgang kann einen Moment dauern...");
        download.setURL(url);
        download.setLocalDirectory("Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") + "/");
        download.setLocalFilename("Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("[^A-Za-z0-9]", "_") + ".pdf");
        download.setOnDownloadFinishedListener(new FileDownload.OnDownloadFinishedListener() {
            @Override
            public void onDownloadFinished(File file) {

                verwendung.setArbeitsauftragCacheFile(file);
                getListAdapter().notifyDataSetChanged();

                showPdfFile(file);
            }

            @Override
            public void onTextReceived(String res) {

                int iStatus;
                String sStatusBody;

                try {

                    JSONObject json = new JSONObject(res);

                    iStatus = json.getInt("Status");
                    sStatusBody = json.getString("StatusBody");

                    switch (iStatus) {
                        case 400:
                            Toast.makeText(getActivity(), sStatusBody, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            onUnknownStatus(iStatus);

                    }

                } catch (Exception e) {
                    onException(e);
                }
            }

            @Override
            public void onException(Exception e) {
                Toast.makeText(getActivity(), "Anwendungsfehler: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onUnknownStatus(int status) {
                Toast.makeText(getActivity(), "Anwendungsfehler: Der Server hat mit einem unbekannten Statuscode geantwortet! (" + status + ")", Toast.LENGTH_LONG).show();
            }
        });
        download.execute();
    }

    public void DLSDL(String sid) {

        String url = "https://oses.mobi/api.php?request=download&session=" + OSES.getSession().getIdentifier() + "&id=" + sid + "&command=SDL";
        FileDownload download = new FileDownload(getActivity());
        download.setTitle("Sonderleistung");
        download.setMessage("Das Dokument wird heruntergeladen, dieser Vorgang kann einen Moment dauern...");
        download.setURL(url);
        download.setLocalDirectory("Dokumente/Nebengeld/");
        download.setOnDownloadFinishedListener(new FileDownload.OnDownloadFinishedListener() {
            @Override
            public void onDownloadFinished(File file) {
                showPdfFile(file);
            }

            @Override
            public void onTextReceived(String res) {

                int iStatus;
                String sStatusBody;

                try {

                    JSONObject json = new JSONObject(res);

                    iStatus = json.getInt("Status");
                    sStatusBody = json.getString("StatusBody");

                    switch (iStatus) {
                        case 400:
                            Toast.makeText(getActivity(), sStatusBody, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            onUnknownStatus(iStatus);

                    }

                } catch (Exception e) {
                    onException(e);
                }
            }

            @Override
            public void onException(Exception e) {
                Toast.makeText(getActivity(), "Anwendungsfehler: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onUnknownStatus(int status) {
                Toast.makeText(getActivity(), "Anwendungsfehler: Der Server hat mit einem unbekannten Statuscode geantwortet! (" + status + ")", Toast.LENGTH_LONG).show();
            }
        });
        download.execute();

    }
} 