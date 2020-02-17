package de.stm.oses.verwendung;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

import de.stm.oses.R;
import de.stm.oses.dialogs.ZeitraumDialogFragment;
import de.stm.oses.fax.FaxActivity;
import de.stm.oses.helper.FileDownload;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.OSESRequest;
import de.stm.oses.helper.RecyclerFragment;

public class VerwendungFragmentNew extends RecyclerFragment implements ActionMode.Callback {

    public String query;
    public int selectedyear;
    public int selectedmonth;
    public ActionMode mActionMode;
    private OSESBase OSES;
    private OSESRequest verwendungRequest;

    private BroadcastReceiver receiver;

    public VerwendungFragmentNew() {
        // Required empty public constructor
    }


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
            default:
                return false;
        }
    }

    @Override
    public VerwendungAdapterNew getListAdapter() {
        return ((VerwendungAdapterNew) super.getListAdapter());
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
        setHasOptionsMenu(true);
        setRetainInstance(true);

        // Datum initialisieren
        Calendar c = Calendar.getInstance();
        selectedyear = c.get(Calendar.YEAR);
        selectedmonth = c.get(Calendar.MONTH) + 1;

    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter("de.stm.oses.OSES_REFRESH_VERWENDUNG");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setRefreshing(true);
                getVerwendung(query);
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
            } else
                mActionMode.finish();
        }

    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = space;
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setColorScheme(R.color.oses_green, R.color.oses_green_dark, R.color.oses_green, R.color.oses_green_dark);

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getVerwendung(query);

            }
        });

        if (getListView().getAdapter() == null)
            getListView().setAdapter(new VerwendungAdapterNew(getActivity()));

        getListView().addItemDecoration(new SpacesItemDecoration(5));

        query = "https://oses.mobi/api.php?request=verwendung_show&json=true&session=" + OSES.getSession().getIdentifier();
        getVerwendung(query);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100 || requestCode == 101) && resultCode == 200) {
            if (mActionMode != null)
                mActionMode.finish();
            setRefreshing(true);
            getVerwendung(query);
        }
    }


    //@Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        getListAdapter().setSelection(position);

        if (mActionMode == null)
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);

        VerwendungClass item = getListAdapter().getItem((int) id);

        String fpla = item.getFpla();

        if (fpla.equals("0"))
            fpla = "";
        else
            fpla = " Fpl/N/" + fpla;

        mActionMode.setTitle(item.getBezeichner() + fpla);
        mActionMode.setSubtitle(item.getDatumFormatted("EE, dd. MMMM yyyy"));
        Menu menu = mActionMode.getMenu();

        if (getListAdapter().getItem((int) id).isAllowSDL()) {
            menu.findItem(R.id.action_download_sdl).setVisible(true);
            menu.findItem(R.id.action_fax_sdl).setVisible(true);
        } else {
            menu.findItem(R.id.action_download_sdl).setVisible(false);
            menu.findItem(R.id.action_fax_sdl).setVisible(false);
        }

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

               selectedyear = year;
               selectedmonth = monthOfYear + 1;
               query = "https://oses.mobi/api.php?request=verwendung_show&json=true&monat=" + selectedmonth + "&jahr=" + selectedyear + "&session=" + OSES.getSession().getIdentifier();
               getVerwendung(query);
           }

       }, selectedyear, selectedmonth - 1, 1);

                zeitraum.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "zeitraumdialog");
    }




    private void parseJSON(String json) {

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

                //item.setAufart(schicht.getString("auf_art"));
                //item.setAufdb(schicht.getString("aufdb"));
                //item.setAufde(schicht.getString("aufde"));
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

                getListAdapter().getItems().add(item);


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
                    getListAdapter().getItems().add(sumData);

                if (position.equals("TOP") || position.equals("BOTH"))
                    getListAdapter().getItems().add(sumData);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void getVerwendung(final String request) {

        if (verwendungRequest != null)
            verwendungRequest.cancel(true);

        //setWaitText("Faxgeräte werden abgerufen...");
        //showFaxList(false);
        getListAdapter().clear();

        verwendungRequest = new OSESRequest(getActivity());
        verwendungRequest.setUrl(request);

        verwendungRequest.setOnRequestFinishedListener(new OSESRequest.OnRequestFinishedListener() {
            @Override
            public void onRequestFinished(String response) {
                parseJSON(response);

                setRefreshing(false);

                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }

            @Override
            public void onRequestException(Exception e) {

                //showError(e.getLocalizedMessage(), R.drawable.ic_action_fail);

            }

            @Override
            public void onRequestUnknown(int status) {

            }

            @Override
            public void onIsNotConnected() {

                //showError("Keine Internetverbindung", R.drawable.ic_action_cloud_off);

            }
        });

        verwendungRequest.execute();
    }


    public class DeleteVerwendungRun implements Runnable {
        private int vid;
        private int lid;

        public DeleteVerwendungRun(int vid, int lid) {
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
        download.setLocalDirectory("Dokumente/");
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
                Toast.makeText(getActivity(), "Anwendungsfehler: Der Server hat mit einem unbekannten Statuscode geantwortet! (" + status + ")", Toast.LENGTH_LONG).show();
            }
        });
        download.execute();

    }
} 