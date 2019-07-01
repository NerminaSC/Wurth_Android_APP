package ba.wurth.mb.Fragments.Map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ba.wurth.mb.Classes.GPS.DirectionsJSONParser;
import ba.wurth.mb.R;

public class MapFragment extends Fragment  {

    private SupportMapFragment fragment;
    private GoogleMap map;
    private Button btnRoute;
    private Button btnOptimized;
    ArrayList<LatLng> markerPoints;
    private boolean optimized = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnOptimized = (Button) getView().findViewById(R.id.btnOptimized);
        btnRoute = (Button) getView().findViewById(R.id.btnRoute);

        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markerPoints.size() >= 2){

                    optimized = false;
                    //map.clear();

                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }
            }
        });

        btnOptimized.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markerPoints.size() >= 2){

                    optimized = true;
                    //map.clear();



                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }
            }
        });

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markerPoints = new ArrayList<LatLng>();

        /*if (getArguments() != null) {
            ClientID = getArguments().getLong("ClientID", 0L);
            DeliveryPlaceID = getArguments().getLong("DeliveryPlaceID", 0L);
            OrderStatusID = getArguments().getInt("OrderStatusID", 0);
        }*/
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.mapview);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.mapview, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (map == null) {
            fragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44, 17.5), 8));
                    bindData();
                }
            });
        }
    }

    private void bindData() {
        try {


            // Setting onclick event listener for the map
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {

                    // Already 10 locations with 8 waypoints and 1 start location and 1 end location.
                    // Upto 8 waypoints are allowed in a query for non-business users
                    if(markerPoints.size()>=10){
                        return;
                    }

                    // Adding new item to the ArrayList
                    markerPoints.add(point);

                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker
                    options.position(point);

                    /**
                     * For the start location, the color of marker is GREEN and
                     * for the end location, the color of marker is RED and
                     * for the rest of markers, the color is AZURE
                     */
                    if(markerPoints.size() == 1){
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icongreen));
                    }
                    else if(markerPoints.size() == 2){
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iconred));
                    }
                    else{
                        int resID = getResources().getIdentifier("icon" + Integer.toString(markerPoints.size() - 2), "drawable", "ba.wurth.mb");
                        options.icon(BitmapDescriptorFactory.fromResource(resID));
                    }

                    // Add new marker to the Google Map Android API V2
                    map.addMarker(options);
                }
            });

            // The map will be cleared on long click
            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                @Override
                public void onMapLongClick(LatLng point) {
                    // Removes all the points from Google Map
                    map.clear();

                    // Removes all the points in the ArrayList
                    markerPoints.clear();
                }
            });
        }
        catch (Exception ex) {

        }
    }


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for(int i=2; i < markerPoints.size(); i++){
            LatLng point  = markerPoints.get(i);
            if(i==2) {
                if (optimized) waypoints = "waypoints=optimize:true|";
                else waypoints = "waypoints=";
            }
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getView() != null) getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
        }

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service

            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                e.printStackTrace();
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points ;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0; i<result.size(); i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.RED);
            }

            map.clear();

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);

            int i = 0;
            for (LatLng item: markerPoints) {
                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(item);

                if(i == 0){
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icongreen));
                }
                else if (i == 1) {
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iconred));
                }
                else {
                    int resID = getResources().getIdentifier("icon" + Integer.toString(i - 1), "drawable", "ba.wurth.mb");
                    options.icon(BitmapDescriptorFactory.fromResource(resID));
                }

                // Add new marker to the Google Map Android API V2
                map.addMarker(options);
                i++;
            }

            if (getView() != null) getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
        }
    }
}
