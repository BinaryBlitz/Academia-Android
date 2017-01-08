package com.academiaexpress.Adapters;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.academiaexpress.Activities.MapActivity;
import com.academiaexpress.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static class LocationObject {
        String place_name;
        String id;
        String location_name;
        LatLng location_data;
        String icon;
    }

    private Context context;
    protected boolean searching = false;
    protected ArrayList<LocationObject> places = new ArrayList<LocationObject>();
    private LatLng lastSearchLocation;

    public LocationSearchAdapter (Context context) {
        this.context = context;
        places = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item, parent, false);

        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder _holder, final int position) {
        final EventViewHolder holder = (EventViewHolder) _holder;

        holder.place_name.setText(places.get(position).place_name);
       // holder.location_name.setText(places.get(position).location_name);
//
//        holder.itemView.findViewById(R.id.main_layout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!Utils.isConnected(context)) {
//                    ((BasePopupActivity) context).showPopup();
//                    return;
//                }
//                PickLocationActivity.location = holder.location_name.getText().toString() + ", " +
//                        holder.place_name.getText().toString();
//                PickLocationActivity.latLng = places.get(position).location_data;
//                PickRegionActivity.latLng = places.get(position).location_data;
//                ((SearchActivity) context).finish();
//            }
//        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapActivity.selected = places.get(position).place_name;
                MapActivity.selected_lat_lng = getLocationFromAddress(places.get(position).place_name);
                MapActivity.picked = true;
                ((Activity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void searchGooglePlacesWithQuery(final String query, final LatLng coordinate) {
        lastSearchLocation = coordinate;
        if (searching) {
            searching = false;
           // requestQueue.cancelAll("search");
        }
        String url = "https://dadata.ru/api/v2/suggest/address";

        JSONObject to_send = new JSONObject();

        try {
            to_send.accumulate("query", query);
            to_send.accumulate("count", 30);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url,
//                to_send,
//
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            places.clear();
//                            notifyDataSetChanged();
//
//                            JSONArray result = response.getJSONArray("suggestions");
//
//                            for (int a = 0; a < result.length(); a++) {
//                                    JSONObject object = result.getJSONObject(a);
//
//                                    JSONObject location = object.getJSONObject("data");
//
//                                    LocationObject locationObject = new LocationObject();
//                                    locationObject.place_name = object.getString("value");
//                                    locationObject.id = "1";
//                                try {
//                                    locationObject.location_data =
//                                            new LatLng(location.getDouble("geo_lat"),
//                                                    location.getDouble("geo_lon"));
//                                    locationObject.location_name = location.getString("street_with_type");
//                                } catch (Exception e) {
//                                    locationObject.location_data = null;
//                                }
//
//                                    places.add(locationObject);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        searching = false;
//                        notifyDataSetChanged();
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                    }
//                })
//        {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("Content-Type", "application/json");
//                params.put("Accept", "application/json");
//                params.put("Authorization", "Token ".concat("bfdacc45560db9c73425f30f5c630842e5c8c1ad"));
//                return params;
//            }
//        }; ;
//        jsonObjReq.setShouldCache(false);
//        jsonObjReq.setTag("search");
//
//        requestQueue.add(jsonObjReq);
//        requestQueue.start();
        notifyDataSetChanged();
    }

    private class EventViewHolder extends RecyclerView.ViewHolder {
        TextView place_name;
        TextView location_name;

        public EventViewHolder(final View itemView) {
            super(itemView);
            place_name = (TextView) itemView.findViewById(R.id.name_text);
        }
    }

    public LatLng getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(),
                    location.getLongitude());

            return p1;
        } catch (Exception e) {
            return null;
        }
    }
}
