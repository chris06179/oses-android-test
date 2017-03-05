package de.stm.oses.schichten;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.stm.oses.R;
import de.stm.oses.helper.ListAdapter;
import de.stm.oses.helper.ListClass;
import de.stm.oses.helper.ListClassDialogFragment;
import de.stm.oses.helper.OSESBase;
import de.stm.oses.helper.SwipeRefreshListFragment;

public class SchichtenFragment extends SwipeRefreshListFragment implements ActionMode.Callback {

    public String query;
    public long selectedSchicht = -1;
    public AsyncTask<String, Void, SchichtenAdapter> task;
    public ActionMode mActionMode;
    private int selectedEst = 0;
    private int selectedFunktion = 0;
    private int selectedGB = 0;

    private OSESBase OSES;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int MOVE_DURATION = 150;

    public SchichtenFragment() {
        // Required empty public constructor
    }

    public void DeleteSchicht(final long sid) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final SchichtenClass item = ((SchichtenAdapter) getListAdapter()).getItem((int) sid);

        if (item == null)
            return;

        String fpla = item.getFpla();

        if (fpla.equals("0"))
            fpla = "";
        else
            fpla = " Fpl/N/" + fpla;

        builder.setMessage("Schicht " + item.getSchicht() + fpla + " (" + item.getGv() + " - " + item.getGb() + ")  wirklich entfernen?")
                .setCancelable(false)
                .setTitle("Referenzschicht entfernen")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                        setRefreshing(true);
                        new Thread(new DeleteSchichtRun(item.getId(), (int) sid)).start();
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

    public class DeleteSchichtRun implements Runnable {
        private int vid;
        private int lid;

        DeleteSchichtRun(int vid, int lid) {
            this.lid = lid;
            this.vid = vid;
        }

        public void run() {

            String response = OSES.getJSON("https://oses.mobi/api.php?request=schichten_show&action=delete_json&id=" + vid + "&session=" + OSES.getSession().getIdentifier(), 30000);

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


    Handler DeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            final int lid = msg.arg2;
            int StatusCode = msg.arg1;

            Bundle extra = new Bundle();
            extra.putString("status", String.valueOf(StatusCode));
            mFirebaseAnalytics.logEvent("OSES_schicht_delete", extra);

            if (StatusCode == 200) {

                getListView().setEnabled(false);

                int firstViewItemIndex = getListView().getFirstVisiblePosition();
                int lastViewItemIndex = getListView().getLastVisiblePosition();
                int viewIndex = lid - firstViewItemIndex;

                if (lid < firstViewItemIndex || lid > lastViewItemIndex) {
                    ((SchichtenAdapter) getListAdapter()).remove(((SchichtenAdapter) getListAdapter()).getItem(lid));
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
                        ((SchichtenAdapter) getListAdapter()).remove(((SchichtenAdapter) getListAdapter()).getItem(lid));
                        getListView().setEnabled(true);
                        setRefreshing(false);
                    }
                });

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    ((SchichtenAdapter) getListAdapter()).remove(((SchichtenAdapter) getListAdapter()).getItem(lid));
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

                Toast.makeText(getActivity().getApplicationContext(), "Schicht konnte nicht entfernt werden!", Toast.LENGTH_SHORT).show();
            }

            if (StatusCode == 999) {

                Toast.makeText(getActivity().getApplicationContext(), "Schicht konnte nicht entfernt werden! Fehlerhafte Antwort vom Server!", Toast.LENGTH_SHORT).show();

            }

