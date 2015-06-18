package de.stm.oses.helper;

import java.util.ArrayList;
 
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.stm.oses.R;

public class MenuAdapter extends ArrayAdapter<MenuClass> {
 
        private final Context context;
        private final ArrayList<MenuClass> MenuClassArrayList;
 
        public MenuAdapter(Context context, ArrayList<MenuClass> MenuClassArrayList) {
 
            super(context, R.layout.menu_item, MenuClassArrayList);
 
            this.context = context;
            this.MenuClassArrayList = MenuClassArrayList;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
 
            // 1. Create inflater 
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
            // 2. Get rowView from inflater
 
            View rowView;
            if(!MenuClassArrayList.get(position).isGroupHeader()){
                rowView = inflater.inflate(R.layout.menu_item, parent, false);
 
                // 3. Get icon,title & counter views from the rowView
                ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon); 
                TextView titleView = (TextView) rowView.findViewById(R.id.item_title);
                TextView counterView = (TextView) rowView.findViewById(R.id.item_counter);
 
                // 4. Set the text for textView 
                imgView.setImageResource(MenuClassArrayList.get(position).getIcon());
                titleView.setText(MenuClassArrayList.get(position).getTitle());
                
                if (!MenuClassArrayList.get(position).getCounter().equals(""))
                	counterView.setText(MenuClassArrayList.get(position).getCounter());
                else
                	counterView.setVisibility(View.INVISIBLE);

                if(MenuClassArrayList.get(position).isSelected()) {

                    RelativeLayout container = (RelativeLayout) rowView.findViewById(R.id.menu_layout);
                    View block = rowView.findViewById(R.id.menu_select_block);

                    //container.setBackgroundColor(Color.parseColor("#E9E9E9"));
                    block.setVisibility(View.VISIBLE);


                }
            }
            else{
                    rowView = inflater.inflate(R.layout.menu_item_header, parent, false);
                    TextView titleView = (TextView) rowView.findViewById(R.id.header);
                    titleView.setText(MenuClassArrayList.get(position).getTitle());
 
            }
 
            // 5. retrn rowView
            return rowView;
        }		
}