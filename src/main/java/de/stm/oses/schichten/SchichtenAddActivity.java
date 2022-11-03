package de.stm.oses.schichten;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.codetroopers.betterpickers.timepicker.TimePickerBuilder;
import com.codetroopers.betterpickers.timepicker.TimePickerDialogFragment;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.stm.oses.R;
import de.stm.oses.helper.ListAdapter;
import de.stm.oses.helper.ListClass;
import de.stm.oses.helper.ListSpinnerAdapter;
import de.stm.oses.helper.OSESBase;


public class SchichtenAddActivity extends AppCompatActivity implements View.OnClickListener {

    String SessionIdentifier;
    private boolean ValidRil100 = false;
    private String sid = "";
    private AsyncTask<Void, Void, String> ril100Task;
    private OSESBase OSES;

    private ListAdapter ests;
    private ListAdapter funktionen;
    private ListAdapter pausen;
    private ListAdapter gbereiche;

    private SchichtAddHolder sAdd;

    private FirebaseAnalytics mFirebaseAnalytics;

    static class SchichtAddHolder {

        Toolbar toolbar;
        Button back;
        Button save;
        ToggleButton abcbutton;

        LinearLayout baureihen_box;
        LinearLayout pause_box;
        LinearLayout auf_box;
        LinearLayout fpla_box;

        TextView auftitle;

        ImageView rilstatus;
        ProgressBar rilprogress;

        EditText db;
        EditText de;
        EditText schicht;
        EditText fpla;
        EditText von;
        EditText bis;
        EditText pausein;
        EditText baureihen;
        EditText notiz;
        EditText aufdb;
        EditText aufde;
        EditText aufdz;

        Spinner gbereich;
        Spinner funktion;
        Spinner pause;
        Spinner est;

        public SchichtAddHolder(SchichtenAddActivity activity) {
            toolbar = activity.findViewById(R.id.schichtadd_toolbar);

            back = activity.findViewById(R.id.schichtadd_back);
            save = activity.findViewById(R.id.schichtadd_save);
            abcbutton = activity.findViewById(R.id.schichtadd_abcbutton);

            baureihen_box = activity.findViewById(R.id.schichtadd_baureihen_box);
            pause_box = activity.findViewById(R.id.schichtadd_pause_box);
            auf_box = activity.findViewById(R.id.schichtadd_auf_box);
            fpla_box = activity.findViewById(R.id.schichtadd_fpla_box);

            auftitle = activity.findViewById(R.id.schichtadd_auftitle);

            rilstatus = activity.findViewById(R.id.schichtadd_rilstatus);
            rilprogress = activity.findViewById(R.id.schichtadd_rilprogress);

            db = activity.findViewById(R.id.schichtadd_db);
            de = activity.findViewById(R.id.schichtadd_de);
            schicht = activity.findViewById(R.id.schichtadd_schicht);
            fpla = activity.findViewById(R.id.schichtadd_fpla);
            von = activity.findViewById(R.id.schichtadd_von);
            bis = activity.findViewById(R.id.schichtadd_bis);
            pausein = activity.findViewById(R.id.schichtadd_pausein);
            baureihen = activity.findViewById(R.id.schichtadd_baureihen);
            notiz = activity.findViewById(R.id.schichtadd_notiz);
            aufdb = activity.findViewById(R.id.schichtadd_aufdb);
            aufde = activity.findViewById(R.id.schichtadd_aufde);
            aufdz = activity.findViewById(R.id.schichtadd_aufdz);

            gbereich = activity.findViewById(R.id.schichtadd_gbereich);
            funktion = activity.findViewById(R.id.schichtadd_funktion);
            pause = activity.findViewById(R.id.schichtadd_pause);
            est = activity.findViewById(R.id.schichtadd_est);

            back.setOnClickListener(activity);
            save.setOnClickListener(activity);
            db.setOnClickListener(activity);
            de.setOnClickListener(activity);
            von.setOnClickListener(activity);
            bis.setOnClickListener(activity);
            abcbutton.setOnClickListener(activity);
            aufdz.setOnClickListener(activity);
            aufdb.setOnClickListener(activity);
            aufde.setOnClickListener(activity);
        }
    }


