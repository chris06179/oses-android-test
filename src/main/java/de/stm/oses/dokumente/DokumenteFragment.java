package de.stm.oses.dokumente;

import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.stm.oses.R;
import de.stm.oses.dialogs.FaxProtokollDetailDialogFragment;
import de.stm.oses.fax.FaxActivity;
import de.stm.oses.helper.FileDownload;
import de.stm.oses.helper.FileDownload.OnDownloadFinishedListener;
import de.stm.oses.helper.ListClass;
import de.stm.oses.helper.ListSpinnerAdapter;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.dialogs.ZeitraumDialogFragment;

public class DokumenteFragment extends Fragment implements View.OnClickListener {

    private OSESBase OSES;
    private Calendar selectedDate;
    private EditText date_text;
    private Spinner type;
    private Spinner exclude;

    private FirebaseAnalytics mFirebaseAnalytics;

    public DokumenteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        OSES = new OSESBase(getActivity());
        selectedDate = Calendar.getInstance();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dokumente, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        type = (Spinner) getActivity().findViewById(R.id.dokumente_typ);
        exclude = (Spinner) getActivity().findViewById(R.id.dokumente_skip);

        type.setAdapter(OSES.getDokumenteAdapter());
        ((ListSpinnerAdapter) type.getAdapter()).setshowRadio(false);
        exclude.setAdapter(OSES.getExcludeAdapter());
        ((ListSpinnerAdapter) exclude.getAdapter()).setshowRadio(false);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Dokumente");
        date_text = ((EditText) getActivity().findViewById(R.id.dokumente_date_text));

        setDateText();

