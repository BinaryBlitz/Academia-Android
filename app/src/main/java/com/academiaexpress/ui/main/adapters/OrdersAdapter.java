package com.academiaexpress.ui.main.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.academiaexpress.ui.order.OrderDetailsActivity;
import com.academiaexpress.data.Order;
import com.academiaexpress.R;

import java.util.ArrayList;

public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity context;
    private ArrayList<Order> collection;

    private static final String EXTRA_PRICE = "price";

    public OrdersAdapter(Activity context) {
        this.context = context;
        collection = new ArrayList<>();
    }

    public void setCollection(ArrayList<Order> collection) {
        this.collection = collection;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_order, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final ViewHolder holder = (ViewHolder) viewHolder;

        if (collection.get(position) == null) {
            setHeader(holder, position);
        } else {
            setItem(holder, position);
        }
    }

    private void setHeader(ViewHolder holder, int position) {
        holder.date.setVisibility(View.GONE);
        holder.price.setVisibility(View.GONE);
        holder.order.setPadding(0, 50, 0, 50);
        holder.itemView.findViewById(R.id.imageView24).setVisibility(View.GONE);
        holder.order.setText(position == 0 ? context.getString(R.string.orders_on_the_way) : context.getString(R.string.completed_orders));
    }

    private void setItem(final ViewHolder holder, int position) {
        holder.date.setVisibility(View.VISIBLE);
        holder.price.setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.imageView24).setVisibility(View.VISIBLE);
        holder.date.setText(context.getString(R.string.order_from) + collection.get(position).getDate());
        holder.order.setPadding(0, 0, 0, 0);

        holder.order.setText(generateParts(position));
        holder.price.setText(context.getString(R.string.price_adapter) + collection.get(position).getPrice() + context.getString(R.string.ruble_sign));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity(holder.getAdapterPosition());
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private String generateParts(int position) {
        if (collection.get(position).getParts() == null) {
            return "";
        }

        String order = "";
        for (int i = 0; i < collection.get(position).getParts().size(); i++) {
            order += collection.get(position).getParts().get(i).getName();
            if (i != collection.get(position).getParts().size() - 1) {
                order += ", ";
            }
        }

        return order;
    }

    private void openActivity(int position) {
        Intent intent = new Intent(context, OrderDetailsActivity.class);
        OrderDetailsActivity.order = collection.get(position);
        intent.putExtra(EXTRA_PRICE, context.getString(R.string.order_price_code) + collection.get(position).getPrice() + context.getString(R.string.ruble_sign));
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return collection.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private TextView order;
        private TextView price;

        ViewHolder(final View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
            order = (TextView) itemView.findViewById(R.id.order);
            price = (TextView) itemView.findViewById(R.id.price);
        }
    }
}