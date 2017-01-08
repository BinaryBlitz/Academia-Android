package com.academiaexpress.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.academiaexpress.Activities.EditOrderActivity;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.R;

import java.util.ArrayList;

public class DeliveryPastAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity context;
    ArrayList<DeliveryOrder.OrderPart> collection;
    boolean inc = false;

    public boolean isInc() {
        return inc;
    }

    public void setInc(boolean inc) {
        this.inc = inc;
    }

    public DeliveryPastAdapter(Activity context) {
        this.context = context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public void setCollection(ArrayList<DeliveryOrder.OrderPart> collection) {
        this.collection = collection;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.delivry_past_card, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final NewsViewHolder holder = (NewsViewHolder) viewHolder;

        holder.date.setText((collection.get(position).getCount()) + "  x");

        SpannableString content = new SpannableString(collection.get(position).getName());
        content.setSpan(new UnderlineSpan(), 0, collection.get(position).getName().length(), 0);
        holder.order.setText(content);
        holder.price.setText(collection.get(position).getPrice() + Html.fromHtml("<html>&#x20bd</html>").toString());

        if(inc) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, EditOrderActivity.class);
                    intent.putExtra("price", collection.get(position).getPrice());
                    intent.putExtra("count", collection.get(position).getCount());
                    intent.putExtra("name",  collection.get(position).getName());
                    intent.putExtra("index", position);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return collection.size();
    }

    private class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private TextView order;
        private TextView price;

        public NewsViewHolder(final View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.textView27);
            order = (TextView) itemView.findViewById(R.id.textView21);
            price = (TextView) itemView.findViewById(R.id.textView22);
        }
    }
}