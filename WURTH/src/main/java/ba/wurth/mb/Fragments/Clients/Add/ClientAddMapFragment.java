package ba.wurth.mb.Fragments.Clients.Add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import ba.wurth.mb.Activities.Clients.ClientAddActivity;
import ba.wurth.mb.R;

public class ClientAddMapFragment extends SupportMapFragment  {

    public JSONObject mTemp;

    public static ClientAddMapFragment newInstance()
    {
        ClientAddMapFragment frag = new ClientAddMapFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        setUpMap();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemp = ((ClientAddActivity) getActivity()).mTemp;
    }

    private void setUpMap() {


        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                GoogleMap mMap;

                mMap = googleMap;

                UiSettings settings = mMap.getUiSettings();
                settings.setAllGesturesEnabled(true);
                settings.setMyLocationButtonEnabled(true);

                double Latitude = 44.5;
                double Longitude = 17.5;

                try {
                    if (mTemp != null) {
                        if (!mTemp.isNull("Latitude") && mTemp.getDouble("Latitude") > 0D) Latitude = mTemp.getDouble("Latitude");
                        if (!mTemp.isNull("Latitude") && mTemp.getDouble("Longitude") > 0D) Longitude = mTemp.getDouble("Longitude");
                    }
                } catch (JSONException e) {
                }

                mMap.addMarker(new MarkerOptions()
                        .title(getString(R.string.CurrentLocation))
                        .snippet(getString(R.string.YourCurrentLocation))
                        .draggable(true)
                        .position(new LatLng(Latitude, Longitude)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude, Longitude), 8));

                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        try {
                            if (mTemp == null) mTemp = new JSONObject();
                            mTemp.put("Latitude", marker.getPosition().latitude);
                            mTemp.put("Longitude", marker.getPosition().longitude);
                        } catch (JSONException e) {

                        }
                    }
                });
            }
        });



    }
}
