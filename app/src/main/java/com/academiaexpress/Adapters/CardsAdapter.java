package com.academiaexpress.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.academiaexpress.Activities.DeliveryFinalActivity;
import com.academiaexpress.Data.CreditCard;
import com.academiaexpress.R;

import java.util.ArrayList;

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity context;

    private ArrayList<CreditCard> collection;

    boolean inc = false;

    public boolean isInc() {
        return inc;
    }

    public void setInc(boolean inc) {
        this.inc = inc;
    }

    public CardsAdapter(Activity context) {
        this.context = context;
        collection = new ArrayList<>();
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public void setCollection(ArrayList<CreditCard> collection) {
        this.collection = collection;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_card, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final NewsViewHolder holder = (NewsViewHolder) viewHolder;

        holder.date.setText(collection.get(position).getNumber());

        if(collection.get(position).getSelected()) {
            holder.itemView.findViewById(R.id.imageView25).setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.argb(128, 0, 0, 0));
        } else {
            holder.itemView.findViewById(R.id.imageView25).setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.argb(0, 0, 0, 0));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < collection.size(); i++) {
                    collection.get(i).setSelected(false);
                }
                collection.get(position).setSelected(true);

                DeliveryFinalActivity.binding = collection.get(position).getBinding();
                DeliveryFinalActivity.cardIndex = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return collection.size();
    }

    private class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView date;

        public NewsViewHolder(final View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.textView63);
        }
    }
}