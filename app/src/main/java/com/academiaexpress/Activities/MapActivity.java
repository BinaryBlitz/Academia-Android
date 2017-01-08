package com.academiaexpress.Activities;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Custom.MyMapFragment;
import com.academiaexpress.Fragments.DishFragment;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, MyMapFragment.TouchableWrapper.UpdateMapAfterUserInterection {

    GoogleMap googleMap;

    static public LatLng selected_lat_lng;
    static public String selected = "";

    static public LatLng selected_lat_lng_final;
    static public String selected_final = "";

    ArrayList<LatLng> lats;

    public static boolean picked = false;

    public static boolean fir = true;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
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
                            || !PolyUtil.containsLocation(selected_lat_lng_final, lats, false)) {
                        selected = "";
                        selected_lat_lng = null;
                        selected_lat_lng_final = null;
                        selected_final = selected;
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

        Handler handler = new Handler();

        final SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.scroll);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("qwetry", "q7");
                mMap.getMapAsync(MapActivity.this);
            }
        }, 100);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (picked) {
            picked = false;
            if (!selected.isEmpty() && selected_lat_lng != null) {

                selected_lat_lng_final = selected_lat_lng;
                selected_final = selected;

                try {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(selected_lat_lng)      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera to east
                            .tilt(45)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder


                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.editText3)).setText(selected);
                                    selected_lat_lng = null;
                                    selected = "";
                                }
                            }, 50);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                } catch (Exception e) {
                    Snackbar.
                            make(findViewById(R.id.main), "Ошибка определения адреса. Попробуйте еще раз.",
                                    Snackbar.LENGTH_LONG).show();
                }

            } else {
                Snackbar.
                        make(findViewById(R.id.main), "Ошибка определения адреса. Попробуйте еще раз.",
                                Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                        // Creating a criteria object to retrieve provider
                        Criteria criteria = new Criteria();

                        // Getting the name of the best provider
                        String provider = locationManager.getBestProvider(criteria, true);
                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Location location = locationManager.getLastKnownLocation(provider);
                        // Getting Current Location
                        // Getting latitude of the current location
                        double latitude = location.getLatitude();

                        // Getting longitude of the current location
                        double longitude = location.getLongitude();

                        LatLng myPosition = new LatLng(latitude, longitude);
                        final CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(myPosition)      // Sets the center of the map to location user
                                .zoom(17)                   // Sets the zoom
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                .build();                   // Creates a CameraPosition from the builder

                        selected_lat_lng = myPosition;

                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    } catch (Exception e) {

                    }

                    new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                                                googleMap.getCameraPosition().target.longitude);

                                        selected_lat_lng_final = new LatLng(googleMap.getCameraPosition().target.latitude,
                                                googleMap.getCameraPosition().target.longitude);
                                        ((TextView) findViewById(R.id.editText3)).setText(selected);
                                        selected_final = getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                                                googleMap.getCameraPosition().target.longitude);
                                    } catch (Exception e) {
                                        Snackbar.
                                                make(findViewById(R.id.main), "Ошибка определения адреса. Попробуйте передвинуть маркер.",
                                                        Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }, 50);
                }
            }, 450);

            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.setPadding(0, (int) DishFragment.convertDpToPixel(66f, this), 0, 0);

        } catch (Exception e) {
        }

        try {
            googleMap.setMyLocationEnabled(true);

            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location arg0) {
                    if(arg0 == null) {
                        Snackbar.
                                make(findViewById(R.id.main), "Невозможно определить ваше текущее местоположение.",
                                        Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if(fir) {
                        fir = false;
                        double latitude = arg0.getLatitude();

                        // Getting longitude of the current location
                        double longitude = arg0.getLongitude();

                        LatLng myPosition = new LatLng(latitude, longitude);
                        final CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(myPosition)      // Sets the center of the map to location user
                                .zoom(17)                   // Sets the zoom
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                .build();                   // Creates a CameraPosition from the builder

                        selected_lat_lng = myPosition;

                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                                            googleMap.getCameraPosition().target.longitude);

                                    selected_lat_lng_final = new LatLng(googleMap.getCameraPosition().target.latitude,
                                            googleMap.getCameraPosition().target.longitude);
                                    ((TextView) findViewById(R.id.editText3)).setText(selected);
                                    selected_final = getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                                            googleMap.getCameraPosition().target.longitude);
                                } catch (Exception e) {
                                    Snackbar.
                                            make(findViewById(R.id.main), "Ошибка определения адреса. Попробуйте передвинуть маркер.",
                                                    Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }, 50);
                    }
                }
            });

            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if(googleMap.getMyLocation() == null) {
                        Snackbar.
                                make(findViewById(R.id.main), "Невозможно определить ваше текущее местоположение.",
                                        Snackbar.LENGTH_LONG).show();
                        return false;
                    }
                    double latitude = googleMap.getMyLocation().getLatitude();

                    // Getting longitude of the current location
                    double longitude = googleMap.getMyLocation().getLongitude();

                    LatLng myPosition = new LatLng(latitude, longitude);
                    final CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(myPosition)      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder

                    selected_lat_lng = myPosition;

                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                                        googleMap.getCameraPosition().target.longitude);

                                selected_lat_lng_final = new LatLng(googleMap.getCameraPosition().target.latitude,
                                        googleMap.getCameraPosition().target.longitude);
                                ((TextView) findViewById(R.id.editText3)).setText(selected);
                                selected_final = getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                                        googleMap.getCameraPosition().target.longitude);
                            } catch (Exception e) {
                                Snackbar.
                                        make(findViewById(R.id.main), "Ошибка определения адреса. Попробуйте передвинуть маркер.",
                                                Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }, 50);
                    return false;
                }
            });
        } catch (Exception e) {

        }

        getEdgePoints();

        findViewById(R.id.editText3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!AndroidUtilities.INSTANCE.isConnected(MapActivity.this)) {
                    return;
                }
                Intent intent = new Intent(MapActivity.this, PickLocationActivity.class);
                startActivity(intent);
            }
        });
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
        for (int i = 0; i < lats.length; i++) {
            MapActivity.this.lats.add(lats[i]);
        }
        PolygonOptions rectOptions = new PolygonOptions()
                .fillColor(Color.argb(30, 56, 142, 60))
                .strokeColor(Color.parseColor("#4CAF50"))
                .add(lats);
        Polygon polygon = googleMap.addPolygon(rectOptions);
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                try {
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex() - 2; i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                } catch (Exception e) {
                    strReturnedAddress = new StringBuilder("");
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                }
                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        selected = strAdd;
        return strAdd;
    }

    @Override
    public void onUpdateMapAfterUserInterection() {
        String res = getCompleteAddressString(googleMap.getCameraPosition().target.latitude,
                googleMap.getCameraPosition().target.longitude);
        ((TextView) findViewById(R.id.editText3)).setText(res);
        selected_lat_lng_final = new LatLng(googleMap.getCameraPosition().target.latitude,
                googleMap.getCameraPosition().target.longitude);
        selected_final = res;
    }

    private boolean isPointInPolygon(LatLng tap, ArrayList<LatLng> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
                intersectCount++;
            }
        }

        return ((intersectCount % 2) == 1); // odd = inside, even = outside;
    }

    private boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {

        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY)
                || (aX < pX && bX < pX)) {
            return false; // a and b can't both be above or below pt.y, and a or
            // b must be east of pt.x
        }

        double m = (aY - bY) / (aX - bX); // Rise over run
        double bee = (-aX) * m + aY; // y = mx + b
        double x = (pY - bee) / m; // algebra is neat!

        return x > pX;
    }
}
