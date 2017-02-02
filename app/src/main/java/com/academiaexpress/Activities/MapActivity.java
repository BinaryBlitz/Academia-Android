package com.academiaexpress.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Custom.MyMapFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.LogUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends BaseActivity
        implements MyMapFragment.TouchableWrapper.UpdateMapAfterUserInteraction, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_PERMISSION = 1;
    private GoogleMap googleMap;

    static public LatLng selected_lat_lng;
    static public String selected = "";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    ArrayList<LatLng> lats;

    public static boolean picked = false;

    public static boolean fir = true;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    private void initMap() {
        final SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.scroll);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mMap.getMapAsync(MapActivity.this);
            }
        });
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient != null) return;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fir = true;

        findViewById(R.id.editText3fd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(MapActivity.this)) {
                    return;
                }
                try {
                    if (((TextView) findViewById(R.id.editText3)).getText().toString().isEmpty() ||
                            ((TextView) findViewById(R.id.editText3)).getText().toString().equals("ВВЕСТИ АДРЕС")
                            || lats == null
                            || !PolyUtil.containsLocation(selected_lat_lng, lats, false)) {
                        selected = "";
                        selected_lat_lng = null;
                        Snackbar.make(findViewById(R.id.main), "Мы доставляем только внутри Садового Кольца.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    finish();
                } catch (Exception e) {
                    Snackbar.make(findViewById(R.id.main), "Мы доставляем только внутри Садового Кольца.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        selected = "";
        selected_lat_lng = null;

        initMap();
        initGoogleApiClient();

    }

    @SuppressLint("NewApi")
    private boolean checkPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setPadding(0, (int) AndroidUtilities.INSTANCE.convertDpToPixel(66f, this), 0, 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermission()) {
                ActivityCompat.requestPermissions(MapActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION);
            } else {
                setUpMap();
            }
        } catch (Exception e) {
            LogUtil.logException(e);
        }
    }

    private void getEdgePoints() {
        ServerApi.get(this).api().getEdgePoints(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) parseEdgePoints(response.body());
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    private void parseEdgePoints(JsonArray array) {
        LatLng[] lats = new LatLng[array.size()];

        for (int i = 0; i < array.size(); i++) {
            try {
                JsonObject object = array.get(i).getAsJsonObject();
                lats[i] = new LatLng(object.get("latitude").getAsDouble(),
                        object.get("longitude").getAsDouble());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MapActivity.this.lats = new ArrayList<>();
        Collections.addAll(MapActivity.this.lats, lats);

        PolygonOptions rectOptions = new PolygonOptions()
                .fillColor(Color.argb(30, 56, 142, 60))
                .strokeColor(Color.parseColor("#4CAF50"))
                .add(lats);
        googleMap.addPolygon(rectOptions);
    }

    private String getCompleteAddressString(double latitude, double lognitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, lognitude, 1);
            if (addresses != null) {
                if (addresses.size() > 0) {
                    strAdd = addresses.get(0).getAddressLine(0);
                }
            }
        } catch (Exception ignored) {
        }

        selected_lat_lng = new LatLng(latitude, lognitude);

        selected = strAdd;
        return strAdd;
    }

    @Override
    public void onUpdateMapAfterUserInteraction() {
        try {
            String res = getCompleteAddressString(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
            ((TextView) findViewById(R.id.editText3)).setText(res);
        } catch (Exception e) {
            LogUtil.logException(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient.isConnected()) {
                    getLocation();
                } else {
                    mGoogleApiClient.connect();
                }
            }
        }, 50);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION);
                } else {
                    getLocation();
                }
            } catch (Exception e) {
                LogUtil.logException(e);
            }
        } else {
            getLocation();
        }
    }

    private boolean isPointInPolygon(LatLng tap, ArrayList<LatLng> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
                intersectCount++;
            }
        }

        return ((intersectCount % 2) == 1);
    }

    private boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {
        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY) || (aX < pX && bX < pX)) {
            return false;
        }

        double m = (aY - bY) / (aX - bX);
        double bee = (-aX) * m + aY;
        double x = (pY - bee) / m;

        return x > pX;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocationError();
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            selected_lat_lng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            moveCamera(true);
        } else {
            onLocationError();
        }
    }

    private void moveCamera(final boolean setText) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selected_lat_lng)
                .zoom(17)
                .bearing(0)
                .tilt(0)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (setText) {
                            getCompleteAddressString(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
                        }
                    }
                }, 50);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    onLocationError();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onLocationError();
    }
}
