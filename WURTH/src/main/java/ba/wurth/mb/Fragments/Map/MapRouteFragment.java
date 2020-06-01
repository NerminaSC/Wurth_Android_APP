package ba.wurth.mb.Fragments.Map;

import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
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

import ba.wurth.mb.Activities.Routes.RouteActivity;
import ba.wurth.mb.Classes.CustomNumberFormat;
import ba.wurth.mb.Classes.GPS.DirectionsJSONParser;
import ba.wurth.mb.Classes.Objects.Route;
import ba.wurth.mb.Classes.wurthMB;
import ba.wurth.mb.DataLayer.Routes.DL_Routes;
import ba.wurth.mb.Fragments.Routes.RouteInfoFragment;
import ba.wurth.mb.R;

public class MapRouteFragment extends Fragment  {

    private SupportMapFragment fragment;
    private GoogleMap map;
    private Button btnRoute;
    private Button btnOptimized;

    private TextView lit_total_distance;
    private TextView lit_total_time;

    ArrayList<LatLng> markerPoints;

    private boolean optimized = false;

    private JSONArray clients;
    private JSONArray optimizedData;

    private JSONArray waypoint_order;
    private JSONObject bounds;

    private Route mRoute;
    private Integer location_type = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markerPoints = new ArrayList<LatLng>();
        mRoute = ((RouteActivity) getActivity()).mRoute;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnOptimized = (Button) getView().findViewById(R.id.btnOptimized);
        btnRoute = (Button) getView().findViewById(R.id.btnRoute);

        lit_total_distance = getView().findViewById(R.id.lit_total_distance);
        lit_total_time = getView().findViewById(R.id.lit_total_time);

        getView().findViewById(R.id.llActions).setVisibility(View.VISIBLE);

        bindListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            FragmentManager fm = getChildFragmentManager();
            fragment = (SupportMapFragment) fm.findFragmentById(R.id.mapview);

