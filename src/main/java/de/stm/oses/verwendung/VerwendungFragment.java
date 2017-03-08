package de.stm.oses.verwendung;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import de.stm.oses.R;
import de.stm.oses.fax.FaxActivity;
import de.stm.oses.helper.ArbeitsauftragBuilder;
import de.stm.oses.helper.FileDownload;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.ProgressDialogFragment;
import de.stm.oses.helper.SwipeRefreshListFragment;
import de.stm.oses.helper.ZeitraumDialogFragment;


public class VerwendungFragment extends SwipeRefreshListFragment implements ActionMode.Callback {

    public String query;
    public int selectedyear;
    public int selectedmonth;
    public AsyncTask<String, Void, VerwendungAdapter> task;
    public ActionMode mActionMode;
    private boolean first = true;
    private OSESBase OSES;

    private FirebaseAnalytics mFirebaseAnalytics;

    private BroadcastReceiver receiver;

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

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getListAdapter().remove(getListAdapter().getItem(lid));
                    getListView().setEnabled(true);
                    setRefreshing(false);
                } else
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
                new ShowArbeitsauftrag().execute(schicht);
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
        task = new GetVerwendung().execute(query);

    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter("de.stm.oses.OSES_REFRESH_VERWENDUNG");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setRefreshing(true);
                task = new GetVerwendung().execute(query);
            }
        };

        getActivity().registerReceiver(receiver, filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
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

                if (getListAdapter().getSelectedItem().isArbeitsauftragAvailable(getActivity()) != ArbeitsauftragBuilder.TYPE_NONE) {
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

        if (item.isArbeitsauftragAvailable(getActivity()) != ArbeitsauftragBuilder.TYPE_NONE) {
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




    private ArrayList<VerwendungClass> parseJSON(String json) {
        ArrayList<VerwendungClass> items = new ArrayList<>();

        try {

            JSONObject verwendung = new JSONObject(json);

            JSONObject monat = verwendung.getJSONObject(verwendung.names().getString(0));

            JSONArray schichten = monat.getJSONArray("data");

            for (int i = 0; i < schichten.length(); i++) {

                VerwendungClass item = new VerwendungClass();

                JSONObject schicht = schichten.getJSONObject(i);

                item.setId(schicht.getInt("id"));

                item.setKat(schicht.getString("kat"));

                item.setBezeichner(schicht.getString("bezeichner"));
                item.setFpla(schicht.getString("fpla"));

                item.setDb(schicht.getString("db"));
                item.setDe(schicht.getString("de"));
                item.setAdb(schicht.getString("adb"));
                item.setAde(schicht.getString("ade"));

                item.setAdban(schicht.optDouble("adb_an", 1));
                item.setAdean(schicht.optDouble("ade_an", 1));

                item.setPause(schicht.getString("pause"));
                item.setPauseInt(schicht.getInt("pauseint"));
                item.setPauseRil(schicht.getString("pause_ril"));
                item.setApause(schicht.getString("apause"));

                item.setDatum(schicht.getInt("datum"));
                item.setBaureihen(schicht.getString("baureihen"));

                item.setAz(schicht.getString("az"));
                item.setOaz(schicht.optInt("o_az", 0));

                item.setAufart(schicht.getString("auf_art"));
                item.setAufdb(schicht.getString("aufdb"));
                item.setAufde(schicht.getString("aufde"));
                item.setAufdz(schicht.optInt("aufdz", 0));

                item.setMdifferenz(schicht.getString("mdifferenz"));

                item.setEst(schicht.getString("est"));
                item.setEstId(schicht.optInt("estid", 0));

                item.setMsoll(schicht.getString("msoll"));

                item.setMist(schicht.getString("mist"));

                item.setFunktion(schicht.getString("funktion"));
                item.setFunktionId(schicht.getInt("funktionid"));

                item.setDbr(schicht.getString("dbr"));
                item.setDer(schicht.getString("der"));
                item.setApauser(schicht.getString("apauser"));


                if (schicht.isNull("notiz"))
                    item.setNotiz(null);
                else
                    item.setNotiz(schicht.optString("notiz", null));

                item.setInfo(schicht.optString("info", ""));

                if (schicht.has("AllowSDL"))
                    item.setAllowSDL(schicht.getBoolean("AllowSDL"));

                if (schicht.has("ShowTimeError"))
                    item.setShowTimeError(schicht.getBoolean("ShowTimeError"));

                items.add(item);

            }

            // Zusammenfassung

            SharedPreferences AppSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String position = AppSettings.getString("sumPosition", "BOTTOM");

            if (!position.equals("NONE")) {

                VerwendungClass sumData = new VerwendungClass(true);

                sumData.setLabel(verwendung.names().getString(0));
                if (monat.has("schichten"))
                    sumData.setSchichten(monat.getInt("schichten"));
                if (monat.has("urlaub"))
                    sumData.setUrlaub(monat.getInt("urlaub"));
                sumData.setMsoll(monat.getString("msoll"));
                sumData.setAzg(monat.getString("azg"));
                sumData.setMdifferenz(monat.getString("mdifferenz"));

                if (position.equals("BOTTOM") || position.equals("BOTH"))
                    items.add(sumData);

                if (position.equals("TOP") || position.equals("BOTH"))
                    items.add(0, sumData);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return items;
    }

    private class ShowArbeitsauftrag extends AsyncTask<VerwendungClass, Void, File> {

        VerwendungClass schicht;

        protected void onPreExecute() {

            mFirebaseAnalytics.logEvent("OSES_show_arbeitsauftrag", null);
            ProgressDialogFragment loginWaitDialog = ProgressDialogFragment.newInstance("Dokument wird erzeugt...", "Bitte warten, der angeforderte Arbeitsauftrag wird extrahiert...");
            loginWaitDialog.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "auftragWaitDialog");

        }

        protected File doInBackground(VerwendungClass... params) {

            schicht = params[0];
            ArbeitsauftragBuilder auftrag = new ArbeitsauftragBuilder(params[0], getActivity());

            File cache = auftrag.getExtractedCacheFile();
            File diloc = auftrag.getDilocSourceFile();

            if (cache != null && diloc != null) {
                if (diloc.lastModified() > cache.lastModified())
                    return auftrag.extractFromDilocSourceFile();
                else
                    return cache;
            }

            if (cache != null)
                return cache;
            else
                return auftrag.extractFromDilocSourceFile();

        }

        protected void onPostExecute(File auftrag) {

            Fragment f = ((AppCompatActivity) getActivity()).getSupportFragmentManager().findFragmentByTag("auftragWaitDialog");
            if (f != null)
                ((ProgressDialogFragment) f).dismiss();


            if (auftrag == null) {
                Toast.makeText(getActivity(), "Es konnte kein Arbeitsauftrag zur angegebenen Schichtnummer extrahiert werden! Bitte rufe das Dokument ggf. direkt über Diloc|Sync auf!", Toast.LENGTH_SHORT).show();
                FirebaseCrash.log("AA = "+schicht.getBezeichner()+", "+schicht.getEst()+", "+schicht.getDatumFormatted("dd.MM.yyyy"));
                FirebaseCrash.report(new Exception("Arbeitsauftrag, keine Extraktion möglich!"));
                return;
            }

            Uri fileUri = FileProvider.getUriForFile(getActivity(), "de.stm.oses.FileProvider", auftrag);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION+Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(intent);

        }

    }


    private class GetVerwendung extends AsyncTask<String, Void, VerwendungAdapter> {


        protected VerwendungAdapter doInBackground(String... params) {

            String response = OSES.getJSON(params[0], 60000);

            if (getActivity() != null && response != null)
                return new VerwendungAdapter(getActivity(), parseJSON(response));
            else
                return null;

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
            if (adapter == null)
                setEmptyText("Keine Verbindung zu OSES!");

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

    public void DLSDL(String sid) {

        String url = "https://oses.mobi/api.php?request=download&session=" + OSES.getSession().getIdentifier() + "&id=" + sid + "&command=SDL";
        FileDownload download = new FileDownload(getActivity());
        download.setTitle("Sonderleistung");
        download.setMessage("Das Dokument wird heruntergeladen, dieser Vorgang kann einen Moment dauern...");
        download.setURL(url);
        download.setLocalDirectory("docs/Nebengeld");
        download.setOnDownloadFinishedListener(new FileDownload.OnDownloadFinishedListener() {
            @Override
            public void onDownloadFinished(File file) {

                Uri fileUri = FileProvider.getUriForFile(getActivity(), "de.stm.oses.FileProvider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION+Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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