package de.stm.oses.helper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import de.stm.oses.R;

public class ListAdapter extends ArrayAdapter<ListClass> implements Serializable {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private final Context context;
    private final ArrayList<ListClass> listClassArrayList;

    public ListAdapter(Context context, ArrayList<ListClass> listClassArrayList) {

        super(context, R.layout.list_item, listClassArrayList);

        this.context = context;
        this.listClassArrayList = listClassArrayList;
    }


    public int getSelection() {
        for (int i = 0; i < listClassArrayList.size(); i++)
            if (listClassArrayList.get(i).isSelected())
                return i;

        return -1;
    }

    public void setSelectionID(int id) {
        for (int i = 0; i < listClassArrayList.size(); i++)
            if (listClassArrayList.get(i).getId() == id)
                listClassArrayList.get(i).setSelected(true);
            else
                listClassArrayList.get(i).setSelected(false);
    }

    public void setSelectionIdent(String ident) {
        for (int i = 0; i < listClassArrayList.size(); i++)
            if (listClassArrayList.get(i).getIdent().equals(ident))
                listClassArrayList.get(i).setSelected(true);
            else
                listClassArrayList.get(i).setSelected(false);
    }


    public void setSelection(int pos) {
        for (int i = 0; i < listClassArrayList.size(); i++)
            listClassArrayList.get(i).setSelected(false);

        listClassArrayList.get(pos).setSelected(true);

    }

    @Override
    public int getItemViewType(int position) {
        if (listClassArrayList.get(position).isHeader())
            return TYPE_HEADER;
        else
            return TYPE_ITEM;
    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void addEstIfNotExists(int estId, String estort) {
        for (ListClass est : listClassArrayList
        ) {
            if (est.getId() == estId) {
                return;
            }
        }

        listClassArrayList.add(new ListClass(true, "Aktuell bearbeitet"));
        listClassArrayList.add(new ListClass(estId,estort, 0));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (type == TYPE_HEADER)
                rowView = inflater.inflate(R.layout.list_item_header, parent, false);
            if (type == TYPE_ITEM)
                rowView = inflater.inflate(R.layout.list_item, parent, false);
        }

        if (type == TYPE_HEADER) {

            TextView titleView = rowView.findViewById(R.id.list_header);
            titleView.setText(listClassArrayList.get(position).getHeaderText());

        }

        if (type == TYPE_ITEM) {

            TextView titleView = rowView.findViewById(R.id.list_title);
            RadioButton radio = rowView.findViewById(R.id.list_selected);

            titleView.setText(listClassArrayList.get(position).getTitle());

            if (listClassArrayList.get(position).isSelected())
                radio.setChecked(true);
            else
                radio.setChecked(false);

            ImageView icon = rowView.findViewById(R.id.list_icon);

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