package de.stm.oses.verwendung;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.codetroopers.betterpickers.timepicker.TimePickerBuilder;
import com.codetroopers.betterpickers.timepicker.TimePickerDialogFragment;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.stm.oses.R;
import de.stm.oses.dialogs.ProgressDialogFragment;
import de.stm.oses.helper.ListAdapter;
import de.stm.oses.helper.ListClass;
import de.stm.oses.helper.ListSpinnerAdapter;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.OSESRequest;


public class VerwendungAddActivity extends AppCompatActivity implements View.OnClickListener {

    private AsyncTask<Void, Void, String> ril100Task;
    private OSESBase OSES;
    private String sid = "";

    private ListAdapter ests;
    private ListAdapter funktionen;
    private ListAdapter pausen;
    private ListAdapter pausenabw;
    private ListAdapter kategorien;

    private boolean saveMultiple = true;
    private boolean increaseDate = true;

    private VerwendungAddHolder vAdd;
    private FirebaseAnalytics mFirebaseAnalytics;

    private static class VerwendungAddHolder {

        Toolbar toolbar;
        Button back;
        Button save;
        ToggleButton abcbutton;
        ImageButton search;
        ImageButton delade;
        ImageButton deladb;
        ImageButton delapause;
        ImageButton deloaz;

        ProgressBar searchprogress;

        LinearLayout scroll_linear_box;
        CardView kategorie_box;
        CardView urlaub_box;
        CardView buero_box;
        CardView dispo_box;
        CardView schicht_box;
        CardView datum_box;
        LinearLayout zeitraum_box;
        LinearLayout datumEnde_box;
        CardView az_box;
        CardView oaz_box;
        CardView auf_box;
        CardView sonstiges_box;
        CardView abweichungen_box;
        LinearLayout baureihen_box;
        LinearLayout pausein_box;
        LinearLayout fpla_box;
        LinearLayout ubeschreibung_box;

        TextView aufest;
        TextView auf_erste_tgk;

        ImageView rilstatus;
        ProgressBar rilprogress;

        EditText db;
        EditText de;
        EditText adb;
        EditText ade;
        EditText dbr;
        EditText der;
        EditText apauser;
        EditText schicht;
        EditText fpla;
        EditText datumStart;
        EditText datumEnde;
        EditText pausein;
        EditText baureihen;
        EditText notiz;
        EditText aufdb;
        EditText aufde;
        EditText aufdz;
        EditText oaz;
        EditText ubeschreibung;
        EditText bbeschreibung;

        Spinner kategorie;
        Spinner funktion;
        Spinner pause;
        Spinner apause;
        Spinner est;
        Spinner best;
        Spinner adban;
        Spinner adean;
        Spinner dispotyp;
        Spinner urlaubtyp;

        CheckBox zeitraum;

        VerwendungAddHolder(VerwendungAddActivity activity) {
            toolbar = activity.findViewById(R.id.verwendungadd_toolbar);

            back = activity.findViewById(R.id.verwendungadd_back);
            save = activity.findViewById(R.id.verwendungadd_save);
            abcbutton = activity.findViewById(R.id.verwendungadd_abcbutton);

            search = activity.findViewById(R.id.verwendungadd_search);
            delade = activity.findViewById(R.id.verwendungadd_delade);
            deladb = activity.findViewById(R.id.verwendungadd_deladb);
            delapause = activity.findViewById(R.id.verwendungadd_delapause);
            deloaz = activity.findViewById(R.id.verwendungadd_deloaz);

            searchprogress = activity.findViewById(R.id.verwendungadd_searchprogress);

            scroll_linear_box = activity.findViewById(R.id.verwendungadd_scroll_linear);
            kategorie_box = activity.findViewById(R.id.verwendungadd_kategorie_box);
            urlaub_box = activity.findViewById(R.id.verwendungadd_urlaub_box);
            buero_box = activity.findViewById(R.id.verwendungadd_buero_box);
            dispo_box = activity.findViewById(R.id.verwendungadd_dispo_box);
            schicht_box = activity.findViewById(R.id.verwendungadd_schicht_box);
            datum_box = activity.findViewById(R.id.verwendungadd_datum_box);
            datumEnde_box = activity.findViewById(R.id.verwendungadd_datumEnde_box);
            zeitraum_box = activity.findViewById(R.id.verwendungadd_zeitraum_box);
            az_box = activity.findViewById(R.id.verwendungadd_az_box);
            oaz_box = activity.findViewById(R.id.verwendungadd_oaz_box);
            auf_box = activity.findViewById(R.id.verwendungadd_auf_box);
            sonstiges_box = activity.findViewById(R.id.verwendungadd_sonstiges_box);
            abweichungen_box = activity.findViewById(R.id.verwendungadd_abweichungen_box);
            baureihen_box = activity.findViewById(R.id.verwendungadd_baureihen_box);
            pausein_box = activity.findViewById(R.id.verwendungadd_pausein_box);
            fpla_box = activity.findViewById(R.id.verwendungadd_fpla_box);
            ubeschreibung_box = activity.findViewById(R.id.verwendungadd_ubeschreibung_box);

            aufest = activity.findViewById(R.id.verwendungadd_auf_est);
            auf_erste_tgk = activity.findViewById(R.id.verwendungadd_auf_erste_tgk);

            rilstatus = activity.findViewById(R.id.verwendungadd_rilstatus);
            rilprogress = activity.findViewById(R.id.verwendungadd_rilprogress);

            db = activity.findViewById(R.id.verwendungadd_db);
            de = activity.findViewById(R.id.verwendungadd_de);
            adb = activity.findViewById(R.id.verwendungadd_adb);
            ade = activity.findViewById(R.id.verwendungadd_ade);
            dbr = activity.findViewById(R.id.verwendungadd_dbr);
            der = activity.findViewById(R.id.verwendungadd_der);
            apauser = activity.findViewById(R.id.verwendungadd_apauser);
            schicht = activity.findViewById(R.id.verwendungadd_bezeichner);
            fpla = activity.findViewById(R.id.verwendungadd_fpla);
            datumStart = activity.findViewById(R.id.verwendungadd_datumStart);
            datumEnde = activity.findViewById(R.id.verwendungadd_datumEnde);
            pausein = activity.findViewById(R.id.verwendungadd_pausein);
            baureihen = activity.findViewById(R.id.verwendungadd_baureihen);
            notiz = activity.findViewById(R.id.verwendungadd_notiz);
            aufdb = activity.findViewById(R.id.verwendungadd_aufdb);
            aufde = activity.findViewById(R.id.verwendungadd_aufde);
            aufdz = activity.findViewById(R.id.verwendungadd_aufdz);
            oaz = activity.findViewById(R.id.verwendungadd_oaz);
            ubeschreibung = activity.findViewById(R.id.verwendungadd_ubeschreibung);
            bbeschreibung = activity.findViewById(R.id.verwendungadd_bbeschreibung);

            kategorie = activity.findViewById(R.id.verwendungadd_kategorie);
            funktion = activity.findViewById(R.id.verwendungadd_funktion);
            pause = activity.findViewById(R.id.verwendungadd_pause);
            apause = activity.findViewById(R.id.verwendungadd_apause);
            est = activity.findViewById(R.id.verwendungadd_est);
            best = activity.findViewById(R.id.verwendungadd_best);
            adban = activity.findViewById(R.id.verwendungadd_adban);
            adean = activity.findViewById(R.id.verwendungadd_adean);
            dispotyp = activity.findViewById(R.id.verwendungadd_dispotyp);
            urlaubtyp = activity.findViewById(R.id.verwendungadd_urlaubtyp);

            zeitraum = activity.findViewById(R.id.verwendungadd_zeitraum);

            back.setOnClickListener(activity);
            save.setOnClickListener(activity);
            abcbutton.setOnClickListener(activity);
            search.setOnClickListener(activity);
            delade.setOnClickListener(activity);
            deladb.setOnClickListener(activity);
            delapause.setOnClickListener(activity);
            deloaz.setOnClickListener(activity);
            db.setOnClickListener(activity);
            de.setOnClickListener(activity);
            adb.setOnClickListener(activity);
            ade.setOnClickListener(activity);
            datumStart.setOnClickListener(activity);
            datumEnde.setOnClickListener(activity);
            aufdz.setOnClickListener(activity);
            aufdb.setOnClickListener(activity);
            aufde.setOnClickListener(activity);
            oaz.setOnClickListener(activity);

        }
    }