    private boolean saveMultiple = true;

	//Your member variable declaration here

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schichtadd);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initiliasiere Views
        sAdd = new SchichtAddHolder(SchichtenAddActivity.this);

        setSupportActionBar(sAdd.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Referenzschicht hinzufügen");

        if (savedInstanceState != null) {
            saveMultiple = savedInstanceState.getBoolean("saveMultiple");
        }

        setResult(400);

        OSES = new OSESBase(this);

        try {
            ests = OSES.getEstAdapter(OSES.getSession().getEst(), true);
            funktionen = OSES.getFunktionAdapter(Integer.parseInt(Integer.toString(OSES.getSession().getFunktion()).substring(0, 1)), true);
            pausen = OSES.getPauseAdapter(0, false);
            gbereiche = OSES.getGBAdapter(OSES.getSession().getGB(), true);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(SchichtenAddActivity.this, "Fehler: Daten konnten nicht zusammen gestellt werden!", Toast.LENGTH_LONG).show();
            return;
        }
	
		SharedPreferences settings = getSharedPreferences("OSESPrefs", 0);
        SessionIdentifier = settings.getString("SessionIdentifier", "");
        
		Calendar c = Calendar.getInstance();

        SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.GERMAN);
		
		sAdd.von.setText(dayFormat.format(c.getTime()) + ", " + pad(c.get(Calendar.DAY_OF_MONTH)) + "." + pad(c.get(Calendar.MONTH) + 1) + "." + pad(c.get(Calendar.YEAR)));

        sAdd.bis.setText(dayFormat.format(c.getTime()) + ", " + pad(c.get(Calendar.DAY_OF_MONTH)) + "." + pad(c.get(Calendar.MONTH) + 1) + "." + pad(c.get(Calendar.YEAR)));


        sAdd.gbereich.setAdapter(gbereiche);
        sAdd.gbereich.setSelection(gbereiche.getSelection());

        sAdd.gbereich.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                gbereiche.setSelection((int) l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sAdd.est.setAdapter(ests);
        sAdd.est.setSelection(ests.getSelection());

        sAdd.est.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ests.setSelection((int) l);
                sAdd.auftitle.setText("Aufenthalt in Einsatzstelle " + ests.getItem(i).getTitle());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        sAdd.funktion.setAdapter(funktionen);
        sAdd.funktion.setSelection(funktionen.getSelection());

        sAdd.funktion.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                funktionen.setSelection((int) id);

                if (funktionen.getItem((int) id).getId() == 1)
                    sAdd.baureihen_box.setVisibility(View.VISIBLE);
                else
                    sAdd.baureihen_box.setVisibility(View.GONE);

                if (funktionen.getItem((int) id).getId() == 5) {
                    sAdd.fpla_box.setVisibility(View.GONE);
                    sAdd.auf_box.setVisibility(View.GONE);
                    sAdd.abcbutton.setChecked(true);
                } else {
                    sAdd.fpla_box.setVisibility(View.VISIBLE);
                    sAdd.auf_box.setVisibility(View.VISIBLE);
                    sAdd.abcbutton.setChecked(false);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        

        sAdd.pause.setAdapter(pausen);
        sAdd.pause.setSelection(pausen.getSelection());

        sAdd.pause.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                pausen.setSelection((int) id);

                if (pausen.getItem((int) id).getId() > 0)
                    sAdd.pause_box.setVisibility(View.VISIBLE);
                else {
                    sAdd.pause_box.setVisibility(View.GONE);
                    sAdd.pausein.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        
               
        sAdd.pausein.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 1) {
                    sAdd.rilstatus.setVisibility(View.GONE);
                    sAdd.rilprogress.setVisibility(View.VISIBLE);
                    if (ril100Task != null)
                        ril100Task.cancel(true);
                    ril100Task = new GetRil100().execute();
                } else {
                    if (ril100Task != null)
                        ril100Task.cancel(true);

                    sAdd.rilstatus.setImageResource(R.drawable.icon_delete);
                    sAdd.rilstatus.setVisibility(View.VISIBLE);
                    sAdd.rilprogress.setVisibility(View.GONE);
                    ValidRil100 = false;
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }


        });

        sAdd.abcbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    sAdd.schicht.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                else
                    sAdd.schicht.setInputType(InputType.TYPE_CLASS_PHONE);
            }
        });

        if (getIntent().hasExtra("item")) {

        SchichtenClass edit = ((SchichtenClass) getIntent().getExtras().getSerializable("item"));

            sid = String.valueOf(edit.getId());

            getSupportActionBar().setTitle("Referenzschicht bearbeiten");

            sAdd.schicht.setText(edit.getSchicht());
            if (!edit.getFpla().equals("0"))
                sAdd.fpla.setText(edit.getFpla());
            ((ListSpinnerAdapter) sAdd.est.getAdapter()).setSelectionID(edit.getEstid());
            sAdd.est.setSelection(((ListSpinnerAdapter) sAdd.est.getAdapter()).getSelection());

            ((ListSpinnerAdapter) sAdd.gbereich.getAdapter()).setSelectionID(edit.getGbid());
            sAdd.gbereich.setSelection(((ListSpinnerAdapter) sAdd.gbereich.getAdapter()).getSelection());

            ((ListSpinnerAdapter) sAdd.funktion.getAdapter()).setSelectionID(edit.getFunktionid());
            sAdd.funktion.setSelection(((ListSpinnerAdapter) sAdd.funktion.getAdapter()).getSelection());

            if (!edit.getPause().equals("-")) {

                int pause_zeit = Integer.parseInt(edit.getPause().substring(0, 2));

                ((ListSpinnerAdapter) sAdd.pause.getAdapter()).setSelectionID(pause_zeit);
                sAdd.pause.setSelection(((ListSpinnerAdapter) sAdd.pause.getAdapter()).getSelection());

                sAdd.pausein.setText(edit.getPauseOrt());

            }

            sAdd.db.setText(edit.getDb());
            sAdd.de.setText(edit.getDe());

            sAdd.aufdb.setText(String.format(Locale.GERMAN,"%02d:%02d", TimeUnit.MINUTES.toHours(edit.getAufdb()), edit.getAufdb() - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(edit.getAufdb()))));
            sAdd.aufde.setText(String.format(Locale.GERMAN,"%02d:%02d", TimeUnit.MINUTES.toHours(edit.getAufde()), edit.getAufde() - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(edit.getAufde()))));
            sAdd.aufdz.setText(String.format(Locale.GERMAN,"%02d:%02d", TimeUnit.MINUTES.toHours(edit.getAufdz()), edit.getAufdz() - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(edit.getAufdz()))));


            sAdd.baureihen.setText(edit.getBaureihen());
            sAdd.notiz.setText(edit.getKommentar());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

            Date vondate = new Date();
            Date bisdate = new Date();

            try {
                vondate = dateFormat.parse(edit.getGv());
                bisdate = dateFormat.parse(edit.getGb());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            c.setTime(vondate);
            sAdd.von.setText(dayFormat.format(c.getTime())+", "+pad(c.get(Calendar.DAY_OF_MONTH))+"."+pad(c.get(Calendar.MONTH)+1)+"."+pad(c.get(Calendar.YEAR)));

            c.setTime(bisdate);
            sAdd.bis.setText(dayFormat.format(c.getTime())+", "+pad(c.get(Calendar.DAY_OF_MONTH))+"."+pad(c.get(Calendar.MONTH)+1)+"."+pad(c.get(Calendar.YEAR)));

        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

	}


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("saveMultiple", saveMultiple);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        if (!getIntent().hasExtra("item")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.schichten_add_menu, menu);
            menu.findItem(R.id.action_save_schicht_multiple).setChecked(saveMultiple);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save_schicht_multiple:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                saveMultiple = item.isChecked();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	
            
	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + c;
	}

    @Override
    public void onClick(View view) {

        // Handle presses on the action bar items
        switch (view.getId()) {
            case R.id.schichtadd_back:
                finish();
                break;
            case R.id.schichtadd_save:
                SchichtAddOnClick();
                break;
            case R.id.schichtadd_db:
                ZeitOnClick(sAdd.db);
                break;
            case R.id.schichtadd_de:
                ZeitOnClick(sAdd.de);
                break;
            case R.id.schichtadd_von:
                DatumOnClick(sAdd.von);
                break;
            case R.id.schichtadd_bis:
                DatumOnClick(sAdd.bis);
                break;
            case R.id.schichtadd_aufdz:
                ZeitOnClick(sAdd.aufdz);
                break;
            case R.id.schichtadd_aufdb:
                ZeitOnClick(sAdd.aufdb);
                break;
            case R.id.schichtadd_aufde:
                ZeitOnClick(sAdd.aufde);
                break;
        }


    }



    private class GetRil100 extends AsyncTask<Void, Void, String> {
	

	protected String doInBackground(Void... params) {

            return OSES.getJSON("https://oses.mobi/api.php?request=suche_bst&session=" + SessionIdentifier + "&ril100=" + sAdd.pausein.getText(), 30000);

	}
	
	protected void onPostExecute(String response) {

        if (sAdd == null || sAdd.rilprogress == null || sAdd.rilstatus == null || response == null)
            return;

        sAdd.rilprogress.setVisibility(View.GONE);
	 
     if (response.length() > 0) {
    	 sAdd.rilstatus.setImageResource(R.drawable.icon_check);
         sAdd.rilstatus.setVisibility(View.VISIBLE);
    	 ValidRil100 = true;
     }
     else {
         sAdd.rilstatus.setImageResource(R.drawable.icon_delete);
    	 ValidRil100 = false;
         sAdd.rilstatus.setVisibility(View.VISIBLE);
     }    
        
		
		
	}
        
 }

