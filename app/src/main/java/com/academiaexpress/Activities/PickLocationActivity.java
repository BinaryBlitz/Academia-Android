package com.academiaexpress.Activities;

import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.academiaexpress.Adapters.LocationSearchAdapter;
import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.R;
import com.academiaexpress.Utils.Image;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PickLocationActivity extends BaseActivity {

    private EditText location;
    private LocationSearchAdapter adapter;
    private LatLng loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_location_layout);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView listView = (RecyclerView) findViewById(R.id.recyclerView);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setHasFixedSize(true);
        adapter = new LocationSearchAdapter(this);
        listView.setAdapter(adapter);

        location = (EditText) findViewById(R.id.editText4);

        location.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    hideKeyboard(textView);
                    return true; // consume.
                }

                return false;
            }
        });
        location.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // Abstract Method of TextWatcher Interface.
            }


            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {
                // Abstract Method of TextWatcher Interface.
            }

            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {

                adapter.searchGooglePlacesWithQuery(s.toString(), loc);
            }
        });

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                Location location = locationManager.getLastKnownLocation(provider);

                try {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    loc = new LatLng(lat, lng);

                    Geocoder geoCoder = new Geocoder(PickLocationActivity.this, Locale.getDefault());

                    StringBuilder builder = new StringBuilder();

                    List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
                    int maxLines = address.get(0).getMaxAddressLineIndex();
                    for (int i = 1; i <= maxLines - 1; i++) {
                        String addressStr = address.get(0).getAddressLine(i);
                        builder.append(addressStr);
                        builder.append(i == maxLines - 1 ? " " : ", ");
                    }
                    String fnialAddress = builder.toString(); //This is the complete address.
                    adapter.searchGooglePlacesWithQuery(fnialAddress, loc);


                } catch (IOException ignored) {
                } catch (NullPointerException e) {
                }
            }
        }, 50);

    }

    public void setText(String text) {
        location.setText(text);
    }

    protected void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}