    //Your member variable declaration here

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verwendungaddn);

        OSES = new OSESBase(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        vAdd = new VerwendungAddHolder(this);

        setSupportActionBar(vAdd.toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Verwendung hinzufügen");

        if (savedInstanceState != null) {
            saveMultiple = savedInstanceState.getBoolean("saveMultiple");
            increaseDate = savedInstanceState.getBoolean("increaseDate");
        }

        setResult(400);

        vAdd.schicht.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    InputMethodManager inputManager =
                            (InputMethodManager) getApplicationContext().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(
                            vAdd.schicht.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    SucheOnClick();

                    handled = true;
                }
                return handled;
            }
        });

        vAdd.schicht.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (vAdd.schicht.getError() != null)
                    vAdd.schicht.setError(null);
            }
        });

        vAdd.schicht.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (vAdd.schicht.getError() != null)
                    vAdd.schicht.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        vAdd.schicht.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vAdd.schicht.getError() != null)
                    vAdd.schicht.setError(null);
            }
        });

        try {
            kategorien = OSES.getKategorieAdapter("S");
            ests = OSES.getEstAdapter(OSES.getSession().getEst(), true);
            funktionen = OSES.getFunktionAdapter(Integer.parseInt(Integer.toString(OSES.getSession().getFunktion()).substring(0, 1)), true);
            pausen = OSES.getPauseAdapter(0, false);
            pausenabw = OSES.getPauseAdapter(-1, true);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(VerwendungAddActivity.this, "Fehler: Daten konnten nicht zusammen gestellt werden!", Toast.LENGTH_LONG).show();
            return;
        }

        vAdd.kategorie.setAdapter(kategorien);
        vAdd.kategorie.setSelection(kategorien.getSelection());

        vAdd.kategorie.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager inputManager =
                        (InputMethodManager) getApplicationContext().
                                getSystemService(Context.INPUT_METHOD_SERVICE);

                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(vAdd.schicht.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                view.performClick();

                return true;
            }
        });

        vAdd.kategorie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                kategorien.setSelection((int) l);

                ListClass item = kategorien.getItem(i);

                if (item == null)
                    return;

                BuildForm(item.getIdent());


                boolean bChangeColor = OSES.getSession().getPreferences().getBoolean("changeVerwendungColor", true);

                if (bChangeColor) {

                    int startcolor = Color.parseColor(item.getColor());


                    if (item.getIdent().equals("R") || item.getIdent().equals("S")) {

                        float[] hsv = new float[3];
                        int color = startcolor;
                        Color.colorToHSV(color, hsv);
                        hsv[2] *= 0.8f; // value component
                        startcolor = Color.HSVToColor(hsv);
                    }

                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(startcolor));

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        float[] hsv = new float[3];
                        int color = startcolor;
                        Color.colorToHSV(color, hsv);
                        hsv[2] *= 0.8f; // value component
                        color = Color.HSVToColor(hsv);

                        Window window = VerwendungAddActivity.this.getWindow();
                        window.setStatusBarColor(color);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        Calendar c = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.GERMAN);

        vAdd.datumStart.setText(dayFormat.format(c.getTime()) + ", " + pad(c.get(Calendar.DAY_OF_MONTH)) + "." + pad(c.get(Calendar.MONTH) + 1) + "." + pad(c.get(Calendar.YEAR)));
        vAdd.datumEnde.setText(dayFormat.format(c.getTime()) + ", " + pad(c.get(Calendar.DAY_OF_MONTH)) + "." + pad(c.get(Calendar.MONTH) + 1) + "." + pad(c.get(Calendar.YEAR)));


        vAdd.est.setAdapter(ests);
        vAdd.est.setSelection(ests.getSelection());

        vAdd.est.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ests.setSelection((int) l);

                ListClass item = ests.getItem(i);

                if (item == null)
                    return;

                vAdd.aufest.setText("Aufenthalt in " + item.getTitle());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        try {
            vAdd.best.setAdapter(OSES.getEstAdapter(OSES.getSession().getEst(), true));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        vAdd.best.setSelection(((ListSpinnerAdapter) vAdd.best.getAdapter()).getSelection());
        vAdd.best.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((ListSpinnerAdapter) vAdd.best.getAdapter()).setSelection((int) l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        vAdd.auf_erste_tgk.setText("Aufenthalt in erster Tätigkeitsstätte " + OSES.getSession().getEstText());

        vAdd.zeitraum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DatumCheck(vAdd.datumStart);
                    vAdd.datumEnde_box.setVisibility(View.VISIBLE);
                } else
                    vAdd.datumEnde_box.setVisibility(View.GONE);

            }
        });

        vAdd.funktion.setAdapter(funktionen);
        vAdd.funktion.setSelection(funktionen.getSelection());

        vAdd.funktion.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                funktionen.setSelection((int) id);

                ListClass item = funktionen.getItem((int) id);

                if (item == null)
                    return;

                if (item.getId() == 1 && getKategorie().equals("S"))
                    vAdd.baureihen_box.setVisibility(View.VISIBLE);
                else
                    vAdd.baureihen_box.setVisibility(View.GONE);

                if (item.getId() == 5) {
                    vAdd.fpla_box.setVisibility(View.GONE);
                    vAdd.auf_box.setVisibility(View.GONE);
                    vAdd.abcbutton.setChecked(true);
                } else {
                    vAdd.fpla_box.setVisibility(View.VISIBLE);
                    if (getKategorie().equals("S"))
                        vAdd.auf_box.setVisibility(View.VISIBLE);
                    vAdd.abcbutton.setChecked(false);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        vAdd.dispotyp.setAdapter(OSES.getDispoAdapter("DF"));
        vAdd.dispotyp.setSelection(((ListSpinnerAdapter) vAdd.dispotyp.getAdapter()).getSelection());
        vAdd.dispotyp.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((ListSpinnerAdapter) vAdd.dispotyp.getAdapter()).setSelection((int) id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        vAdd.urlaubtyp.setAdapter(OSES.getUrlaubAdapter("U"));
        vAdd.urlaubtyp.setSelection(((ListSpinnerAdapter) vAdd.urlaubtyp.getAdapter()).getSelection());
        vAdd.urlaubtyp.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((ListSpinnerAdapter) vAdd.urlaubtyp.getAdapter()).setSelection((int) id);

                if (((ListClass) vAdd.urlaubtyp.getSelectedItem()).getIdent().equals("UN")) {
                    vAdd.ubeschreibung_box.setVisibility(View.VISIBLE);
                } else
                    vAdd.ubeschreibung_box.setVisibility(View.GONE);

                if (!((ListClass) vAdd.urlaubtyp.getSelectedItem()).getIdent().equals("U")) {
                    vAdd.oaz_box.setVisibility(View.VISIBLE);
                } else
                    vAdd.oaz_box.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        vAdd.adban.setAdapter(OSES.getAnrechnungAdapter(100));
        ((ListSpinnerAdapter) vAdd.adban.getAdapter()).setshowRadio(false);
        vAdd.adban.setSelection(((ListSpinnerAdapter) vAdd.adban.getAdapter()).getSelection());

        vAdd.adean.setAdapter(OSES.getAnrechnungAdapter(100));
        ((ListSpinnerAdapter) vAdd.adean.getAdapter()).setshowRadio(false);
        vAdd.adean.setSelection(((ListSpinnerAdapter) vAdd.adean.getAdapter()).getSelection());

        vAdd.pause.setAdapter(pausen);
        vAdd.pause.setSelection(pausen.getSelection());

        vAdd.pause.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                pausen.setSelection((int) id);

                ListClass item = pausen.getItem((int) id);

                if (item == null)
                    return;

                if (item.getId() > 0 && !getKategorie().equals("B"))
                    vAdd.pausein_box.setVisibility(View.VISIBLE);
                else {
                    vAdd.pausein_box.setVisibility(View.GONE);
                    vAdd.pausein.setText("");
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        vAdd.pausein.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 1) {
                    vAdd.rilstatus.setVisibility(View.GONE);
                    vAdd.rilprogress.setVisibility(View.VISIBLE);
                    if (ril100Task != null)
                        ril100Task.cancel(true);
                    ril100Task = new GetRil100().execute();
                } else {
                    if (ril100Task != null)
                        ril100Task.cancel(true);

                    vAdd.rilstatus.setImageResource(R.drawable.ic_action_cancel);
                    vAdd.rilstatus.setVisibility(View.VISIBLE);
                    vAdd.rilprogress.setVisibility(View.GONE);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }


        });

        // Abweichungen erkennen


        vAdd.adb.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    vAdd.dbr.setVisibility(View.VISIBLE);
                    vAdd.deladb.setVisibility(View.VISIBLE);
                    vAdd.adban.setVisibility(View.VISIBLE);
                    vAdd.dbr.setVisibility(View.VISIBLE);
                } else {
                    vAdd.dbr.setVisibility(View.GONE);
                    vAdd.deladb.setVisibility(View.GONE);
                    vAdd.adban.setVisibility(View.GONE);
                    vAdd.dbr.setVisibility(View.GONE);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {


            }

        });

        vAdd.ade.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    vAdd.der.setVisibility(View.VISIBLE);
                    vAdd.delade.setVisibility(View.VISIBLE);
                    vAdd.adean.setVisibility(View.VISIBLE);
                    vAdd.der.setVisibility(View.VISIBLE);
                } else {
                    vAdd.der.setVisibility(View.GONE);
                    vAdd.delade.setVisibility(View.GONE);
                    vAdd.adean.setVisibility(View.GONE);
                    vAdd.der.setVisibility(View.GONE);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        vAdd.oaz.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
                    vAdd.deloaz.setVisibility(View.VISIBLE);
                else
                    vAdd.deloaz.setVisibility(View.GONE);
            }
        });

        vAdd.apause.setAdapter(pausenabw);
        vAdd.apause.setSelection(pausenabw.getSelection());

        vAdd.apause.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                pausenabw.setSelection((int) id);

                ListClass item = pausenabw.getItem((int) id);

                if (item == null)
                    return;

                if (item.getId() > -1) {
                    vAdd.apauser.setVisibility(View.VISIBLE);
                    vAdd.delapause.setVisibility(View.VISIBLE);
                } else {
                    vAdd.apauser.setVisibility(View.GONE);
                    vAdd.delapause.setVisibility(View.GONE);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        vAdd.abcbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    vAdd.schicht.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                else
                    vAdd.schicht.setInputType(InputType.TYPE_CLASS_PHONE);
            }
        });

        if (getIntent().hasExtra("item")) {

            vAdd.zeitraum_box.setVisibility(View.GONE);

            VerwendungClass edit = getIntent().getParcelableExtra("item");

            sid = String.valueOf(edit.getId());

            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle("Verwendung bearbeiten");

            String subtitle;
            if (edit.getFpla().equals("0"))
                subtitle = edit.getBezeichner();
            else
                subtitle = edit.getBezeichner() + "\nFpl/N/" + edit.getFpla();

            getSupportActionBar().setSubtitle(subtitle + " (" + edit.getDatumFormatted("E, dd.MM.yyyy") + ")");

            setKategorie(edit.getKat().substring(0, 1));

            vAdd.datumStart.setText(edit.getDatumFormatted("E, dd.MM.yyyy"));
            vAdd.notiz.setText(edit.getNotiz());

            switch (getKategorie()) {
                case "S":
                    vAdd.schicht.setText(edit.getBezeichner());
                    if (!edit.getFpla().equals("0"))
                        vAdd.fpla.setText(edit.getFpla());
                    setSelectedId(vAdd.est, edit.getEstId());
                    setSelectedId(vAdd.funktion, edit.getFunktionId());
                    vAdd.db.setText(edit.getDb());
                    vAdd.de.setText(edit.getDe());
                    setSelectedId(vAdd.pause, edit.getPauseInt());
                    if (edit.getPauseInt() > 0)
                        vAdd.pausein.setText(edit.getPauseRil());
                    vAdd.aufdb.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(edit.getAufdb()), edit.getAufdb() - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(edit.getAufdb()))));
                    vAdd.aufde.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(edit.getAufde()), edit.getAufde() - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(edit.getAufde()))));
                    vAdd.aufdz.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.SECONDS.toHours(edit.getAufdz()), TimeUnit.SECONDS.toMinutes(edit.getAufdz()) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(edit.getAufdz()))));
                    vAdd.baureihen.setText(edit.getBaureihen());
                    if (!edit.getAdb().equals("null")) {
                        vAdd.adb.setText(edit.getAdb());
                        vAdd.dbr.setText(edit.getDbr());
                        setSelectedId(vAdd.adban, (int) (edit.getAdban() * 100));
                    }
                    if (!edit.getAde().equals("null")) {
                        vAdd.ade.setText(edit.getAde());
                        vAdd.der.setText(edit.getDer());
                        setSelectedId(vAdd.adean, (int) (edit.getAdean() * 100));
                    }
                    if (!edit.getApause().equals("null")) {
                        setSelectedId(vAdd.apause, edit.getApause());
                        vAdd.apauser.setText(edit.getApauser());
                    }
                    vAdd.oaz.setText(edit.getAz().substring(0, 5));
                    break;
                case "T":
                    vAdd.schicht.setText(edit.getBezeichner());
                    if (!edit.getFpla().equals("0"))
                        vAdd.fpla.setText(edit.getFpla());
                    setSelectedId(vAdd.est, edit.getEstId());
                    setSelectedId(vAdd.funktion, edit.getFunktionId());
                    vAdd.db.setText(edit.getDb());
                    vAdd.de.setText(edit.getDe());
                    setSelectedId(vAdd.pause, edit.getPauseInt());
                    if (edit.getPauseInt() > 0)
                        vAdd.pausein.setText(edit.getPauseRil());
                    vAdd.oaz.setText(edit.getAz().substring(0, 5));
                    break;
                case "D":
                    setSelectedIdent(vAdd.dispotyp, edit.getKat());
                    break;
                case "U":
                    setSelectedIdent(vAdd.urlaubtyp, edit.getKat());
                    vAdd.oaz.setText(edit.getAz().substring(0, 5));
                    if (edit.getKat().equals("UN"))
                        vAdd.ubeschreibung.setText(edit.getBezeichner());
                    break;
                case "B":
                    setSelectedId(vAdd.best, edit.getEstId());
                    vAdd.db.setText(edit.getDb());
                    vAdd.de.setText(edit.getDe());
                    setSelectedId(vAdd.pause, edit.getPauseInt());
                    vAdd.bbeschreibung.setText(edit.getBezeichner());
                    if (edit.getOaz() != 0) {
                        vAdd.oaz.setText(String.format("%02d:%02d", TimeUnit.SECONDS.toHours(edit.getOaz()), TimeUnit.SECONDS.toMinutes(edit.getOaz()) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(edit.getOaz()))));
                    }
                    break;
                case "K":
                    if (edit.getOaz() != 0) {
                        vAdd.oaz.setText(String.format("%02d:%02d", TimeUnit.SECONDS.toHours(edit.getOaz()), TimeUnit.SECONDS.toMinutes(edit.getOaz()) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(edit.getOaz()))));
                    }
                    break;
            }
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public String getKategorie() {
        return ((ListClass) vAdd.kategorie.getSelectedItem()).getIdent();
    }

    public void setKategorie(String kategorie) {
        ((ListSpinnerAdapter) vAdd.kategorie.getAdapter()).setSelectionIdent(kategorie);
        vAdd.kategorie.setSelection(((ListSpinnerAdapter) vAdd.kategorie.getAdapter()).getSelection());
    }

    public int getSelectedId(Spinner spinner) {
        return ((ListClass) spinner.getSelectedItem()).getId();
    }

    public String getSelectedIdString(Spinner spinner) {
        return String.valueOf(((ListClass) spinner.getSelectedItem()).getId());
    }

    public void setSelectedId(Spinner spinner, String id) {
        ((ListSpinnerAdapter) spinner.getAdapter()).setSelectionID(Integer.parseInt(id));
        spinner.setSelection(((ListSpinnerAdapter) spinner.getAdapter()).getSelection());
    }

    public void setSelectedId(Spinner spinner, int id) {
        ((ListSpinnerAdapter) spinner.getAdapter()).setSelectionID(id);
        spinner.setSelection(((ListSpinnerAdapter) spinner.getAdapter()).getSelection());
    }

    public void setSelectedIdent(Spinner spinner, String ident) {
        ((ListSpinnerAdapter) spinner.getAdapter()).setSelectionIdent(ident);
        spinner.setSelection(((ListSpinnerAdapter) spinner.getAdapter()).getSelection());
    }

    public String getSelectedIdent(Spinner spinner) {
        return ((ListClass) spinner.getSelectedItem()).getIdent();
    }

    public void BuildForm(String kategorie) {

        if (getIntent().hasExtra("item")) {
            vAdd.zeitraum_box.setVisibility(View.GONE);
            vAdd.zeitraum.setChecked(false);
        } else {
            vAdd.zeitraum_box.setVisibility(View.VISIBLE);
            vAdd.zeitraum.setChecked(false);
        }

        // Hide all
        for (int i = 0; i < vAdd.scroll_linear_box.getChildCount(); i++) {
            View v = vAdd.scroll_linear_box.getChildAt(i);
            if (v.getId() != R.id.verwendungadd_datum_box &&
                    v.getId() != R.id.verwendungadd_kategorie_box &&
                    v.getId() != R.id.verwendungadd_sonstiges_box && (v.getId() != R.id.verwendungadd_schicht_box || !kategorie.equals("S"))
                    ) {
                v.setVisibility(View.GONE);
            }
        }

        vAdd.baureihen_box.setVisibility(View.GONE);

        switch (kategorie) {
            case "S":
                vAdd.schicht_box.setVisibility(View.VISIBLE);
                vAdd.az_box.setVisibility(View.VISIBLE);
                if (((ListClass) vAdd.funktion.getSelectedItem()).getId() == 1)
                    vAdd.auf_box.setVisibility(View.VISIBLE);
                vAdd.sonstiges_box.setVisibility(View.VISIBLE);
                vAdd.abweichungen_box.setVisibility(View.VISIBLE);
                vAdd.zeitraum_box.setVisibility(View.GONE);
                vAdd.zeitraum.setChecked(false);
                if (getSelectedId(vAdd.pause) > 0)
                    vAdd.pausein_box.setVisibility(View.VISIBLE);
                else
                    vAdd.pausein_box.setVisibility(View.GONE);
                if (getSelectedId(vAdd.funktion) == 1)
                    vAdd.baureihen_box.setVisibility(View.VISIBLE);
                else
                    vAdd.baureihen_box.setVisibility(View.GONE);
                break;
            case "T":
                vAdd.schicht_box.setVisibility(View.VISIBLE);
                vAdd.az_box.setVisibility(View.VISIBLE);
                vAdd.zeitraum_box.setVisibility(View.GONE);
                vAdd.zeitraum.setChecked(false);
                vAdd.sonstiges_box.setVisibility(View.VISIBLE);
                vAdd.baureihen_box.setVisibility(View.GONE);
                if (getSelectedId(vAdd.pause) > 0)
                    vAdd.pausein_box.setVisibility(View.VISIBLE);
                else
                    vAdd.pausein_box.setVisibility(View.GONE);
                break;
            case "D":
                vAdd.dispo_box.setVisibility(View.VISIBLE);
                break;
            case "U":
                vAdd.urlaub_box.setVisibility(View.VISIBLE);
                break;
            case "K":
                vAdd.oaz_box.setVisibility(View.VISIBLE);
                break;
            case "B":
                vAdd.buero_box.setVisibility(View.VISIBLE);
                vAdd.az_box.setVisibility(View.VISIBLE);
                vAdd.oaz_box.setVisibility(View.VISIBLE);
                vAdd.zeitraum_box.setVisibility(View.GONE);
                vAdd.zeitraum.setChecked(false);
                vAdd.pausein_box.setVisibility(View.GONE);
                break;


        }


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("saveMultiple", saveMultiple);
        outState.putBoolean("increaseDate", increaseDate);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        if (!getIntent().hasExtra("item")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.verwendung_add_menu, menu);
            menu.findItem(R.id.action_save_verwendung_date_plus).setChecked(increaseDate);
            menu.findItem(R.id.action_save_verwendung_multiple).setChecked(saveMultiple);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save_verwendung_multiple:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                saveMultiple = item.isChecked();
                return true;
            case R.id.action_save_verwendung_date_plus:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                increaseDate = item.isChecked();
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
            case R.id.verwendungadd_back:
                finish();
                break;
            case R.id.verwendungadd_save:
                VerwendungAddOnClick();
                break;
            case R.id.verwendungadd_search:
                SucheOnClick();
                break;
            case R.id.verwendungadd_db:
                ZeitOnClick(vAdd.db);
                break;
            case R.id.verwendungadd_de:
                ZeitOnClick(vAdd.de);
                break;
            case R.id.verwendungadd_adb:
                ZeitOnClick(vAdd.adb);
                break;
            case R.id.verwendungadd_ade:
                ZeitOnClick(vAdd.ade);
                break;
            case R.id.verwendungadd_deladb:
                deladb();
                break;
            case R.id.verwendungadd_delade:
                delade();
                break;
            case R.id.verwendungadd_delapause:
                delapause();
                break;
            case R.id.verwendungadd_deloaz:
                deloaz();
                break;
            case R.id.verwendungadd_datumStart:
                DatumOnClick(vAdd.datumStart);
                break;
            case R.id.verwendungadd_datumEnde:
                DatumOnClick(vAdd.datumEnde);
                break;
            case R.id.verwendungadd_aufdz:
                ZeitOnClick(vAdd.aufdz);
                break;
            case R.id.verwendungadd_aufdb:
                ZeitOnClick(vAdd.aufdb);
                break;
            case R.id.verwendungadd_aufde:
                ZeitOnClick(vAdd.aufde);
                break;
            case R.id.verwendungadd_oaz:
                ZeitOnClick(vAdd.oaz);
                break;

        }


    }

    public void deladb() {
        vAdd.adb.setText("");
    }

    public void delade() {
        vAdd.ade.setText("");
    }

    public void deloaz() {
        vAdd.oaz.setText("");
    }

    public void delapause() {
        ((ListSpinnerAdapter) vAdd.apause.getAdapter()).setSelectionID(-1);
        vAdd.apause.setSelection(((ListSpinnerAdapter) vAdd.apause.getAdapter()).getSelection());
    }


    private class GetRil100 extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... params) {

            return OSES.getJSON("https://oses.mobi/api.php?request=suche_bst&session=" + OSES.getSession().getIdentifier() + "&ril100=" + vAdd.pausein.getText(), 30000);

        }

        protected void onPostExecute(String response) {

            if (vAdd == null || vAdd.rilprogress == null || vAdd.rilstatus == null || response == null)
                return;

            vAdd.rilprogress.setVisibility(View.GONE);

            if (response.length() > 0) {
                vAdd.rilstatus.setImageResource(R.drawable.ic_action_accept);
                vAdd.rilstatus.setVisibility(View.VISIBLE);
            } else {
                vAdd.rilstatus.setImageResource(R.drawable.ic_action_cancel);
                vAdd.rilstatus.setVisibility(View.VISIBLE);
            }
        }

    }

    private void showWaitDialog() {

        ProgressDialogFragment loginWaitDialog = ProgressDialogFragment.newInstance("Kommunikation", "Verwendung wird gespeichert...", ProgressDialog.STYLE_SPINNER);
        loginWaitDialog.show(getSupportFragmentManager(), "saveVerwendungDialog");

    }

    private void hideWaitDialog() {

        Fragment f = getSupportFragmentManager().findFragmentByTag("saveVerwendungDialog");

        if (f != null)
            ((ProgressDialogFragment) f).dismiss();


    }

    public void VerwendungAddOnClick() {

        showWaitDialog();

        // Datum parsen
        String datumStart = "";
        String datumEnde = "";

        String[] split = vAdd.datumStart.getText().toString().substring(5).split("\\.");

        if (split.length == 3) {
            datumStart = split[2] + "-" + split[1] + "-" + split[0];
        }

        split = vAdd.datumEnde.getText().toString().substring(5).split("\\.");

        if (split.length == 3) {
            datumEnde = split[2] + "-" + split[1] + "-" + split[0];
        }

        // Hashmap aufbauen
        Map<String, String> postdata = new HashMap<>();

        // Session
        postdata.put("session", OSES.getSession().getIdentifier());

        // Grunddaten
        postdata.put("sid", sid);
        postdata.put("kategorie", getKategorie());
        postdata.put("schicht", vAdd.schicht.getText().toString());
        postdata.put("fpla", vAdd.fpla.getText().toString());
        postdata.put("datumStart", datumStart);
        postdata.put("datumEnde", datumEnde);
        postdata.put("zeitraum", String.valueOf(vAdd.zeitraum.isChecked()));
        postdata.put("est", String.valueOf(((ListClass) vAdd.est.getSelectedItem()).getId()));
        postdata.put("best", String.valueOf(((ListClass) vAdd.best.getSelectedItem()).getId()));
        postdata.put("funktion", String.valueOf(((ListClass) vAdd.funktion.getSelectedItem()).getId()));
        postdata.put("db", vAdd.db.getText().toString());
        postdata.put("de", vAdd.de.getText().toString());
        postdata.put("pause", String.valueOf(((ListClass) vAdd.pause.getSelectedItem()).getId()));
        postdata.put("pause_ort", vAdd.pausein.getText().toString());
        postdata.put("baureihen", vAdd.baureihen.getText().toString());

        // Abweichungen
        postdata.put("adb", vAdd.adb.getText().toString());
        postdata.put("ade", vAdd.ade.getText().toString());
        postdata.put("adb_an", getSelectedIdString(vAdd.adban));
        postdata.put("ade_an", getSelectedIdString(vAdd.adean));
        postdata.put("dbr", vAdd.dbr.getText().toString());
        postdata.put("der", vAdd.der.getText().toString());
        postdata.put("apause", String.valueOf(((ListClass) vAdd.apause.getSelectedItem()).getId()));
        postdata.put("apauser", vAdd.apauser.getText().toString());

        // Aufenthalt
        postdata.put("aufdb", vAdd.aufdb.getText().toString());
        postdata.put("aufde", vAdd.aufde.getText().toString());
        postdata.put("aufdz", vAdd.aufdz.getText().toString());

        //Anreise -> TODO
        //postdata.put("jsonanreise", jsonanreise);
        //postdata.put("jsonabreise", jsonabreise);

        //Typen
        postdata.put("dispotyp", getSelectedIdent(vAdd.dispotyp));
        postdata.put("urlaubtyp", getSelectedIdent(vAdd.urlaubtyp));

        //Beschreibung
        postdata.put("ubeschreibung", vAdd.ubeschreibung.getText().toString());
        postdata.put("bbeschreibung", vAdd.bbeschreibung.getText().toString());

        //OAZ
        postdata.put("o_az", vAdd.oaz.getText().toString());


        // Sonstiges
        postdata.put("notiz", vAdd.notiz.getText().toString());

        OSESRequest request = new OSESRequest(this);

        request.setUrl("https://oses.mobi/api.php?request=verwendung&command=addajax");
        request.setTimeout(30000);
        request.setParams(postdata);

        request.setOnRequestFinishedListener(new OSESRequest.OnRequestFinishedListener() {
            @Override
            public void onRequestFinished(String response) {

                String Status = "900";
                String StatusBody = "Verarbeitung fehlgeschlagen";

                JSONObject json;

                try {

                    json = new JSONObject(response);

                    Status = json.getString("Status");
                    StatusBody = json.getString("StatusBody");

                } catch (Exception e) {
                    e.getStackTrace();
                }

                Bundle extra = new Bundle();
                extra.putString("status", Status);
                mFirebaseAnalytics.logEvent("OSES_verwendung_save", extra);

                if (Status.equals("200")) {

                    hideWaitDialog();

                    setResult(200);
                    if (saveMultiple) {
                        if (increaseDate) {

                            Integer year;
                            Integer month;
                            Integer day;

                            Calendar c = Calendar.getInstance();

                            if (vAdd.datumStart.getText().length() > 0) {
                                String[] split = vAdd.datumStart.getText().toString().substring(5).split("\\.");
                                day = Integer.parseInt(split[0]);
                                month = Integer.parseInt(split[1]) - 1;
                                year = Integer.parseInt(split[2]);
                            } else {
                                day = c.get(Calendar.DAY_OF_MONTH);
                                month = c.get(Calendar.MONTH);
                                year = c.get(Calendar.YEAR);
                            }

                            c.set(year, month, day);
                            c.add(Calendar.DATE, 1);

                            day = c.get(Calendar.DAY_OF_MONTH);
                            month = c.get(Calendar.MONTH);
                            year = c.get(Calendar.YEAR);

                            SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.GERMAN);

                            vAdd.datumStart.setText(dayFormat.format(c.getTime()) + ", " + pad(day) + "." + pad(month + 1) + "." + pad(year));


                        }
                    } else
                        finish();
                }

                if (Status.equals("201")) {
                    hideWaitDialog();
                    setResult(200);
                    finish();
                }


                Toast.makeText(VerwendungAddActivity.this, Status + ": " + StatusBody, Toast.LENGTH_LONG).show();
                hideWaitDialog();

            }

            @Override
            public void onRequestException(Exception e) {

                Toast.makeText(VerwendungAddActivity.this, "Es ist ein interner Programmfehler aufgetreten: " + e.getMessage(), Toast.LENGTH_LONG).show();
                hideWaitDialog();

            }

            @Override
            public void onRequestUnknown(int status) {

                Toast.makeText(VerwendungAddActivity.this, "Es ist ein Serverfehler aufgetreten: Der Servermeldet einen unbekannten Antwortcode (" + status + ")", Toast.LENGTH_LONG).show();
                hideWaitDialog();

            }

            @Override
            public void onIsNotConnected() {

                Toast.makeText(getApplicationContext(), "Keine Kommunikation mit dem Server möglich. Bitte stelle eine Datenverbindung her!", Toast.LENGTH_LONG).show();
                hideWaitDialog();

            }
        });

        request.execute();

    }


    public void SucheOnClick() {

        vAdd.searchprogress.setVisibility(View.VISIBLE);
        vAdd.search.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vAdd.search.getWindowToken(), 0);
        vAdd.search.setEnabled(false);

        // RESET Abweichung
        deloaz();
        delapause();
        deladb();
        delade();

        String Tag = "";
        String Monat = "";
        String Jahr = "";

        String DatumText = vAdd.datumStart.getText().toString().substring(5);

        String[] split = DatumText.split("\\.");

        if (split.length == 3) {
            Tag = split[0];
            Monat = split[1];
            Jahr = split[2];
        }

        OSESRequest request = new OSESRequest(this);

        request.setUrl("https://oses.mobi/api.php?request=suche_schicht&session=" + OSES.getSession().getIdentifier() + "&schicht=" + vAdd.schicht.getText().toString().replace(" ", "+") + "&datum=" + Jahr + "-" + Monat + "-" + Tag);
        request.setTimeout(10000);

        request.setOnRequestFinishedListener(new OSESRequest.OnRequestFinishedListener() {
            @Override
            public void onRequestFinished(String response) {

                try {
                    final JSONArray schichten = new JSONArray(response);

                    if (schichten.length() == 0) {
                        vAdd.schicht.setError("Leider wurde keine Referenzschicht gefunden, du kannst das Formular jedoch manuell ausfüllen!");
                        Handler mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                vAdd.schicht.setError(null);
                            }
                        }, 5000);

                    }

                    if (schichten.length() == 1) {

                        JSONObject schicht = schichten.getJSONObject(0);

                        vAdd.schicht.setText(schicht.getString("schicht"));

                        if (!schicht.getString("fpla").equals("0"))
                            vAdd.fpla.setText(schicht.getString("fpla"));
                        else
                            vAdd.fpla.setText("");

                        vAdd.db.setText(schicht.getString("db").substring(0, 5));
                        vAdd.de.setText(schicht.getString("de").substring(0, 5));
                        vAdd.baureihen.setText(schicht.getString("baureihen"));

//                        if (schicht.getString("ril100").equals("HG")) {
//							apause.setSelection(1);
//							apauser.setText("Keine Abnahme Pausenraum durch Betriebsrat");
//							Toast.makeText(getApplicationContext(), "Achtung: Kein Pausenraum in Göttingen! Abweichung automatisch erstellt!" , Toast.LENGTH_LONG).show();
//						}

                        ((ListSpinnerAdapter) vAdd.pause.getAdapter()).setSelectionID(schicht.getInt("pause"));
                        vAdd.pause.setSelection(((ListSpinnerAdapter) vAdd.pause.getAdapter()).getSelection());

                        if (schicht.getInt("pause") > 0)
                            vAdd.pausein.setText(schicht.getString("ril100"));

                        ((ListSpinnerAdapter) vAdd.est.getAdapter()).setSelectionID(schicht.getInt("est"));
                        vAdd.est.setSelection(((ListSpinnerAdapter) vAdd.est.getAdapter()).getSelection());

                        ((ListSpinnerAdapter) vAdd.funktion.getAdapter()).setSelectionID(schicht.getInt("funktioni"));
                        vAdd.funktion.setSelection(((ListSpinnerAdapter) vAdd.funktion.getAdapter()).getSelection());

                        vAdd.aufdb.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(schicht.getInt("aufdb")), schicht.getInt("aufdb") - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(schicht.getInt("aufdb")))));
                        vAdd.aufde.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(schicht.getInt("aufde")), schicht.getInt("aufde") - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(schicht.getInt("aufde")))));

                        if (schicht.getInt("est") == OSES.getSession().getEst()) {
                            vAdd.aufdz.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(schicht.getInt("aufdz")), schicht.getInt("aufdz") - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(schicht.getInt("aufdz")))));
                        } else {
                            vAdd.aufdz.setText("00:00");
                        }

                    }

                    if (schichten.length() > 1) {


                        String[] split = new String[schichten.length()];

                        for (int i = 0; i < schichten.length(); i++) {

                            JSONObject schicht = schichten.getJSONObject(i);

                            String schichtname;

                            if (schicht.getString("fpla").equals("0"))
                                schichtname = schicht.getString("schicht");
                            else
                                schichtname = schicht.getString("schicht") + " Fpl/N/" + schicht.getString("fpla");

                            String kommentar = "";

                            if (!schicht.isNull("kommentar")) {
                                kommentar = schicht.getString("kommentar") + "\r\n";
                            }

                            split[i] = "\r\n" + schicht.getString("funktion") + ": " + schichtname + "\r\n" + schicht.getString("ort") + "\r\n" + schicht.getString("db").substring(0, 5) + " - " + schicht.getString("de").substring(0, 5) + "  |  Pause: " + schicht.getString("pause") + " Min" + "\r\n" + kommentar;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(VerwendungAddActivity.this);
                        builder.setTitle("Bitte wähle eine Schicht:");
                        builder.setSingleChoiceItems(split, -1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                try {

                                    JSONObject schicht = schichten.getJSONObject(item);

                                    vAdd.schicht.setText(schicht.getString("schicht"));

                                    if (!schicht.getString("fpla").equals("0"))
                                        vAdd.fpla.setText(schicht.getString("fpla"));
                                    else
                                        vAdd.fpla.setText("");

                                    vAdd.db.setText(schicht.getString("db").substring(0, 5));
                                    vAdd.de.setText(schicht.getString("de").substring(0, 5));
                                    vAdd.baureihen.setText(schicht.getString("baureihen"));


//                        if (schicht.getString("ril100").equals("HG")) {
//							apause.setSelection(1);
//							apauser.setText("Keine Abnahme Pausenraum durch Betriebsrat");
//							Toast.makeText(getApplicationContext(), "Achtung: Kein Pausenraum in Göttingen! Abweichung automatisch erstellt!" , Toast.LENGTH_LONG).show();
//						}

                                    ((ListSpinnerAdapter) vAdd.pause.getAdapter()).setSelectionID(schicht.getInt("pause"));
                                    vAdd.pause.setSelection(((ListSpinnerAdapter) vAdd.pause.getAdapter()).getSelection());

                                    if (schicht.getInt("pause") > 0)
                                        vAdd.pausein.setText(schicht.getString("ril100"));

                                    ((ListSpinnerAdapter) vAdd.est.getAdapter()).setSelectionID(schicht.getInt("est"));
                                    vAdd.est.setSelection(((ListSpinnerAdapter) vAdd.est.getAdapter()).getSelection());

                                    ((ListSpinnerAdapter) vAdd.funktion.getAdapter()).setSelectionID(schicht.getInt("funktioni"));
                                    vAdd.funktion.setSelection(((ListSpinnerAdapter) vAdd.funktion.getAdapter()).getSelection());

                                    vAdd.aufdb.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(schicht.getInt("aufdb")), schicht.getInt("aufdb") - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(schicht.getInt("aufdb")))));
                                    vAdd.aufde.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(schicht.getInt("aufde")), schicht.getInt("aufde") - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(schicht.getInt("aufde")))));

                                    if (schicht.getInt("est") == OSES.getSession().getEst()) {
                                        vAdd.aufdz.setText(String.format(Locale.GERMAN, "%02d:%02d", TimeUnit.MINUTES.toHours(schicht.getInt("aufdz")), schicht.getInt("aufdz") - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(schicht.getInt("aufdz")))));
                                    } else {
                                        vAdd.aufdz.setText("00:00");
                                    }

                                } catch (Exception e) {
                                    dialog.dismiss();

                                }

                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.getListView().setDivider(new ColorDrawable(Color.parseColor("#cacaca")));
                        alert.getListView().setDividerHeight(1);
                        alert.show();


                    }


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Es ist ein Fehler beim Suchen der Schicht aufgetreten: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                vAdd.searchprogress.setVisibility(View.GONE);

                vAdd.search.setVisibility(View.VISIBLE);
                vAdd.search.setEnabled(true);

            }

            @Override
            public void onRequestException(Exception e) {
                Toast.makeText(getApplicationContext(), "Es ist ein Fehler beim Suchen der Schicht aufgetreten: " + e.getMessage(), Toast.LENGTH_LONG).show();
                vAdd.searchprogress.setVisibility(View.GONE);
                vAdd.search.setVisibility(View.VISIBLE);
                vAdd.search.setEnabled(true);
            }

            @Override
            public void onRequestUnknown(int status) {
                Toast.makeText(VerwendungAddActivity.this, "Es ist ein Serverfehler aufgetreten: Der Servermeldet einen unbekannten Antwortcode (" + status + ")", Toast.LENGTH_LONG).show();
                vAdd.searchprogress.setVisibility(View.GONE);
                vAdd.search.setVisibility(View.VISIBLE);
                vAdd.search.setEnabled(true);
            }

            @Override
            public void onIsNotConnected() {
                Toast.makeText(getApplicationContext(), "Keine Kommunikation mit dem Server möglich. Bitte stelle eine Datenverbindung her!", Toast.LENGTH_LONG).show();
                vAdd.searchprogress.setVisibility(View.GONE);
                vAdd.search.setVisibility(View.VISIBLE);
                vAdd.search.setEnabled(true);
            }
        });

        request.execute();

    }

    public void DatumCheck(TextView setter) {

        final Calendar cStart = Calendar.getInstance();
        final Calendar cEnde = Calendar.getInstance();
        final SimpleDateFormat dayFormat = new SimpleDateFormat("E, dd.MM.yyyy", Locale.GERMAN);

        try {
            cStart.setTime(dayFormat.parse(vAdd.datumStart.getText().toString()));
            cEnde.setTime(dayFormat.parse(vAdd.datumEnde.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (setter.getId()) {
            case R.id.verwendungadd_datumStart:
                if (cStart.getTime().after(cEnde.getTime())) {
                    vAdd.datumEnde.setText(dayFormat.format(cStart.getTime()));
                }
                break;
            case R.id.verwendungadd_datumEnde:
                if (cEnde.getTime().before(cStart.getTime())) {
                    vAdd.datumStart.setText(dayFormat.format(cEnde.getTime()));
                }
                break;
        }

    }

    public void DatumOnClick(final TextView datumSet) {

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

    public void ZeitOnClick(final TextView Zeit) {

        Integer hour;
        Integer minute;

        if (Zeit.getText().length() == 5) {

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

