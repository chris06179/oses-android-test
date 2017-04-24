package de.stm.oses.verwendung;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.stm.oses.R;
import de.stm.oses.arbeitsauftrag.ArbeitsauftragDilocIntentService;
import de.stm.oses.dialogs.DilocInfoDialogFragment;
import de.stm.oses.fax.FaxActivity;
import de.stm.oses.arbeitsauftrag.ArbeitsauftragBuilder;
import de.stm.oses.fcm.ListenerService;
import de.stm.oses.helper.FileDownload;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.dialogs.ProgressDialogFragment;
import de.stm.oses.helper.OSESRequest;
import de.stm.oses.helper.SwipeRefreshListFragment;
import de.stm.oses.dialogs.ZeitraumDialogFragment;

import static android.content.Context.ACTIVITY_SERVICE;


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

    // Called each time the action mode is shown. Always called after onCreateActionMode, but
    // may be called multiple times if the mode is invalidated.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // Return false if nothing is done
    }

    // Called when the user selects a contextual menu item
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        VerwendungClass schicht = getListAdapter().getSelectedItem();

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
                CheckArbeitsauftrag(schicht);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OSES = new OSESBase(getActivity());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        setHasOptionsMenu(true);
        setRetainInstance(true);

        // Datum initialisieren
        Calendar c = Calendar.getInstance();
        selectedyear = c.get(Calendar.YEAR);
        selectedmonth = c.get(Calendar.MONTH) + 1;

        query = "https://oses.mobi/api.php?request=verwendung_show&json=true&session=" + OSES.getSession().getIdentifier();

        if (!OSES.getSession().getSessionDilocReminder() && OSES.isPackageInstalled("de.diloc.DiLocSyncMobile", getActivity().getPackageManager()) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

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
                    task = new GetVerwendung().execute(query);
                    OSES.rebuildWorkingDirectory();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        if (!isIntentServiceRunning("de.stm.oses.arbeitsauftrag.ArbeitsauftragDilocIntentService")) {
            if (((AppCompatActivity) getActivity()).getSupportFragmentManager().getFragments() != null)
                for (Fragment f : ((AppCompatActivity) getActivity()).getSupportFragmentManager().getFragments()) {
                    if (f != null && f.getTag() != null && f.getTag().contains("arbeitsauftragWaitDialog"))
                        ((ProgressDialogFragment) f).getDialog().dismiss();
                }
        }
    }

    private boolean isIntentServiceRunning(String serviceName) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
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

                VerwendungClass item = getListAdapter().getSelectedItem();

                String fpla = item.getFpla();

                if (fpla.equals("0"))
                    fpla = "";
                else
                    fpla = " Fpl/N/" + fpla;

                mActionMode.setTitle(item.getBezeichner() + fpla);
                mActionMode.setSubtitle(item.getDatumFormatted("EE, dd. MMMM yyyy"));
                Menu menu = mActionMode.getMenu();

                if (getListAdapter().getSelectedItem().isAllowSDL()) {
                    menu.findItem(R.id.action_download_sdl).setVisible(true);
                    menu.findItem(R.id.action_fax_sdl).setVisible(true);
                } else {
                    menu.findItem(R.id.action_download_sdl).setVisible(false);
                    menu.findItem(R.id.action_fax_sdl).setVisible(false);
                }

                if (getListAdapter().getSelectedItem().getArbeitsauftragType() != ArbeitsauftragBuilder.TYPE_NONE) {
                    menu.findItem(R.id.action_show_arbeitsauftrag).setVisible(true);
                } else {
                    menu.findItem(R.id.action_show_arbeitsauftrag).setVisible(false);
                }

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

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                task = new GetVerwendung().execute(query);

            }
        });

        if (savedInstanceState == null) {

            task = new GetVerwendung().execute(query);

            String lastSync = OSES.getSession().getSessionLastVerwendung();

            if (lastSync != null) {

                ArrayList<VerwendungClass> list = null;

                try {
                    list = VerwendungClass.getNewList(lastSync, getActivity());
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

        VerwendungClass item = getListAdapter().getItem((int) id);

        if (item == null)
            return;

        String fpla = item.getFpla();

        if (fpla.equals("0"))
            fpla = "";
        else
            fpla = " Fpl/N/" + fpla;

        mActionMode.setTitle(item.getBezeichner() + fpla);
        mActionMode.setSubtitle(item.getDatumFormatted("EE, dd. MMMM yyyy"));
        Menu menu = mActionMode.getMenu();

        if (item.isAllowSDL()) {
            menu.findItem(R.id.action_download_sdl).setVisible(true);
            menu.findItem(R.id.action_fax_sdl).setVisible(true);
        } else {
            menu.findItem(R.id.action_download_sdl).setVisible(false);
            menu.findItem(R.id.action_fax_sdl).setVisible(false);
        }

        if (item.getArbeitsauftragType() != ArbeitsauftragBuilder.TYPE_NONE) {
            menu.findItem(R.id.action_show_arbeitsauftrag).setVisible(true);
        } else {
            menu.findItem(R.id.action_show_arbeitsauftrag).setVisible(false);
        }

        Vibrator vibrate;
        vibrate = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrate.vibrate(15);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.verwendung_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

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
    public void onMessageEvent(ListenerService.RefreshVerwendungEvent event) {
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

        zeitraum.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "zeitraumdialog");
    }

    private void CheckArbeitsauftrag(VerwendungClass schicht) {

        mFirebaseAnalytics.logEvent("OSES_show_arbeitsauftrag", null);

        if (schicht.getArbeitsauftragType() == ArbeitsauftragBuilder.TYPE_ONLINE) {
            downloadArbeitsauftrag(schicht);
            return;
        }

        File cache = schicht.getArbeitsauftragCacheFile();
        File diloc = schicht.getArbeitsauftragDilocFile();

        if (cache != null && diloc != null) {
            if (diloc.lastModified() > cache.lastModified()) {
                GenerateArbeitsauftrag(schicht);
                return;
            } else {
                ShowPDFFile(cache);
                return;
            }
        }

        if (cache != null) {
            ShowPDFFile(cache);
            return;
        } else {
            GenerateArbeitsauftrag(schicht);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ArbeitsauftragDilocIntentService.ArbeitsauftragProgressEvent event) {
        ProgressDialogFragment dialogFragment = ((ProgressDialogFragment) ((AppCompatActivity) getActivity()).getSupportFragmentManager().findFragmentByTag("arbeitsauftragWaitDialog" + event.id));
        if (dialogFragment != null)
            if (dialogFragment.getDialog() != null) {
                ProgressDialog dialog = dialogFragment.getDialog();

                if (event.progress == event.max) {
                    dialog.setProgress(event.max);
                    dialogFragment.setMessage("PDF-Datei wird erstellt...");
                } else {
                    dialogFragment.setProgressNumberFormat("Seite %1d / %2d");
                    dialog.setIndeterminate(false);
                    dialog.setMax(event.max);
                    dialog.setProgress(event.progress);
                }
            }
    }

    public String getStringFile(File f) {
        InputStream inputStream = null;
        String encodedFile= "", lastVal;
        try {
            inputStream = new FileInputStream(f.getAbsolutePath());

            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
            encodedFile =  output.toString();
        }
        catch (FileNotFoundException e1 ) {
            e1.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        lastVal = encodedFile;
        return lastVal;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ArbeitsauftragDilocIntentService.ArbeitsauftragResultEvent event) {

        Object result = event.result;
        VerwendungClass schicht = event.schicht;

        Fragment f = ((AppCompatActivity) getActivity()).getSupportFragmentManager().findFragmentByTag("arbeitsauftragWaitDialog" + schicht.getId());

        if (result instanceof File) {

            getListAdapter().getItemByID(event.schicht.getId()).setArbeitsauftragCacheFile((File) result);
            getListAdapter().notifyDataSetChanged();

            if (f != null)
                ShowPDFFile((File) result);


            if (OSES.getSession().getGroup() == 1) {
                String pdf64 = getStringFile(((File) result));
                if (pdf64.isEmpty())
                    return;

                Map<String, String> map = new HashMap<>();
                map.put("pdf", pdf64);

                OSESRequest upload = new OSESRequest(getActivity());
                upload.setParams(map);
                upload.setUrl("https://oses.mobi/api.php?request=arbeitsauftrag&command=upload&id=" + schicht.getId() + "&session=" + OSES.getSession().getIdentifier());
                upload.setOnRequestFinishedListener(new OSESRequest.OnRequestFinishedListener() {
                    @Override
                    public void onRequestFinished(String response) {
                        Log.d("AA_UP", "OK: " + response);
                    }

                    @Override
                    public void onRequestException(Exception e) {
                        Log.d("AA_UP", "ERROR: " + e.getMessage());
                    }

                    @Override
                    public void onRequestUnknown(int status) {
                        Log.d("AA_UP", "UNKNOWN: " + status);
                    }

                    @Override
                    public void onIsNotConnected() {
                        Log.d("AA_UP", "NOT CONNECTED");
                    }
                });

                upload.execute();
            }
        }

        if (f != null)
            ((ProgressDialogFragment) f).dismiss();

        if (result == null)
            return;

        if (result instanceof Exception) {
            Toast.makeText(getActivity(), "Es konnte kein Arbeitsauftrag zur angegebenen Schichtnummer extrahiert werden! Bitte rufe das Dokument ggf. direkt über Diloc|Sync auf!", Toast.LENGTH_SHORT).show();
            FirebaseCrash.log("AA = " + schicht.getBezeichner() + ", " + schicht.getEst() + ", " + schicht.getDatumFormatted("dd.MM.yyyy"));
            FirebaseCrash.report(new Exception("Arbeitsauftrag, keine Extraktion möglich!"));
            return;
        }

    }

    public void GenerateArbeitsauftrag(VerwendungClass schicht) {

        ProgressDialogFragment loginWaitDialog;
        if (schicht.getArbeitsauftragCacheFile() == null)
            loginWaitDialog = ProgressDialogFragment.newInstance("Dokument wird erzeugt...", "Bitte warten, der angeforderte Arbeitsauftrag wird extrahiert. Die Quelldatei aus Diloc|Sync wird durchsucht...", ProgressDialog.STYLE_HORIZONTAL);
        else
            loginWaitDialog = ProgressDialogFragment.newInstance("Dokument wird erzeugt...", "Bitte warten, der angeforderte Arbeitauftrag wurde aktualisiert und wird neu extrahiert. Die Quelldatei aus Diloc|Sync wird durchsucht...", ProgressDialog.STYLE_HORIZONTAL);
        loginWaitDialog.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "arbeitsauftragWaitDialog" + schicht.getId());


        Intent serviceIntent = new Intent(getActivity(), ArbeitsauftragDilocIntentService.class);
        serviceIntent.putExtra("item", schicht);
        getActivity().startService(serviceIntent);

    }

    public void ShowPDFFile(File file) {

        Uri fileUri = FileProvider.getUriForFile(getActivity(), "de.stm.oses.FileProvider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivity(intent);

    }

    @Override
    public void onDilocInfoDialogRequestPermission() {

        FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_STORAGE_DILOC);

    }

    @Override
    public void onDilocInfoDialogDecline() {
        OSES.getSession().setSessionDilocReminder(true);
    }

    private class GetVerwendung extends AsyncTask<String, Void, VerwendungAdapter> {


        protected VerwendungAdapter doInBackground(String... params) {

            String response = OSES.getJSON(params[0], 60000);

            try {
                if (getActivity() != null && response != null) {
                    ArrayList<VerwendungClass> list = VerwendungClass.getNewList(response, getActivity());

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
                Toast.makeText(getActivity(), "Keine Verbindung zu OSES, angezeigte Daten ggf. nicht aktuell!", Toast.LENGTH_SHORT).show();
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
        download.setLocalDirectory("Dokumente/Arbeitsaufträge/" + verwendung.getDatumFormatted("yyyy/MM - MMMM") +"/");
        download.setLocalFilename("Arbeitsauftrag_" + verwendung.getDatumFormatted("dd.MM.yyyy_EE").replaceAll(".$", "") + "_" + verwendung.getBezeichner().replaceAll("\\s", "_") + ".pdf");
        download.setOnDownloadFinishedListener(new FileDownload.OnDownloadFinishedListener() {
            @Override
            public void onDownloadFinished(File file) {

                verwendung.setArbeitsauftragCacheFile(file);
                getListAdapter().notifyDataSetChanged();

                Uri fileUri = FileProvider.getUriForFile(getActivity(), "de.stm.oses.FileProvider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(intent);
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
                Toast.makeText(getActivity(), "Anwendungsfehler: Der Server hat mit einem unbekannten Statuscode geantwortet! (" + String.valueOf(status) + ")", Toast.LENGTH_LONG).show();
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
        download.setLocalDirectory("Dokumente/Nebengeld");
        download.setOnDownloadFinishedListener(new FileDownload.OnDownloadFinishedListener() {
            @Override
            public void onDownloadFinished(File file) {

                Uri fileUri = FileProvider.getUriForFile(getActivity(), "de.stm.oses.FileProvider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(intent);
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
                Toast.makeText(getActivity(), "Anwendungsfehler: Der Server hat mit einem unbekannten Statuscode geantwortet! (" + String.valueOf(status) + ")", Toast.LENGTH_LONG).show();
            }
        });
        download.execute();

    }
} 