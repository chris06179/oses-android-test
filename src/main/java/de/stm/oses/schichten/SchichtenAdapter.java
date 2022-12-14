package de.stm.oses.schichten;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.stm.oses.R;

public class SchichtenAdapter extends ArrayAdapter<SchichtenClass> {

        private final Context context;
        private final ArrayList<SchichtenClass> SchichtenClassArrayList;

        public SchichtenAdapter(Context context, ArrayList<SchichtenClass> SchichtenClassArrayList) {
 
            super(context, R.layout.schicht_item, SchichtenClassArrayList);
 
            this.context = context;
            this.SchichtenClassArrayList = SchichtenClassArrayList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
 

 
            // 2. Get rowView from inflater
            
            SchichtenClass item = SchichtenClassArrayList.get(position);
 
            View rowView = convertView;

            if (rowView == null || (rowView.getTag() != null && rowView.getTag().equals("DELETED"))) {
                // 1. Create inflater
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.schicht_item, parent, false);
            }


                FrameLayout container = rowView.findViewById(R.id.schicht_container);

                if (item.isSelected())
                    container.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.list_selector));
                else
                    container.setForeground(null);

 
              // Init Views
                TextView schicht = rowView.findViewById(R.id.schicht_schicht);

                TextView von = rowView.findViewById(R.id.schicht_von);
                TextView bis = rowView.findViewById(R.id.schicht_bis);

                TextView pause = rowView.findViewById(R.id.schicht_pause);



                TextView est = rowView.findViewById(R.id.schicht_est);

                TextView az = rowView.findViewById(R.id.schicht_az);

                TextView funktion = rowView.findViewById(R.id.schicht_funktion);

                TextView gvgb = rowView.findViewById(R.id.schicht_gvgb);

                TextView kommentar = rowView.findViewById(R.id.schicht_kommentar);

                RelativeLayout kommentar_container = rowView.findViewById(R.id.schicht_kommentar_container);



                // Set Data
                schicht.setText(item.getSchicht());

                if (!item.getFpla().equals("0"))
                    schicht.setText(item.getSchicht()+"\nFpl/N/"+item.getFpla());

                von.setText(item.getDb());
                bis.setText(item.getDe());


                funktion.setText(item.getFunktion());

                pause.setText("RP: "+item.getPause()+ " ("+item.getPauseOrt()+")");
                est.setText(item.getEst());

                az.setText("AZ: "+item.getAz());

                gvgb.setText(item.getGv()+" - "+item.getGb());

                if (item.getKommentar() == null) {
                    kommentar.setText("");
                    kommentar_container.setVisibility(View.GONE);
                } else {
                    kommentar.setText(item.getKommentar());
                    kommentar_container.setVisibility(View.VISIBLE);
                }






            return rowView;
        }		
}