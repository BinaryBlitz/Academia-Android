package com.academiaexpress.ui.order;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import com.academiaexpress.R;
import com.academiaexpress.extras.Extras;
import com.academiaexpress.network.DeviceInfoStore;
import com.academiaexpress.network.ServerApi;
import com.academiaexpress.ui.BaseActivity;
import com.academiaexpress.ui.views.customViews.MyMapFragment;
import com.academiaexpress.utils.AndroidUtilities;
import com.academiaexpress.utils.LogUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int LOCATION_PERMISSION = 1;
    private GoogleMap googleMap;
    private LocationManager manager;
    private LatLng selectedLocation = null;
    private String selectedLocationName = "";
    private GoogleApiClient mGoogleApiClient;

    private ArrayList<LatLng> coordinates;
    private ProgressDialog dialog;

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


    private void initFields() {
        dialog = new ProgressDialog(this);
    }

    private void setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLocationName = "";
                finish();
            }
        });

        findViewById(R.id.editText3fd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(MapActivity.this)) {
                    Snackbar.make(findViewById(R.id.main), "Нет подключения к интернету!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                try {
                    if (isSelectedPointCorrect()) {
                        Snackbar.make(findViewById(R.id.main), R.string.location_out_zone, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    Intent locationIntent = new Intent();
                    locationIntent.putExtra(Extras.EXTRA_LOCATION_NAME, selectedLocationName);
                    locationIntent.putExtra(Extras.EXTRA_LOCATION_LATLNG, selectedLocation);
                    setResult(RESULT_OK, locationIntent);
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
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.751244, 37.618423), 10.0f));

        getLocationFromGoogle();
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
        }, 30);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermission()) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
            } else {
                setUpMap();
            }
        } catch (Exception e) {
            dialog.dismiss();
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
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
                    checkLocationInSettings();
                } else {
                    checkLocationInSettings();
                    getLocation();
                }
            } catch (Exception e) {
                LogUtil.logException(e);
            }
        } else {
            checkLocationInSettings();
            getLocation();
        }

    }

    private void checkLocationInSettings() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);
        PendingResult result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        if (result != null) {
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                if (status.hasResolution()) {
                                    status.startResolutionForResult(MapActivity.this, 1000);
                                }
                            } catch (IntentSender.SendIntentException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }

        Location lastLocation;
        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria mCriteria = new Criteria();
        String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
        lastLocation = manager.getLastKnownLocation(bestProvider);

        if (lastLocation != null) {
            final double currentLatitude = lastLocation.getLatitude();
            final double currentLongitude = lastLocation.getLongitude();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 15));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

            processGoogleLocation(lastLocation);
        }
    }

    private void processGoogleLocation(Location lastLocation) {
        selectedLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        getCompleteAddressString(selectedLocation.latitude, selectedLocation.longitude);
        ((TextView) findViewById(R.id.editText3)).setText(selectedLocationName);
        moveCamera(true);
    }

    private void moveCamera(final boolean setText) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(selectedLocation)
                .zoom(15)
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

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == Activity.RESULT_OK) && (requestCode == 1000)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
            }
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    @Override
    public void onBackPressed() {
        selectedLocationName = "";
        finish();
        super.onBackPressed();
    }


}