private class SaveSchicht extends AsyncTask<Void, Void, String> {

    private ProgressDialog dialog;

    protected void onPreExecute() {

        dialog = ProgressDialog.show(SchichtenAddActivity.this, "Bitte warten...",
                "Verwendung wird gespeichert...", true);

    }

    protected String doInBackground(Void... params) {

        // Datum parsen
        String[] splitVon = sAdd.von.getText().toString().substring(5).split("\\.");
        String gv = splitVon[2] + '-' + splitVon[1] + '-' + splitVon[0];

        String[] splitBis = sAdd.bis.getText().toString().substring(5).split("\\.");
        String gb = splitBis[2] + '-' + splitBis[1] + '-' + splitBis[0];

        // Hashmap aufbauen
        Map<String, String> postdata = new HashMap<>();

        // Session
        postdata.put("session", SessionIdentifier);

        // Erforderliche Informationen
        postdata.put("sid", sid);
        postdata.put("schicht", sAdd.schicht.getText().toString());
        postdata.put("fpla", sAdd.fpla.getText().toString());
        postdata.put("db", sAdd.db.getText().toString());
        postdata.put("de", sAdd.de.getText().toString());
        postdata.put("gv", gv);
        postdata.put("gb", gb);
        postdata.put("est", String.valueOf(((ListClass) sAdd.est.getSelectedItem()).getId()));
        postdata.put("gbereich", String.valueOf(((ListClass) sAdd.gbereich.getSelectedItem()).getId()));
        postdata.put("funktion", String.valueOf(((ListClass) sAdd.funktion.getSelectedItem()).getId()));
        postdata.put("pause", String.valueOf(((ListClass) sAdd.pause.getSelectedItem()).getId()));
        postdata.put("pause_ort", sAdd.pausein.getText().toString());
        postdata.put("baureihen", sAdd.baureihen.getText().toString());
        postdata.put("kommentar", sAdd.notiz.getText().toString());
        postdata.put("aufdb", sAdd.aufdb.getText().toString());
        postdata.put("aufde", sAdd.aufde.getText().toString());
        postdata.put("aufdz", sAdd.aufdz.getText().toString());


        return OSES.getJSON("https://oses.mobi/api.php?request=schichten&command=addajax", postdata, 60000);

    }