        type.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setDateText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        exclude.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                LinearLayout excludebox = (LinearLayout) getActivity().findViewById(R.id.LinearLayout20);
                if (id > 0)
                    excludebox.setVisibility(View.VISIBLE);
                else
                    excludebox.setVisibility(View.GONE);


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        new GetProtokoll().execute();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dokumente_menu, menu);
    }

    public void setDateText() {

        DateFormat formatter;

        if (type.getSelectedItemPosition() != 2)
            formatter = new SimpleDateFormat("MMMM yyyy", Locale.GERMAN);
        else
            formatter = new SimpleDateFormat("yyyy", Locale.GERMAN);

        date_text.setText(formatter.format(selectedDate.getTime()));
        date_text.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_doc_download:
                ErstellenOnClick(0);
                return true;
            case R.id.action_doc_fax:
                ErstellenOnClick(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200)
            new GetProtokoll().execute();
    }

    public void ErstellenOnClick(int action) {

        EditText Excludes = (EditText) getActivity().findViewById(R.id.editText1);

        final String typ;
        String zeitraum;
        Integer monat;
        Integer jahr;
        String excludestring = "";

        monat = selectedDate.get(Calendar.MONTH) + 1;
        jahr = selectedDate.get(Calendar.YEAR);


        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.GERMAN);


        zeitraum = sdf.format(selectedDate.getTime());

        switch (type.getSelectedItemPosition()) {
            case 0:
                typ = "ausbleibe";
                break;
            case 1:
                typ = "auslagen";
                break;
            case 2:
                typ = "steuer";
                break;
            default:
                typ = "";
        }

        if (exclude.getSelectedItemPosition() == 1) {
            excludestring = "&excludetype=SKIP&exclude=" + Excludes.getText().toString();
        }

        if (exclude.getSelectedItemPosition() == 2) {
            excludestring = "&excludetype=FREE&exclude=" + Excludes.getText().toString();
        }


        if (action == 0) { //Herunterladen

            String url = "https://oses.mobi/api.php?request=download&session=" + OSES.getSession().getIdentifier() + "&monat=" + monat + "&jahr=" + jahr + "&command=" + typ + excludestring;
            FileDownload download = new FileDownload(getActivity());
            download.setTitle(((ListClass) type.getSelectedItem()).getTitle());
            download.setMessage("Das Dokument wird heruntergeladen, dieser Vorgang kann einen Moment dauern...");
            download.setURL(url);
            download.setLocalDirectory("Dokumente/Nebengeld/");
            download.setOnDownloadFinishedListener(new OnDownloadFinishedListener() {
                @Override
                public void onDownloadFinished(File file) {

                    Bundle extra = new Bundle();
                    extra.putString("typ", typ);
                    extra.putString("status", "200");
                    mFirebaseAnalytics.logEvent("OSES_download_doc", extra);

                    Uri fileUri = FileProvider.getUriForFile(getActivity(),"de.stm.oses.FileProvider", file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION+Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivity(intent);
                }

                @Override
                public void onTextReceived(String res) {

                    int iStatus;
                    String sStatusBody;
                    String sStatusExtra1 = "";

                    try {

                        JSONObject json = new JSONObject(res);

                        iStatus = json.getInt("Status");
                        sStatusBody = json.getString("StatusBody");

                        if (json.has("StatusExtra1"))
                            sStatusExtra1 = json.getString("StatusExtra1");

                        Bundle extra = new Bundle();
                        extra.putString("typ", typ);
                        extra.putString("status", String.valueOf(iStatus));
                        mFirebaseAnalytics.logEvent("OSES_download_doc", extra);


                        switch (iStatus) {
                            case 400:
                                Toast.makeText(getActivity(), sStatusBody, Toast.LENGTH_LONG).show();
                                break;
                            case 410:
                                JSONArray jaEst = new JSONArray(sStatusExtra1);
                                String sMessage = sStatusBody + "\n\n";

                                for (int i = 0; i < jaEst.length(); i++) {
                                    JSONObject joEst = jaEst.getJSONObject(i);
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
                                    Date date = format.parse(joEst.getString("datum"));
                                    format = new SimpleDateFormat("dd.MM.", Locale.GERMAN);
                                    sMessage += joEst.getString("name") + " (" + joEst.getString("ril100") + ") am " + format.format(date) + "\n";
                                }

                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Fehler")
                                        .setMessage(sMessage)
                                        .setPositiveButton("Berichtigen (Web)", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String url = "https://oses.mobi/system.php?action=pers#ESTA";
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse(url));
                                                startActivity(i);
                                            }
                                        })
                                        .setNegativeButton("Schließen", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
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

                    Bundle extra = new Bundle();
                    extra.putString("typ", typ);
                    extra.putString("status", "EXCEPTION");
                    mFirebaseAnalytics.logEvent("OSES_download_doc", extra);

                    if (e instanceof FileDownload.NoDownloadPermissionException)
                        Toast.makeText(getActivity(), "Berechtigungsfehler: Die Datei konnte wegen fehlender Berechtigungen nicht auf das Gerät heruntergeladen werden!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), "Anwendungsfehler: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUnknownStatus(int status) {

                    Bundle extra = new Bundle();
                    extra.putString("typ", typ);
                    extra.putString("status", String.valueOf(status));
                    mFirebaseAnalytics.logEvent("OSES_download_doc", extra);

                    Toast.makeText(getActivity(), "Anwendungsfehler: Der Server hat mit einem unbekannten Statuscode geantwortet! (" + String.valueOf(status) + ")", Toast.LENGTH_LONG).show();
                }
            });
            download.execute();

        }

        if (action == 1) { //Faxversand

            if (type.getSelectedItemPosition() == 2) {
                Toast.makeText(getActivity(), "Steuernachweise sind für den Faxversand gesperrt, bitte drucke dieses Dokument über einen lokalen Drucker!", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(getActivity(), FaxActivity.class);
            intent.putExtra("type", typ);
            intent.putExtra("monat", String.valueOf(monat));
            intent.putExtra("jahr", String.valueOf(jahr));
            intent.putExtra("date", zeitraum);
            intent.putExtra("excludestring", excludestring);
            startActivityForResult(intent, 500);


        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dokumente_date_text:
                ZeitraumDialogFragment zeitraum = ZeitraumDialogFragment.newInstance(new ZeitraumDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(int year, int monthOfYear, int dayOfMonth) {

                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        setDateText();

                    }
                }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), 1);

                if (type.getSelectedItemPosition() == 2)
                    zeitraum.setHideMonth(true);

                zeitraum.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "zeitraumdialog");
                break;
        }
    }

    private class GetProtokoll extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {

            return OSES.getJSON("https://oses.mobi/api.php?request=fax_protokoll&json=true&session=" + OSES.getSession().getIdentifier(), 60000);

        }

        protected void onPostExecute(String response) {

            if (getActivity() == null)
                return;

            final LinearLayout list = (LinearLayout) getActivity().findViewById(R.id.dokumente_fax_protokoll);

            if (list == null)
                return;

            list.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            inflater.inflate(R.layout.fax_protokoll_divider, list);

            try {

                JSONArray protokoll = new JSONArray(response);

                for (int i = 0; i < protokoll.length(); i++) {

                    final JSONObject eintrag = protokoll.getJSONObject(i);
                    final Bundle details = new Bundle();

                    View rowView = inflater.inflate(R.layout.fax_protokoll_item, list, false);

                    TextView datum = (TextView) rowView.findViewById(R.id.fax_protokoll_date);
                    TextView description = (TextView) rowView.findViewById(R.id.fax_protokoll_description);
                    TextView destination = (TextView) rowView.findViewById(R.id.fax_protokoll_destination);

                    TextView units = (TextView) rowView.findViewById(R.id.fax_protokoll_units);
                    TextView costs = (TextView) rowView.findViewById(R.id.fax_protokoll_costs);
                    TextView sum = (TextView) rowView.findViewById(R.id.fax_protokoll_sum);

                    ImageView icon = (ImageView) rowView.findViewById(R.id.fax_protokoll_icon);

                    SimpleDateFormat inputdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
                    SimpleDateFormat outputdate = new SimpleDateFormat("dd. MMMM yyyy HH:mm:ss", Locale.GERMAN);

                    details.putString("id", eintrag.optString("id"));

                    final String startdate = outputdate.format(inputdate.parse(eintrag.optString("time", "")));

                    final String completedate;

                    if (!eintrag.isNull("completiontime"))
                        completedate = outputdate.format(inputdate.parse(eintrag.optString("completiontime")));
                    else
                        completedate = "";

                    details.putString("date", startdate);
                    details.putString("completion", completedate);

                    datum.setText(startdate);

                    description.setText(eintrag.optString("description", ""));
                    details.putString("doc", eintrag.optString("description", ""));

                    destination.setText(eintrag.optString("destname", ""));
                    if (eintrag.optString("destname", "").startsWith("Eigene Nummer:"))
                        details.putString("ort", "Unbekannt (manuelle Rufnummer)");
                    else
                        details.putString("ort", eintrag.optString("destname", ""));
                    details.putString("number", eintrag.optString("dest", ""));

                    double dUnits = eintrag.optDouble("units", 0);
                    double dCosts = eintrag.optDouble("costperunit", 0) * 1.19;
                    double dSum = dUnits * dCosts;

                    units.setText(String.format("%.1f", dUnits) + " Einheiten");
                    costs.setText("x " + String.format("%.4f", dCosts) + " €");
                    sum.setText("= " + String.format("%.2f", dSum) + " €");

                    details.putString("units", String.format("%.1f", dUnits));
                    details.putString("costs", String.format("%.4f", dCosts) + " €");
                    details.putString("sum", String.format("%.2f", dSum) + " €");


                    if (eintrag.getString("status").equals("SUCCESS")) {
                        icon.setImageResource(R.drawable.ic_action_accept);
                        details.putString("status", "Erfolgreich");
                        details.putString("statustext", eintrag.optString("statusdescription", ""));
                    }

                    if (eintrag.getString("status").equals("ERROR")) {
                        icon.setImageResource(R.drawable.ic_action_cancel);
                        details.putString("status", "Fehler");
                        details.putString("statustext", eintrag.optString("statusdescription", ""));
                    }

                    if (eintrag.getString("status").equals("TRANSMITTED")) {
                        icon.setImageResource(R.drawable.ic_action_send_now);
                        details.putString("status", "Erzeugt");
                    }

                    if (eintrag.getString("status").equals("START")) {
                        icon.setImageResource(R.drawable.ic_action_refresh);
                        details.putString("status", "Wird erstellt");
                    }

                    rowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            DialogFragment faxDetail = FaxProtokollDetailDialogFragment.newInstance(details);
                            faxDetail.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "faxdetail");

                        }
                    });

                    list.addView(rowView);

                    if (i < protokoll.length() - 1) {
                        inflater.inflate(R.layout.fax_protokoll_divider, list);
                    }


                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }

    }

}
