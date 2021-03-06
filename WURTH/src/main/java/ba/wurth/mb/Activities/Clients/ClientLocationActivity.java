package ba.wurth.mb.Activities.Clients;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ba.wurth.mb.Classes.Notifications;
import ba.wurth.mb.Classes.Objects.Client;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Clients.DL_Clients;
import ba.wurth.mb.DataLayer.Custom.DL_Wurth;
import ba.wurth.mb.R;

public class ClientLocationActivity extends AppCompatActivity {

    private Long ClientID = 0L;
    private Long Latitude = 0L;
    private Long Longitude = 0L;

    private GoogleMap mMap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deliveryplace_location);

        try {

            if (getIntent().hasExtra("ClientID")) {
                ClientID = getIntent().getLongExtra("ClientID", 0L);
                Client c = DL_Wurth.GET_Client(ClientID);
                if (c != null) {
                    getSupportActionBar().setTitle(c.Name);
                    getSupportActionBar().setSubtitle(c.code);
                }
            }
            else {
                getSupportActionBar().setTitle("");
                getSupportActionBar().setSubtitle("");
            }

        }
        catch (Exception ex) {
            wurthMB.AddError("ClientLocationActivity", ex.getMessage(), ex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    setUpMap();
                    bindData();
                }
            });
        }
    }

    private void bindData() {
        try {
            findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ClientID > 0L && Longitude > 0L && Latitude > 0L) {
                        DL_Clients.UpdateClientLocation(ClientID, Latitude, Longitude);
                        Notifications.showNotification(ClientLocationActivity.this, "Success", getString(R.string.DatabaseUpdated), 0);
                        finish();
                    }
                }
            });
        }
        catch (Exception ex) {

        }
    }

    private void setUpMap() {
        try {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            LatLng currentLocation = new LatLng(44.5, 17.5);

            if (wurthMB.currentBestLocation != null) {
                currentLocation = new LatLng(wurthMB.currentBestLocation.getLatitude(), wurthMB.currentBestLocation.getLongitude());
            }

            Latitude = (long) (currentLocation.latitude * 10000000);
            Longitude = (long) (currentLocation.longitude * 10000000);

            //mMap.setMyLocationEnabled(true);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }

            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.CurrentLocation))
                    .snippet(getString(R.string.YourCurrentLocation))
                    .draggable(true)
                    .position(currentLocation));

            if (ClientID > 0L) {
                Client client = DL_Clients.GetByClientID(ClientID);
                String name = "";

                Long _Latitude = 0L;
                Long _Longitude = 0L;

                if (client != null) {
                    _Latitude = client.Latitude;
                    _Longitude = client.Longitude;
                    name = client.Name;
                }

                if (_Latitude != 0D && _Longitude != 0D) {

                    LatLng dpLocation = new LatLng(((double) _Latitude) / 10000000D, ((double) _Longitude) / 10000000D);

                    mMap.addMarker(new MarkerOptions()
                            .title(name)
                            .snippet(name)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_target))
                            .position(dpLocation));

                    builder.include(dpLocation);
                }
            }

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Latitude = (long) (marker.getPosition().latitude * 10000000D);
                    Longitude = (long) (marker.getPosition().longitude * 10000000D);
                }
            });

            builder.include(currentLocation);

            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 25, 25, 5));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 7.0f));
        }
        catch (Exception ex) {

        }
    }
}