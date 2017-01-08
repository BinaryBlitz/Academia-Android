package com.academiaexpress.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Activities.ProductsActivity;
import com.academiaexpress.Data.DeliveryOrder;
import com.academiaexpress.Data.MiniProduct;
import com.academiaexpress.Fragments.DishFragment;
import com.academiaexpress.R;
import com.academiaexpress.Utils.Image;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;

public class ProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MiniProduct> collection;
    private ArrayList<DeliveryOrder.OrderPart> collection2;

    private Activity context;

    public ProductsAdapter(Activity context) {
        this.context = context;
        collection = new ArrayList<>();
    }

    public void setCollection(ArrayList<MiniProduct> collection) {
        this.collection = collection;
        collection2 = new ArrayList<>();
        for(int i = 0; i < collection.size(); i++) {
            collection2.add(new DeliveryOrder.OrderPart(collection.get(i).getPrice(), collection.get(i).getName(),
                    collection.get(i).getId()));
        }

        notifyDataSetChanged();
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.mini_product_catd, parent, false);

        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final FriendViewHolder holder = (FriendViewHolder) viewHolder;
        holder.name.setText(collection.get(position).getName());
        holder.sub_name.setText(collection.get(position).getIngridients());
        holder.price.setText(collection.get(position).getPrice() + Html.fromHtml("<html>&#x20bd</html>").toString());

        Image.loadPhoto(collection.get(position).getPhotoLink(), holder.avatar);

        if(collection.get(position).getCount() == 0) {
            holder.itemView.findViewById(R.id.textView19fd).setVisibility(View.GONE);
        } else {
            holder.itemView.findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
            ((TextView) holder.itemView.findViewById(R.id.textView19fd)).setText(Integer.toString(collection.get(position).getCount()));
        }

        holder.itemView.findViewById(R.id.imageView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DishFragment.answer) {
                    Answers.getInstance().logCustom(new CustomEvent("Товар добавлен"));
                }
                DishFragment.answer = true;
                holder.itemView.findViewById(R.id.textView19fd).setVisibility(View.VISIBLE);
                collection.get(position).setCount(collection.get(position).getCount() + 1);
                ((TextView) holder.itemView.findViewById(R.id.textView19fd)).setText(Integer.toString(collection.get(position).getCount()));
                ((ProductsActivity) context).addProduct(collection2.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return collection.size();
    }

    private class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView sub_name;
        private TextView price;
        private ImageView avatar;

        public FriendViewHolder(final View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.textView12);
            sub_name = (TextView) itemView.findViewById(R.id.textView13);
            price = (TextView) itemView.findViewById(R.id.textView14);
            avatar = (ImageView) itemView.findViewById(R.id.imageView4);
        }
    }
}