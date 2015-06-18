package de.stm.oses.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import de.stm.oses.R;

public class ListSpinnerAdapter extends ListAdapter {

        private final Context context;
        private final ArrayList<ListClass> listClassArrayList;
        private boolean isKategorie = false;
        private boolean showRadio = true;

        public ListSpinnerAdapter(Context context, ArrayList<ListClass> listClassArrayList) {
            super(context, listClassArrayList);
            this.context = context;
            this.listClassArrayList = listClassArrayList;
        }

        public ListSpinnerAdapter(Context context, ArrayList<ListClass> listClassArrayList, boolean isKategorie) {
            super(context, listClassArrayList);
            this.context = context;
            this.listClassArrayList = listClassArrayList;
            this.isKategorie = isKategorie;
        }

        public void setshowRadio(boolean show) {
           this.showRadio = show;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView;

            if (isKategorie) {

                rowView = inflater.inflate(R.layout.kategorie_item, parent, false);

                TextView titleView = (TextView) rowView.findViewById(R.id.list_title);
                titleView.setText(listClassArrayList.get(position).getTitle());

                ImageView icon = (ImageView) rowView.findViewById(R.id.list_icon);
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(listClassArrayList.get(position).getIcon());
                icon.setColorFilter(Color.parseColor(listClassArrayList.get(position).getColor()), PorterDuff.Mode.MULTIPLY);

            } else {

                rowView = inflater.inflate(R.layout.list_item_spinner, parent, false);

                TextView titleView = (TextView) rowView.findViewById(R.id.list_title);
                titleView.setText(listClassArrayList.get(position).getTitle());

            }

            return rowView;
        }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        if (convertView != null && (Integer)convertView.getTag() != getItemViewType(position))
            convertView = null;

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (type == TYPE_HEADER)
                rowView = inflater.inflate(R.layout.list_item_header, parent, false);
            if (type == TYPE_ITEM)
                rowView = inflater.inflate(R.layout.list_item, parent, false);
        }

        rowView.setTag(Integer.valueOf(type));


        if (type == TYPE_HEADER) {

            TextView titleView = (TextView) rowView.findViewById(R.id.list_header);
            titleView.setText(listClassArrayList.get(position).getHeaderText());

        }

        if (type == TYPE_ITEM) {

            TextView titleView = (TextView) rowView.findViewById(R.id.list_title);
            RadioButton radio = (RadioButton) rowView.findViewById(R.id.list_selected);

            titleView.setText(listClassArrayList.get(position).getTitle());
            if (showRadio) {
                if (listClassArrayList.get(position).isSelected())
                    radio.setChecked(true);
                else
                    radio.setChecked(false);
            } else
                radio.setVisibility(View.GONE);

            ImageView icon = (ImageView) rowView.findViewById(R.id.list_icon);

            if (listClassArrayList.get(position).getIcon() > 0) {
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(listClassArrayList.get(position).getIcon());

                if (!listClassArrayList.get(position).getColor().equals(""))
                    icon.setColorFilter(Color.parseColor(listClassArrayList.get(position).getColor()), PorterDuff.Mode.MULTIPLY);

                if (listClassArrayList.get(position).getIcon() == R.drawable.ic_blank)
                    icon.setBackgroundColor(Color.parseColor("#FFFFFF"));

            } else if (listClassArrayList.get(position).getIcon() == 0)
                icon.setVisibility(View.INVISIBLE);
            else if (listClassArrayList.get(position).getIcon() == -1)
                icon.setVisibility(View.GONE);

        }

        return rowView;

    }
}