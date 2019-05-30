package de.stm.oses.fax;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import de.stm.oses.R;

public class FaxAdapter extends RecyclerView.Adapter<FaxAdapter.FaxViewHolder>{

    private Context context;
    private SortedList<FaxClass> items;

    private int selection = -1;

    OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public FaxAdapter(Context context) {
        this.context = context;

        this.items = new SortedList<>(FaxClass.class, new SortedListAdapterCallback<FaxClass>(this) {
            @Override
            public int compare(FaxClass lhs, FaxClass rhs) {

                    if (lhs.getDistance() < rhs.getDistance())
                        return -1;

                    if (lhs.getDistance() > rhs.getDistance())
                        return 1;

                    return 0;
            }

            @Override
            public boolean areContentsTheSame(FaxClass oldItem, FaxClass newItem) {
                return oldItem.getId() == newItem.getId() && oldItem.getDistance() == newItem.getDistance();
            }

            @Override
            public boolean areItemsTheSame(FaxClass item1, FaxClass item2) {

                return item1.equals(item2);

            }
        });
    }


    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    public SortedList<FaxClass> getItems() {
        return items;
    }

    public class FaxViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView beschreibung;
        TextView dist;
        TextView gb;
        RelativeLayout main;
        FrameLayout frame;

        FaxViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.fax_name);
            dist = (TextView) itemView.findViewById(R.id.fax_dist);
            beschreibung = (TextView) itemView.findViewById(R.id.fax_beschreibung);
            gb = (TextView) itemView.findViewById(R.id.fax_gb);
            main = (RelativeLayout) itemView.findViewById(R.id.fax_layout);
            frame = (FrameLayout) itemView.findViewById(R.id.fax_frame);
        }
    }

    public int getSelectionId() {
        return this.selection;
    }

    public void clear() {
        items.clear();
        selection = -1;
    }

    public void setSelectionId(int id) {
        this.selection = id;
        notifyItemRangeChanged(0, items.size());
    }


    public FaxClass setSelectionByMarker(Marker marker) {

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getMarker().getId().equals(marker.getId())) {
                setSelectionId(items.get(i).getId());
                return items.get(i);
            }
        }

        return null;

    }

    @Override
    public int getItemCount() {
        return items.size();
    }



    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public FaxClass getSelectedItem() {
        if (selection == -1)
            return null;

        for (int i = 0; i < items.size(); i++) {
            if (selection == items.get(i).getId())
                return items.get(i);
        }

        return null;
    }

    @Override
    public FaxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fax_item, parent, false);
        return new FaxViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FaxViewHolder holder, final int position) {
        final FaxClass item = items.get(position);


        holder.name.setText(item.getName());
        holder.dist.setText(String.format("%.1f", item.getDistance()) + " km");
        if (!holder.beschreibung.getText().toString().equals(item.getBeschreibung())) {
            holder.beschreibung.setText(item.getBeschreibung());
            holder.beschreibung.setSelected(true);
        }
        holder.gb.setText(item.getGb());

        if (selection == item.getId()) {
            holder.main.setBackgroundResource(R.drawable.list_selector);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                holder.itemView.setTranslationZ(10);
        }
        else {
            holder.main.setBackgroundColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                holder.itemView.setTranslationZ(0);
        }




        holder.frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(v, position);
                }
            }
        });



    }





}