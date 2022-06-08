package com.example.lab1_jinesh_patel_c0851605;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Home extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {

    GoogleMap map;

    List<Marker> arrMarkers = new ArrayList<>();
    List<LatLng> arrLatLong = new ArrayList<>();

    Polygon polygon = null;
    TextView lblDistance;

    SeekBar seekLineColor, seekPolyColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        lblDistance = findViewById(R.id.lblDistance);
        lblDistance.setVisibility(TextView.INVISIBLE);
        seekLineColor = findViewById(R.id.seekLine);
        seekPolyColor = findViewById(R.id.seekPolyLine);
        lblDistance.setTextSize(30);

        seekLineColor.setOnSeekBarChangeListener(this);
        seekPolyColor.setOnSeekBarChangeListener(this);
        loadMap();

    }

    void loadMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fGoogleMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        if (arrMarkers.size() == 0) {
            LatLngBounds boundsNorthAmerica = new LatLngBounds(new LatLng(43.273909, -127.120020), new LatLng(43.273909, -68.409081));
            int padding = 3;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsNorthAmerica, padding);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    map.moveCamera(cameraUpdate);
                }
            }, 100);
        }

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                for(Marker marker : arrMarkers) {
                    if(Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05) {
                        arrLatLong.remove(marker.getPosition());
                        arrMarkers.remove(marker);

                        drawPolygon();
                        marker.remove();
                        break;
                    }
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Boolean isFound = false;
                for(Marker marker1 : arrMarkers) {
                    if (Math.abs(marker1.getPosition().latitude - marker.getPosition().latitude) < 0.05 && Math.abs(marker.getPosition().longitude - marker.getPosition().longitude) < 0.05) {
                        isFound = true;
                    }
                }
                if (isFound == false) {
                    marker.remove();
                }
                return false;
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                Marker marker = map.addMarker(markerOptions);

                if (arrLatLong.size() < 4) {
                arrLatLong.add(latLng);
                arrMarkers.add(marker);

                    drawPolygon();
                }
            }
        });

        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                Log.d("LINE", polyline.getPoints().toString());
            }
        });

        googleMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                Log.d("added",polygon.toString());
                double total = 0.0;
                for (int i = 0; i < arrLatLong.size(); i++) {
                    if (i == arrLatLong.size() - 1) {
                        total += calculationByDistance(arrLatLong.get(i), arrLatLong.get(0));
                    } else {
                        total += calculationByDistance(arrLatLong.get(i), arrLatLong.get(i+1));
                    }
                }
                Integer totalInInt = Math.toIntExact(Math.round(total));
//                Toast.makeText(Home.this, "Total Distance is :- " + totalInInt + " km", Toast.LENGTH_SHORT).show();
                if (lblDistance.getVisibility() == TextView.INVISIBLE) {
                    lblDistance.setVisibility(TextView.VISIBLE);
                    lblDistance.setText("Total Distance is :- " + totalInInt + " km");
                } else {
                    lblDistance.setVisibility(TextView.INVISIBLE);
                    lblDistance.setText("");
                }
            }
        });
    }

    void drawPolygon() {
        if (arrMarkers.size() >= 2) {
            if(polygon != null) polygon.remove();
            PolygonOptions polygonOptions = new PolygonOptions().addAll(arrLatLong)
                    .clickable(true);
            polygonOptions.clickable(true);
            polygon = map.addPolygon(polygonOptions);


            polygon.setStrokeColor(Color.RED);
            polygon.setFillColor(Color.parseColor("#3500FF00"));
        }
    }

    double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        final int min = 0;
        final int max = 255;
        final int random = new Random().nextInt((max - min) + 1) + min;
        final int random1 = new Random().nextInt((max - min) + 1) + min;

        float[] hsvColor = {random, random, 0};
        hsvColor[2] = 360f * progress / progress;

        float[] hsvColor1 = {random1, 0, random1};
        hsvColor1[1] = 360f * progress / progress;

        switch (seekBar.getId()){
            case R.id.seekLine:
                if(polygon != null) {
                    polygon.setStrokeColor(Color.HSVToColor(hsvColor));
                }
                break;
            case R.id.seekPolyLine:
                if (polygon != null) {
                    polygon.setFillColor(Color.HSVToColor(hsvColor1));
                }
        }
//        if(polygon != null)
//
//            polygon.setStrokeColor(Color.rgb(Red,Green,Blue));
//        if(checkBox.isChecked())
//            polygon.setFillColor(Color.rgb(Red,Green,Blue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}