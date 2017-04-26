package com.academiaexpress.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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

    public static LatLng selectedLocation = null;
    public static String selectedLocationName = "";

    private GoogleApiClient mGoogleApiClient;

    private ArrayList<LatLng> coordinates;
    private ProgressDialog dialog;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    private void initMap() {
        dialog.show();
        final SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.scroll);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mMap.getMapAsync(MapActivity.this);
            }
        });
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initGoogleApiClient();
        initFields();
        initMap();
        setOnClickListeners();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getEdgePoints();
            }
        });
    }

    private void initFields() {
        dialog = new ProgressDialog(this);
    }

    private void setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.editText3fd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(MapActivity.this)) {
                    return;
                }
                try {
                    if (isSelectedPointCorrect()) {
                        Snackbar.make(findViewById(R.id.main), R.string.location_out_zone, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    finish();
                } catch (Exception e) {
                    Snackbar.make(findViewById(R.id.main), R.string.location_out_zone, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isSelectedPointCorrect() {
        return ((TextView) findViewById(R.id.editText3)).getText().toString().isEmpty() ||
                ((TextView) findViewById(R.id.editText3)).getText().toString().equals(getString(R.string.select_address))
                || coordinates == null
                || !PolyUtil.containsLocation(selectedLocation, coordinates, false);
    }

    @SuppressLint("NewApi")
    private boolean checkPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void setUpMap() {
        dialog.dismiss();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (selectedLocation != null) {
            processPreviouslySelectedLocation();
        } else {
            getLocationFromGoogle();
        }
    }

    private void processPreviouslySelectedLocation() {
        getCompleteAddressString(selectedLocation.latitude, selectedLocation.longitude);
        ((TextView) findViewById(R.id.editText3)).setText(selectedLocationName);
        moveCamera(true);
    }

    private void getLocationFromGoogle() {
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermission()) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(MapActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION);
            } else {
                setUpMap();
            }
        } catch (Exception e) {
            dialog.dismiss();
            LogUtil.logException(e);
        }
    }

    private void getEdgePoints() {
        ServerApi.get(this).api().getEdgePoints(DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    parseEdgePoints(response.body());
                }
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

        coordinates = new ArrayList<>();
        Collections.addAll(coordinates, lats);

        PolygonOptions rectOptions = new PolygonOptions()
                .fillColor(Color.argb(30, 56, 142, 60))
                .strokeColor(Color.parseColor("#4CAF50"))
                .add(lats);
        googleMap.addPolygon(rectOptions);
    }

    private void getCompleteAddressString(double latitude, double longitude) {
        String strAdd = selectedLocationName;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                if (addresses.size() > 0) {
                    strAdd = addresses.get(0).getAddressLine(0);
                }
            }
        } catch (Exception e) {
            LogUtil.logException(e);
        }

        selectedLocationName = strAdd;
    }

    @Override
    public void onUpdateMapAfterUserInteraction() {
        try {
            selectedLocation = googleMap.getCameraPosition().target;
            getCompleteAddressString(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
            ((TextView) findViewById(R.id.editText3)).setText(selectedLocationName);
        } catch (Exception e) {
            LogUtil.logException(e);
        }
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

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (lastLocation != null) {
            processGoogleLocation(lastLocation);
        }
    }

    private void processGoogleLocation(Location lastLocation) {
        LogUtil.logError(lastLocation.toString());
        selectedLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        getCompleteAddressString(selectedLocation.latitude, selectedLocation.longitude);
        ((TextView) findViewById(R.id.editText3)).setText(selectedLocationName);
        moveCamera(true);
    }

    private void moveCamera(final boolean setText) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selectedLocation)
                .zoom(17)
                .bearing(0)
                .tilt(0)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (setText) {
                            getCompleteAddressString(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
                        }
                    }
                });
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
                    getLocationFromGoogle();
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
