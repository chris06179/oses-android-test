package de.stm.oses.verwendung;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.stm.oses.R;
import de.stm.oses.arbeitsauftrag.ArbeitsauftragBuilder;


public class VerwendungAdapter extends ArrayAdapter<VerwendungClass> {

    private final Context context;
    private final ArrayList<VerwendungClass> VerwendungClassArrayList;
    private int selection = -1;

    public static final int TYPE_SUM = 0;
    public static final int TYPE_ITEM = 1;

    public VerwendungAdapter(Context context, ArrayList<VerwendungClass> VerwendungClassArrayList) {

        super(context, R.layout.verwendung_item2, VerwendungClassArrayList);

        this.context = context;
        this.VerwendungClassArrayList = VerwendungClassArrayList;
    }

    static class ItemHolder {
        FrameLayout container;
        RelativeLayout abweichung_container;
        RelativeLayout info_container;
        RelativeLayout notiz_container;
        View type_block;
        TextView bezeichner;
        TextView von;
        TextView bis;
        TextView pause;
        TextView datum;
        TextView est;
        TextView az;
        TextView mdifferenz;
        TextView funktion;
        TextView dbr;
        TextView der;
        TextView apauser;
        TextView notiz;
        TextView info;
        ImageView aa_icon;
        ProgressBar aa_extracting;
    }

    static class SumHolder {
        TextView sumTitel;
        TextView sumSchichten;
        TextView sumUrlaub;
        TextView sumSoll;
        TextView sumIst;
        TextView sumDifferenz;
    }

    public void setSelection(int position) {
        this.selection = position;
        notifyDataSetChanged();
    }

    public int getSelection() {
        return selection;
    }

    public VerwendungClass getSelectedItem() {
        if (selection == -1)
            return null;

        VerwendungClass item = VerwendungClassArrayList.get(selection);

        if (item != null)
            return item;

        return null;
    }

    public VerwendungClass getItemByID(int id) {
        for (VerwendungClass schicht: VerwendungClassArrayList) {
            if (schicht.getId() == id)
                return schicht;
        }

        return null;
    }

    public ArrayList<VerwendungClass> getArrayList() {
        return VerwendungClassArrayList;
    }

    public int getTodayPos() {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("Dyyyy", Locale.GERMAN); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        String currentDate = sdf.format(date);

        for (int i = 0; i < VerwendungClassArrayList.size(); i++)
            if (currentDate.equals(VerwendungClassArrayList.get(i).getDatumFormatted("Dyyyy")))
                return i;
        for (int i = 0; i < VerwendungClassArrayList.size(); i++)
            if (date.before(VerwendungClassArrayList.get(i).getDatumDate()))
                return i;
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (VerwendungClassArrayList.get(position).isVerwendungSummary())
            return TYPE_SUM;
        else
            return TYPE_ITEM;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        int type = getItemViewType(position);

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (type == TYPE_SUM)
                rowView = inflater.inflate(R.layout.verwendung_summary, parent, false);
            if (type == TYPE_ITEM)
                rowView = inflater.inflate(R.layout.verwendung_item2, parent, false);
        }

        VerwendungClass item = VerwendungClassArrayList.get(position);

        if (type == TYPE_SUM) {

            SumHolder sumHolder;

            if (rowView.getTag() != null)
                sumHolder = (SumHolder) rowView.getTag();
            else {
                sumHolder = new SumHolder();
                sumHolder.sumTitel = (TextView) rowView.findViewById(R.id.verwendung_sum_monat);
                sumHolder.sumSchichten = (TextView) rowView.findViewById(R.id.verwendung_sum_schichten);
                sumHolder.sumUrlaub = (TextView) rowView.findViewById(R.id.verwendung_sum_urlaub);
                sumHolder.sumSoll = (TextView) rowView.findViewById(R.id.verwendung_sum_soll);
                sumHolder.sumIst = (TextView) rowView.findViewById(R.id.verwendung_sum_ist);
                sumHolder.sumDifferenz = (TextView) rowView.findViewById(R.id.verwendung_sum_differenz);
                rowView.setTag(sumHolder);
            }

            sumHolder.sumTitel.setText(item.getLabel());
            sumHolder.sumSchichten.setText(String.valueOf(item.getSchichten()));
            sumHolder.sumUrlaub.setText(String.valueOf(item.getUrlaub()));
            sumHolder.sumSoll.setText(item.getMsoll());
            sumHolder.sumIst.setText(item.getAzg());
            sumHolder.sumDifferenz.setText(item.getMdifferenz());

        }