    protected void onPostExecute(String response) {

        String Status = "900";
        String StatusHead = "Interner Fehler";
        String StatusBody = "Verarbeitung fehlgeschlagen";

        JSONObject json = new JSONObject();

        try {

            json = new JSONObject(response);

            Status = json.getString("Status");
            StatusHead = json.getString("StatusHead");
            StatusBody = json.getString("StatusBody");

        } catch (Exception e) {

        }

        Bundle extra = new Bundle();
        extra.putString("status", Status);
        mFirebaseAnalytics.logEvent("OSES_schicht_save", extra);

        if (Status.equals("200")) {
            dialog.dismiss();
            setResult(200);
            if (!saveMultiple)
                finish();
        }

        if (Status.equals("201")) {
            dialog.dismiss();
            setResult(200);
            finish();
        }


            Toast.makeText(getApplicationContext(), Status + ": " + StatusBody, Toast.LENGTH_LONG).show();
            dialog.dismiss();



    }
}


    public void SchichtAddOnClick() {

        boolean stop = false;

        if (sAdd.schicht.getText().length() == 0) {
            sAdd.schicht.setError("Ungültige Schichtbezeichnung");
            stop = true;
        }

        if (sAdd.db.getText().length() == 0) {
            sAdd.db.setError("Ungültiger Dienstbeginn");
            stop = true;
        }

        if (sAdd.de.getText().length() == 0) {
            sAdd.de.setError("Ungültiges Dienstende");
            stop = true;
        }

        if (ValidRil100 == false && sAdd.pause.getSelectedItemPosition() > 0) {
            sAdd.pausein.setError("Ungültige Betriebsstelle nach Ril 100");
            stop = true;
        }


        if (stop)
            return;

        new SaveSchicht().execute();

    }

    public void DatumCheck(TextView setter) {

        final Calendar cStart = Calendar.getInstance();
        final Calendar cEnde = Calendar.getInstance();
        final SimpleDateFormat dayFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.GERMAN);

        try {
            cStart.setTime(dayFormat.parse(sAdd.von.getText().toString()));
            cEnde.setTime(dayFormat.parse(sAdd.bis.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (setter.getId()) {
            case R.id.schichtadd_von:
                if (cStart.getTime().after(cEnde.getTime())) {
                    sAdd.bis.setText(dayFormat.format(cStart.getTime()));
                }
                break;
            case R.id.schichtadd_bis:
                if (cEnde.getTime().before(cStart.getTime())) {
                    sAdd.von.setText(dayFormat.format(cEnde.getTime()));
                }
                break;
        }

    }


    public void DatumOnClick (final TextView datumSet)  {

        final Calendar cSet = Calendar.getInstance();
        final SimpleDateFormat dayFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.GERMAN);

        try {
            cSet.setTime(dayFormat.parse(datumSet.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        DatePickerDialog dpd = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
                        cSet.set(year, monthOfYear, dayOfMonth);
                        datumSet.setText(dayFormat.format(cSet.getTime()));

                        DatumCheck(datumSet);
                    }
                }, cSet.get(Calendar.YEAR), cSet.get(Calendar.MONTH), cSet.get(Calendar.DAY_OF_MONTH));


        dpd.show(getSupportFragmentManager(), "Datepickerdialog");

    }
	
	public void ZeitOnClick (final TextView Zeit) {
		
		Integer hour;
		Integer minute;
		
		if (Zeit.getText().length() > 0) {
		
			String[] split = Zeit.getText().toString().split(":");
			hour = Integer.parseInt(split[0]);
			minute = Integer.parseInt(split[1]);			
			
		} else {					

			hour = 0;
			minute = 0;
			
		}

        if (OSES.getSession().getPreferences().getBoolean("useAlternateTimePicker", false)) {

            TimePickerBuilder tpb = new TimePickerBuilder()
                    .setFragmentManager(getSupportFragmentManager())
                    .setStyleResId(R.style.OSESBetterPickerTheme)
                    .addTimePickerDialogHandler(new TimePickerDialogFragment.TimePickerDialogHandler() {
                        @Override
                        public void onDialogTimeSet(int reference, int hour, int minute) {
                            Zeit.setText(pad(hour) + ":" + pad(minute));
                            Zeit.setError(null);
                        }
                    });
            tpb.show();


        } else {



            TimePickerDialog tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePickerDialog view, int hour, int minute, int second) {
                    Zeit.setText(pad(hour) + ":" + pad(minute));
                    Zeit.setError(null);
                }
            }, hour, minute, true);
            tpd.show(getSupportFragmentManager(), "timepickerdialog");
        }
		
    }

}
	
