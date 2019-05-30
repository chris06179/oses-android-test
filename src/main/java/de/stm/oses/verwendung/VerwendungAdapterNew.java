package de.stm.oses.verwendung;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.stm.oses.R;

public class VerwendungAdapterNew extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private SortedList<VerwendungClass> items;

    public static final int TYPE_SUM = 0;
    public static final int TYPE_ITEM = 1;

    private int selection = -1;

    OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public VerwendungAdapterNew(Context context) {
        this.context = context;

        this.items = new SortedList<>(VerwendungClass.class, new SortedListAdapterCallback<VerwendungClass>(this) {
            @Override
            public int compare(VerwendungClass lhs, VerwendungClass rhs) {

                    if (lhs.getDatumDate().after(rhs.getDatumDate())) return 1;

                     if (lhs.getDatumDate().before(rhs.getDatumDate())) return -1;

                    return 0;
            }

            @Override
            public boolean areContentsTheSame(VerwendungClass oldItem, VerwendungClass newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(VerwendungClass item1, VerwendungClass item2) {

                return item1.equals(item2);

            }
        });
    }


    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    public SortedList<VerwendungClass> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position).isVerwendungSummary())
            return TYPE_SUM;
        else
            return TYPE_ITEM;
    }

    public VerwendungClass getItem(int id) {
        return items.get(id);
    }

    public class VerwendungViewHolder extends RecyclerView.ViewHolder {

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

        VerwendungViewHolder(View rowView) {
            super(rowView);
            container = (FrameLayout) rowView.findViewById(R.id.verwendung_container);
            bezeichner = (TextView) rowView.findViewById(R.id.verwendung_bezeichner);
            von = (TextView) rowView.findViewById(R.id.verwendung_von);
            bis = (TextView) rowView.findViewById(R.id.verwendung_bis);
            pause = (TextView) rowView.findViewById(R.id.verwendung_pause);
            datum = (TextView) rowView.findViewById(R.id.verwendung_datum);
            est = (TextView) rowView.findViewById(R.id.verwendung_est);
            az = (TextView) rowView.findViewById(R.id.verwendung_az);
            mdifferenz = (TextView) rowView.findViewById(R.id.verwendung_mdifferenz);
            funktion = (TextView) rowView.findViewById(R.id.verwendung_funktion);
            dbr = (TextView) rowView.findViewById(R.id.verwendung_dbr);
            der = (TextView) rowView.findViewById(R.id.verwendung_der);
            apauser = (TextView) rowView.findViewById(R.id.verwendung_apauser);
            notiz = (TextView) rowView.findViewById(R.id.verwendung_notiz);
            info = (TextView) rowView.findViewById(R.id.verwendung_info);
            abweichung_container = (RelativeLayout) rowView.findViewById(R.id.verwendung_abweichung_container);
            info_container = (RelativeLayout) rowView.findViewById(R.id.verwendung_info_container);
            notiz_container = (RelativeLayout) rowView.findViewById(R.id.verwendung_notiz_container);
            type_block = rowView.findViewById(R.id.verwendung_type_block);
        }
    }

    public class SumViewHolder extends RecyclerView.ViewHolder {

        TextView sumTitel;
        TextView sumSchichten;
        TextView sumUrlaub;
        TextView sumSoll;
        TextView sumIst;
        TextView sumDifferenz;

        SumViewHolder(View rowView) {
            super(rowView);
            sumTitel = (TextView) rowView.findViewById(R.id.verwendung_sum_monat);
            sumSchichten = (TextView) rowView.findViewById(R.id.verwendung_sum_schichten);
            sumUrlaub = (TextView) rowView.findViewById(R.id.verwendung_sum_urlaub);
            sumSoll = (TextView) rowView.findViewById(R.id.verwendung_sum_soll);
            sumIst = (TextView) rowView.findViewById(R.id.verwendung_sum_ist);
            sumDifferenz = (TextView) rowView.findViewById(R.id.verwendung_sum_differenz);

        }
    }

    public int getSelection() {
        return this.selection;
    }

    public void clear() {
        items.clear();
        selection = -1;
    }

    public void setSelection(int id) {
        this.selection = id;
        notifyItemRangeChanged(0, items.size());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }



    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public VerwendungClass getSelectedItem() {
        if (selection == -1)
            return null;

        for (int i = 0; i < items.size(); i++) {
            if (selection == items.get(i).getId())
                return items.get(i);
        }

        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_ITEM:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.verwendung_item2, parent, false);
                return new VerwendungViewHolder(v);
            case TYPE_SUM:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.verwendung_summary, parent, false);
                return new SumViewHolder(v);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        VerwendungClass item = items.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_ITEM:

                VerwendungViewHolder itemHolder = (VerwendungViewHolder) holder;

                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("Dyyyy", Locale.GERMAN); // the format of your date
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
                String currentDate = sdf.format(date);


                if (selection == position) {
                    itemHolder.container.setForeground(ContextCompat.getDrawable(context, R.drawable.list_selector));
                } else if (currentDate.equals(item.getDatumFormatted("Dyyy"))) {
                    itemHolder.container.setForeground(ContextCompat.getDrawable(context, R.drawable.today_selector));
                } else
                    itemHolder.container.setForeground(null);

                // Set Data
                if (item.getFpla().equals("0"))
                    itemHolder.bezeichner.setText(item.getBezeichner());
                else
                    itemHolder.bezeichner.setText(item.getBezeichner() + "\nFpl/N/" + item.getFpla());

                if (item.getKat().equals("T"))
                    itemHolder.bezeichner.setText(itemHolder.bezeichner.getText()+ "\n(Streik)");

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
                switch(item.getKat().substring(0,1)) {
                    case "S":  bgColor = Color.parseColor("#A4A4A4"); break;
                    case "U":  bgColor = Color.parseColor("#00C000"); break;
                    case "D":  bgColor = Color.parseColor("#0080FF"); break;
                    case "K":  bgColor = Color.parseColor("#FF8000"); break;
                    case "R":  bgColor = Color.parseColor("#FFFF00"); break;
                    case "T":  bgColor = Color.parseColor("#FF0000"); break;
                    case "B":  bgColor = Color.parseColor("#804000"); break;
                    case "O":  bgColor = Color.parseColor("#282828"); break;
                    case "F":  bgColor = Color.parseColor("#5E00AA"); break;
                }

                itemHolder.type_block.setBackgroundColor(bgColor);

                break;

            case TYPE_SUM:

                SumViewHolder sumHolder = (SumViewHolder) holder;

                sumHolder.sumTitel.setText(item.getLabel());
                sumHolder.sumSchichten.setText(String.valueOf(item.getSchichten()));
                sumHolder.sumUrlaub.setText(String.valueOf(item.getUrlaub()));
                sumHolder.sumSoll.setText(item.getMsoll());
                sumHolder.sumIst.setText(item.getAzg());
                sumHolder.sumDifferenz.setText(item.getMdifferenz());

                break;

        }

    }





}