        if (type == TYPE_ITEM) {

            ItemHolder itemHolder;

            if (rowView.getTag() != null && rowView.getTag().equals("DELETED")) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                rowView = inflater.inflate(R.layout.verwendung_item2, parent, false);
            }

            if (rowView.getTag() != null)
                itemHolder = (ItemHolder) rowView.getTag();
            else {
                itemHolder = new ItemHolder();
                itemHolder.container = (FrameLayout) rowView.findViewById(R.id.verwendung_container);
                itemHolder.bezeichner = (TextView) rowView.findViewById(R.id.verwendung_bezeichner);
                itemHolder.von = (TextView) rowView.findViewById(R.id.verwendung_von);
                itemHolder.bis = (TextView) rowView.findViewById(R.id.verwendung_bis);
                itemHolder.pause = (TextView) rowView.findViewById(R.id.verwendung_pause);
                itemHolder.datum = (TextView) rowView.findViewById(R.id.verwendung_datum);
                itemHolder.est = (TextView) rowView.findViewById(R.id.verwendung_est);
                itemHolder.az = (TextView) rowView.findViewById(R.id.verwendung_az);
                itemHolder.mdifferenz = (TextView) rowView.findViewById(R.id.verwendung_mdifferenz);
                itemHolder.funktion = (TextView) rowView.findViewById(R.id.verwendung_funktion);
                itemHolder.dbr = (TextView) rowView.findViewById(R.id.verwendung_dbr);
                itemHolder.der = (TextView) rowView.findViewById(R.id.verwendung_der);
                itemHolder.apauser = (TextView) rowView.findViewById(R.id.verwendung_apauser);
                itemHolder.notiz = (TextView) rowView.findViewById(R.id.verwendung_notiz);
                itemHolder.info = (TextView) rowView.findViewById(R.id.verwendung_info);
                itemHolder.aa_icon = (ImageView) rowView.findViewById(R.id.verwendung_aa_icon);
                itemHolder.aa_extracting = rowView.findViewById(R.id.verwendung_aa_extracting);
                itemHolder.abweichung_container = (RelativeLayout) rowView.findViewById(R.id.verwendung_abweichung_container);
                itemHolder.info_container = (RelativeLayout) rowView.findViewById(R.id.verwendung_info_container);
                itemHolder.notiz_container = (RelativeLayout) rowView.findViewById(R.id.verwendung_notiz_container);
                itemHolder.type_block = rowView.findViewById(R.id.verwendung_type_block);
                rowView.setTag(itemHolder);
            }

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("Dyyyy", Locale.GERMAN); // the format of your date
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
            String currentDate = sdf.format(date);