            setRefreshing(false);

        }
    };

    // Called when the action mode is created; startActionMode() was called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        getActivity().getMenuInflater().inflate(R.menu.schichten_action_menu, menu);
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
        SchichtenClass schicht = ((SchichtenAdapter) getListAdapter()).getItem((int) selectedSchicht);

        switch (item.getItemId()) {
            case R.id.action_delete_schicht:
                DeleteSchicht(selectedSchicht);
                return true;
            case R.id.action_edit_schicht:
                EditSchicht(schicht);
                return true;
            default:
                return false;
        }
    }

    // Called when the user exits the action mode
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        RemoveListSelection(true);
        selectedSchicht = -1;
        mActionMode = null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        OSES = new OSESBase(getActivity().getApplicationContext());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        selectedEst = OSES.getSession().getEst();
        selectedFunktion = Integer.parseInt(Integer.toString(OSES.getSession().getFunktion()).substring(0, 1));
        selectedGB = OSES.getSession().getGB();

        query = "https://oses.mobi/api.php?request=schichten_show&json=true&session=" + OSES.getSession().getIdentifier();
        task = new GetSchichten().execute(query);


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

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Referenzschichten");

        if (mActionMode != null) {
            if (selectedSchicht > -1) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);

                SchichtenClass item = ((SchichtenAdapter) getListAdapter()).getItem((int) selectedSchicht);

                String fpla = item.getFpla();

                if (fpla.equals("0"))
                    fpla = "";
                else
                    fpla = " Fpl/N/" + fpla;

                mActionMode.setTitle(item.getSchicht() + fpla);
                mActionMode.setSubtitle(item.getGv() + " - " + item.getGb());
            } else
                mActionMode.finish();
        }

        if (getListAdapter() != null && getListAdapter().isEmpty())
            setEmptyText("Keine Referenzschichten mit den angegebenen Filterkriterien gefunden!");
        if (getListAdapter() == null)
            setEmptyText("Keine Verbindung zu OSES!");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        task.cancel(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100) && resultCode == 200) {
            if (mActionMode != null)
                mActionMode.finish();
            setRefreshing(true);
            task = new GetSchichten().execute(query);
        }
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
                task = new GetSchichten().execute(query);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectedSchicht = id;

        if (mActionMode == null)
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);

        SchichtenClass item = ((SchichtenAdapter) getListAdapter()).getItem((int) id);

        String fpla = item.getFpla();

        if (fpla.equals("0"))
            fpla = "";
        else
            fpla = " Fpl/N/" + fpla;

        mActionMode.setTitle(item.getSchicht() + fpla);
        mActionMode.setSubtitle(item.getGv() + " - " + item.getGb());

        Menu menu = mActionMode.getMenu();

        RemoveListSelection(false);
        setListSelection(id);

        Vibrator vibrate = (Vibrator) getActivity().getSystemService(getActivity().getApplicationContext().VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        vibrate.vibrate(25);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.schichten_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_schicht:
                SchichtAddOnClick();
                return true;
            case R.id.action_filter_est:
                FilterEstOnClick();
                return true;
            case R.id.action_filter_funktion:
                FilterFunktionOnClick();
                return true;
            case R.id.action_filter_gb:
                FilterGBOnClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void SchichtAddOnClick() {

        Intent intent = new Intent(getActivity(), SchichtenAddActivity.class);
        startActivityForResult(intent, 100);


    }

    public void EditSchicht(SchichtenClass item) {

        Intent intent = new Intent(getActivity(), SchichtenAddActivity.class);

        intent.putExtra("item", item);
        startActivityForResult(intent, 100);


    }


    public void RemoveListSelection(boolean notifyChange) {

        for (int i = 0; i < ((SchichtenAdapter) getListAdapter()).getCount(); i++) {
            ((SchichtenAdapter) getListAdapter()).getItem(i).setSelected(false);
        }
        if (notifyChange)
            ((SchichtenAdapter) getListAdapter()).notifyDataSetChanged();

    }

    public void setListSelection(long id) {

        SchichtenClass item = (SchichtenClass) getListAdapter().getItem((int) id);
        item.setSelected(true);
        ((SchichtenAdapter) getListAdapter()).notifyDataSetChanged();

    }


    private ArrayList<SchichtenClass> parseJSON(String json) {
        ArrayList<SchichtenClass> items = new ArrayList<SchichtenClass>();

        try {
            JSONObject schichten_est = new JSONObject(json);

            JSONArray schichten = schichten_est.getJSONArray(schichten_est.names().getString(0));

            for (int i = 0; i < schichten.length(); i++) {

                SchichtenClass item = new SchichtenClass();

                JSONObject schicht = schichten.getJSONObject(i);

                item.setId(schicht.getInt("id"));

                item.setSchicht(schicht.getString("schicht"));
                item.setFpla(schicht.getString("fpla"));

                item.setDb(schicht.getString("db"));
                item.setDe(schicht.getString("de"));

                item.setPause(schicht.getString("pause"));
                item.setPauseOrt(schicht.getString("pause_ort"));

                item.setGb(schicht.getString("gb"));
                item.setGv(schicht.getString("gv"));

                item.setAz(schicht.getString("az"));

                item.setEst(schicht.getString("est"));
                item.setEstid(schicht.getInt("estid"));

                item.setFunktion(schicht.getString("funktion"));
                item.setFunktionid(schicht.getInt("funktionid"));

                item.setGbid(schicht.getInt("gbid"));

                item.setAufdb(schicht.getInt("aufdb"));
                item.setAufde(schicht.getInt("aufde"));

                item.setBaureihen(schicht.getString("baureihen"));

                if (schicht.isNull("kommentar"))
                    item.setKommentar(null);
                else
                    item.setKommentar(schicht.optString("kommentar", null));


                items.add(item);

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return items;
    }


    private class GetSchichten extends AsyncTask<String, Void, SchichtenAdapter> {

        protected SchichtenAdapter doInBackground(String... params) {

            String response = OSES.getJSON(params[0], 60000);

            if (getActivity() != null && response != null)
                return new SchichtenAdapter(getActivity(), parseJSON(response));
            else
                return null;

        }

        protected void onPostExecute(SchichtenAdapter adapter) {

            try {
                if (getActivity() == null || getListView() == null)
                    return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }


            if (adapter != null && adapter.isEmpty())
                setEmptyText("Keine Referenzschichten mit den angegebenen Filterkriterien gefunden!");
            if (adapter == null)
                setEmptyText("Keine Verbindung zu OSES!");

            if (selectedGB == 0 || selectedFunktion == 0 || selectedEst == 0) {
                setEmptyText("Benutzerprofil unvollständig! Das automatische Festlegen der Filterkriterien ist fehlgeschlagen. Bitte lege diese zum Fortfahren manuell fest!");
            }
            int index = getListView().getFirstVisiblePosition();
            View v = getListView().getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();

            setListAdapter(adapter);

            if (index != 0)
                getListView().setSelectionFromTop(index, top - getListView().getDividerHeight() - 2);


            setListShown(true);
            setRefreshing(false);

            if (mActionMode != null) {
                mActionMode.finish();
            }


        }

    }

    public void FilterEstOnClick() {

        final ListAdapter ests;

        try {
            ests = OSES.getEstAdapter(selectedEst);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "Fehler: Liste der Einsatzstellen konnte nicht abgerufen werden!", Toast.LENGTH_LONG).show();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Einsatzstelle auswählen");
        builder.setAdapter(ests, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ListClass item = ests.getItem(i);
                setListShown(false);
                selectedEst = item.getId();
                query = "https://oses.mobi/api.php?request=schichten_show&json=true&session=" + OSES.getSession().getIdentifier() + "&filter=1&filterFunktion=" + selectedFunktion + "&filterEst=" + selectedEst + "&filterGB=" + selectedGB;
                new GetSchichten().execute(query);


            }
        });


        AlertDialog alert = builder.create();

        alert.show();

    }

    public void FilterFunktionOnClick() {


        final ListAdapter funktionen;

        try {
            funktionen = OSES.getFunktionAdapter(selectedFunktion);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "Fehler: Liste der Funktionen konnte nicht abgerufen werden!", Toast.LENGTH_LONG).show();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Funktion auswählen");
        builder.setAdapter(funktionen, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListClass item = funktionen.getItem(i);
                setListShown(false);
                selectedFunktion = item.getId();
                query = "https://oses.mobi/api.php?request=schichten_show&json=true&session=" + OSES.getSession().getIdentifier() + "&filter=1&filterFunktion=" + selectedFunktion + "&filterEst=" + selectedEst + "&filterGB=" + selectedGB;
                new GetSchichten().execute(query);
                dialogInterface.dismiss();

            }
        });


        AlertDialog alert = builder.create();

        alert.show();
    }

    public void FilterGBOnClick() {


        final ListAdapter GB;

        try {
            GB = OSES.getGBAdapter(selectedGB);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "Fehler: Liste der Geschäftsbereiche konnte nicht abgerufen werden!", Toast.LENGTH_LONG).show();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Geschäftsbereich auswählen");
        builder.setAdapter(GB, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListClass item = GB.getItem(i);
                setListShown(false);
                selectedGB = item.getId();
                query = "https://oses.mobi/api.php?request=schichten_show&json=true&session=" + OSES.getSession().getIdentifier() + "&filter=1&filterFunktion=" + selectedFunktion + "&filterEst=" + selectedEst + "&filterGB=" + selectedGB;
                new GetSchichten().execute(query);
                dialogInterface.dismiss();

            }
        });


        AlertDialog alert = builder.create();

        alert.show();

    }

} 