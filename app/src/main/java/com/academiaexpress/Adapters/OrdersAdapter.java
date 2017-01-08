package com.academiaexpress.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.academiaexpress.Activities.OrderDetailsActivity;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.R;

import java.util.ArrayList;

public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity context;

    private ArrayList<DeliveryOrder> collection;

    public OrdersAdapter(Activity context) {
        this.context = context;
        collection = new ArrayList<>();
    }

    public void setCollection(ArrayList<DeliveryOrder> collection) {
        this.collection = collection;
        notifyDataSetChanged();
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.order_card, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final NewsViewHolder holder = (NewsViewHolder) viewHolder;

        if(collection.get(position) == null) {
            holder.date.setVisibility(View.GONE);
            holder.price.setVisibility(View.GONE);
            holder.order.setPadding(0, 50, 0, 50);
            holder.itemView.findViewById(R.id.imageView24).setVisibility(View.GONE);
            holder.order.setText(position == 0 ? "Заказы в пути" : "Доставленные заказы");
        } else {
            holder.date.setVisibility(View.VISIBLE);
            holder.price.setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.imageView24).setVisibility(View.VISIBLE);
            holder.date.setText("Заказ от " + collection.get(position).getDate());
            holder.order.setPadding(0, 0, 0, 0);
            String order = "";
            for (int i = 0; i < collection.get(position).getParts().size(); i++) {
                order += collection.get(position).getParts().get(i).getName();
                if(i != collection.get(position).getParts().size() - 1) {
                    order += ", ";
                }
            }

            holder.order.setText(order);

            holder.price.setText("На сумму: " + collection.get(position).getPrice() + Html.fromHtml("<html>&#x20bd</html>").toString());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, OrderDetailsActivity.class);
                    OrderDetailsActivity.order = collection.get(position);
                    intent.putExtra("price", "Заказ на сумму: " + collection.get(position).getPrice() + Html.fromHtml("<html>&#x20bd</html>").toString());
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
            date = (TextView) itemView.findViewById(R.id.textView20);
            order = (TextView) itemView.findViewById(R.id.textView24);
            price = (TextView) itemView.findViewById(R.id.textView25);
        }
    }
}