            if (fragment == null) {
                fragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.mapview, fragment).commit();
            }        }
        catch (Exception ex) {
            wurthMB.AddError("MapRouteFragment", ex.getMessage(), ex);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            mRoute = ((RouteActivity) getActivity()).mRoute;

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
        catch (Exception ex) {
            wurthMB.AddError("MapRouteFragment", ex.getMessage(), ex);
        }
    }

    public void showPins() {
       try {

           map.clear();
           markerPoints.clear();

           if (mRoute != null) {

               JSONObject data = new JSONObject(mRoute.raw);

               if(data.has("total_distance")) lit_total_distance.setText(CustomNumberFormat.GenerateFormat(data.getDouble("total_distance")) + " km");
               if(data.has("total_time")) lit_total_time.setText(data.getString("total_time"));

               clients = data.getJSONArray("nodes");

               for (int i = 0; i < clients.length(); i++) {
                   JSONObject jsonObject = clients.getJSONObject(i);

                   if (jsonObject.getDouble("latitude") == 0 || jsonObject.getDouble("longitude") == 0) continue;

                    // Adding new item to the ArrayList
                   LatLng point = new LatLng(jsonObject.getDouble("latitude") / 10000000, jsonObject.getDouble("longitude") / 10000000);
                   markerPoints.add(point);

                   // Creating MarkerOptions
                   MarkerOptions options = new MarkerOptions();

                   // Setting the position of the marker
                   options.position(point);

                   int resID = getResources().getIdentifier("icon" + Integer.toString(i + 1), "drawable", "ba.wurth.mb");
                   options.icon(BitmapDescriptorFactory.fromResource(resID));

                    options.title(jsonObject.getString("name"));

                   // Add new marker to the Google Map Android API V2
                   map.addMarker(options);
               }
           }
       }
       catch (Exception ex) {

       }
    }

    private void bindData() {
        try {

            // Enable MyLocation Button in the Map
            map.setMyLocationEnabled(true);

            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

                @Override
                public void onMapLongClick(LatLng point) {
                    map.clear();
                    markerPoints.clear();
                }
            });

            showPins();
        }
        catch (Exception ex) {

        }
    }

    private void bindListeners() {
        try {

            btnRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(markerPoints.size() >= 2){
                        setPolyline();
                    }
                }
            });

            btnOptimized.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(markerPoints.size() >= 2){

                        try{
                            final Dialog dialog = new Dialog(getContext(), R.style.CustomDialog);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.alert_options);
                            dialog.setCancelable(false);

                            ((TextView) dialog.findViewById(R.id.Title)).setText(R.string.Notification_AttentionNeeded);
                            ((TextView) dialog.findViewById(R.id.text)).setText(R.string.Notification_StartLocation);

                            ((Button) dialog.findViewById(R.id.dialogButtonOK)).setText(R.string.UserLocation);
                            ((Button) dialog.findViewById(R.id.dialogButtonCANCEL)).setText(R.string.CurrentLocation);

                            dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {

                                    location_type = 2;
                                    OptimizeTask optimizeTask = new OptimizeTask();
                                    optimizeTask.execute("");

                                    dialog.dismiss();
                                }
                            });

                            dialog.findViewById(R.id.dialogButtonCANCEL).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {

                                    location_type = 1;
                                    OptimizeTask optimizeTask = new OptimizeTask();
                                    optimizeTask.execute("");

                                    dialog.dismiss();
                                }
                            });

                            dialog.show();

                        }catch (Exception e){

                        }
                    }
                }
            });
        }
        catch (Exception ex) {

        }
    }

    private class OptimizeTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getView() != null) getView().findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service

            String data = "";

            try {
                DL_Routes.OptimizeRoute(mRoute._id, location_type);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                if (getView() != null)  getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);

                mRoute = DL_Routes.GetByID(mRoute._id);

                ((RouteActivity)getActivity()).refreshMyData();

                bindData();

                ParserTask parserTask = new ParserTask();

                // Invokes the thread for parsing the JSON data

            }catch (Exception e){

            }
        }
    }



    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for(int i = 1; i < markerPoints.size() - 1; i++){
            LatLng point  = markerPoints.get(i);
            if (i == 1) {
                if (optimized) waypoints = "waypoints=optimize:true|";
                else waypoints = "waypoints=";
            }
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {

        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
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

        }
        catch (Exception e){
            e.printStackTrace();
        } finally {
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

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }
            catch (Exception e){
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

                waypoint_order = jObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order");
                bounds = jObject.getJSONArray("routes").getJSONObject(0).getJSONObject("bounds");

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            try {

                // Traversing through all the routes
                for(int i=0; i < result.size(); i++){
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for(int j=0; j < path.size(); j++){
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

                if (optimized && waypoint_order != null) {

                    optimizedData = new JSONArray();

                    optimizedData.put(clients.getJSONObject(0));

                    for (int i = 0; i < waypoint_order.length(); i++) {
                        optimizedData.put(clients.getJSONObject(waypoint_order.getInt(i) + 1));
                    }

                    optimizedData.put(clients.getJSONObject(clients.length() - 1));

                    for (int i = 0; i < optimizedData.length(); i++) {
                        JSONObject jsonObject = optimizedData.getJSONObject(i);

                        // Adding new item to the ArrayList
                        LatLng point = new LatLng(jsonObject.getDouble("Latitude"), jsonObject.getDouble("Longitude"));

                        // Creating MarkerOptions
                        MarkerOptions options = new MarkerOptions();

                        // Setting the position of the marker
                        options.position(point);

                        if (i == 0){
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icongreen));
                        }
                        else if (i == optimizedData.length() - 1){
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iconred));
                        }
                        else {
                            int resID = getResources().getIdentifier("icon" + Integer.toString(i), "drawable", "ba.wurth.mb");
                            options.icon(BitmapDescriptorFactory.fromResource(resID));
                        }

                        // Add new marker to the Google Map Android API V2
                        map.addMarker(options);
                    }

                    //((RouteInfoFragment) ((RouteActivity) getActivity()).mAdapter.getItem(0)).data = optimizedData;
                    ((RouteInfoFragment) ((RouteActivity) getActivity()).mAdapter.getItem(0)).bindList();
                }
                else {

                    for (int i = 0; i < clients.length(); i++) {
                        JSONObject jsonObject = clients.getJSONObject(i);

                        // Adding new item to the ArrayList
                        LatLng point = new LatLng(jsonObject.getDouble("Latitude"), jsonObject.getDouble("Longitude"));

                        // Creating MarkerOptions
                        MarkerOptions options = new MarkerOptions();

                        // Setting the position of the marker
                        options.position(point);

                        if (i == 0){
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icongreen));
                        }
                        else if (i == clients.length() - 1){
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iconred));
                        }
                        else {
                            int resID = getResources().getIdentifier("icon" + Integer.toString(i), "drawable", "ba.wurth.mb");
                            options.icon(BitmapDescriptorFactory.fromResource(resID));
                        }

                        // Add new marker to the Google Map Android API V2
                        map.addMarker(options);
                    }

                    //((RouteInfoFragment) ((RouteActivity) getActivity()).mAdapter.getItem(0)).data = data;
                    ((RouteInfoFragment) ((RouteActivity) getActivity()).mAdapter.getItem(0)).bindList();
                }

                if (bounds != null) {
                    LatLngBounds.Builder _bounds = new LatLngBounds.Builder();
                    _bounds.include(new LatLng(bounds.getJSONObject("northeast").getDouble("lat"), bounds.getJSONObject("northeast").getDouble("lng")));
                    _bounds.include(new LatLng(bounds.getJSONObject("southwest").getDouble("lat"), bounds.getJSONObject("southwest").getDouble("lng")));
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(_bounds.build(), 50));
                }
            }
            catch (Exception ex) {

            }

            if (getView() != null) getView().findViewById(R.id.progressContainer).setVisibility(View.GONE);
        }
    }


    private void setPolyline() {
        List<LatLng> coordinates = new ArrayList<>();

        if (mRoute != null) {

            try {
                JSONObject data = new JSONObject(mRoute.raw);

                if(data.has("polyline")){

                    for (int i = 0; i < data.getJSONArray("polyline").length(); i++) {
                        coordinates.add(new LatLng(data.getJSONArray("polyline").getJSONObject(i).getDouble("lat"), data.getJSONArray("polyline").getJSONObject(i).getDouble("lng")));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        PolylineOptions polyline = new PolylineOptions();
        polyline.addAll(coordinates);
        polyline.width(3);
        polyline.color(Color.BLUE);
        map.addPolyline(polyline);
    }

}