            if (selection == position) {
                itemHolder.container.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.list_selector));
            } else if (currentDate.equals(item.getDatumFormatted("Dyyy"))) {
                itemHolder.container.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.today_selector));
            } else
                itemHolder.container.setForeground(null);

            // Set Data
            if (item.getFpla().equals("0"))
                itemHolder.bezeichner.setText(item.getBezeichner());
            else
                itemHolder.bezeichner.setText(item.getBezeichner() + "\nFpl/N/" + item.getFpla());

            if (item.getKat().equals("T"))
                itemHolder.bezeichner.setText(itemHolder.bezeichner.getText() + "\n(Streik)");

            itemHolder.von.setText(item.getDb());
            itemHolder.bis.setText(item.getDe());

            if (item.getNotiz() == null) {
                itemHolder.notiz.setText("");
                itemHolder.notiz_container.setVisibility(View.GONE);
            } else {
                itemHolder.notiz.setText(item.getNotiz());
                itemHolder.notiz_container.setVisibility(View.VISIBLE);
            }

            if (!item.getInfo().equals("-"))
                itemHolder.info.setText(item.getInfo());
            else
                itemHolder.info.setText("");

            switch (item.getArbeitsauftragType()) {
                case ArbeitsauftragBuilder.TYPE_NONE:
                    itemHolder.aa_icon.setVisibility(View.GONE);
                    itemHolder.aa_extracting.setVisibility(View.GONE);
                    break;
                case ArbeitsauftragBuilder.TYPE_CACHED:
                    itemHolder.aa_icon.setVisibility(View.VISIBLE);
                    itemHolder.aa_icon.setAlpha(1f);
                    itemHolder.aa_extracting.setVisibility(View.GONE);
                    break;
                case ArbeitsauftragBuilder.TYPE_DILOC:
                case ArbeitsauftragBuilder.TYPE_ONLINE:
                    itemHolder.aa_icon.setVisibility(View.VISIBLE);
                    itemHolder.aa_icon.setAlpha(0.5f);
                    itemHolder.aa_extracting.setVisibility(View.GONE);
                    break;
                case ArbeitsauftragBuilder.TYPE_EXTRACTING:
                    itemHolder.aa_icon.setVisibility(View.GONE);
                    itemHolder.aa_extracting.setVisibility(View.VISIBLE);
            }

            int countab = 0;

            if (item.getAdb().equals("null")) {
                itemHolder.von.setTextColor(Color.BLACK);
                itemHolder.dbr.setVisibility(View.GONE);
                itemHolder.dbr.setText("");
            } else {
                itemHolder.von.setTextColor(Color.RED);
                itemHolder.von.setText(item.getAdb());
                itemHolder.dbr.setVisibility(View.VISIBLE);
                itemHolder.dbr.setText(item.getDbr());
                countab++;
            }

            if (item.getAde().equals("null")) {
                itemHolder.bis.setTextColor(Color.BLACK);
                itemHolder.der.setVisibility(View.GONE);
                itemHolder.der.setText("");
            } else {
                itemHolder.bis.setTextColor(Color.RED);
                itemHolder.bis.setText(item.getAde());
                itemHolder.der.setVisibility(View.VISIBLE);
                itemHolder.der.setText(item.getDer());
                countab++;
            }


            if (item.getApause().equals("null")) {
                itemHolder.pause.setTextColor(Color.BLACK);
                itemHolder.apauser.setVisibility(View.GONE);
                itemHolder.apauser.setText("");
            } else {
                itemHolder.pause.setTextColor(Color.RED);
                itemHolder.apauser.setVisibility(View.VISIBLE);
                itemHolder.apauser.setText(item.getApauser());
                countab++;
            }

            if (countab > 0)
                itemHolder.abweichung_container.setVisibility(View.VISIBLE);
            else
                itemHolder.abweichung_container.setVisibility(View.GONE);


            itemHolder.funktion.setText(item.getFunktion());

            itemHolder.pause.setText("RP: " + item.getPause());

            itemHolder.datum.setText(item.getDatumFormatted("EEEE, dd. MMMM yyyy"));


            itemHolder.est.setText(item.getEst());

            itemHolder.az.setText("AZ: " + item.getAz());

            itemHolder.mdifferenz.setText("AZ-Stand: " + item.getMdifferenz());

            int bgColor = Color.parseColor("#A4A4A4");
            switch (item.getKat().substring(0, 1)) {
                case "S":
                    bgColor = Color.parseColor("#A4A4A4");
                    break;
                case "U":
                    bgColor = Color.parseColor("#00C000");
                    break;
                case "D":
                    bgColor = Color.parseColor("#0080FF");
                    break;
                case "K":
                    bgColor = Color.parseColor("#FF8000");
                    break;
                case "R":
                    bgColor = Color.parseColor("#FFFF00");
                    break;
                case "T":
                    bgColor = Color.parseColor("#FF0000");
                    break;
                case "B":
                    bgColor = Color.parseColor("#804000");
                    break;
                case "O":
                    bgColor = Color.parseColor("#282828");
                    break;
                case "F":
                    bgColor = Color.parseColor("#5E00AA");
                    break;
            }

            itemHolder.type_block.setBackgroundColor(bgColor);

        }

        return rowView;
    }
}