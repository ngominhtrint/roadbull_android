package sg.tringo.roadbull;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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

import sg.tringo.roadbull.model.Place;
import sg.tringo.roadbull.parser.DataParser;
import sg.tringo.roadbull.views.CustomInfoWindowAdapter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;

    private static final String TAG = "MapsActivity";

    private Location mLocation = null;
    private GoogleMap mMap;
    private Polyline mLine;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d(TAG, String.format("%f, %f", location.getLatitude(), location.getLongitude()));
                drawMarker(location);
                mLocationManager.removeUpdates(mLocationListener);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getCurrentLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        initialMarkers();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng origin = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                LatLng dest = marker.getPosition();

                // Getting URL to the Google Directions API
                String url = getUrl(origin, dest);
                Log.d("onMapClick", url.toString());
                FetchUrl FetchUrl = new FetchUrl();

                // Start downloading json data from Google Directions API
                FetchUrl.execute(url);

                return false;
            }
        });
    }

    private void getCurrentLocation() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            if (!(isGPSEnabled || isNetworkEnabled)) {
                Toast.makeText(this, R.string.error_location_provider, Toast.LENGTH_SHORT).show();
            } else {
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (isGPSEnabled) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }

        if (mLocation != null) {
            Log.d(TAG, String.format("getCurrentLocation(%f, %f)", mLocation.getLatitude(),
                    mLocation.getLongitude()));
            drawMarker(mLocation);
        }
    }

    private void drawMarker(Location location) {
        if (mMap != null) {
            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(gps)
                    .title("Current mLocation")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
            mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 18));
        }
    }

    private void initialMarkers() {

        LatLng yongKangGeo = new LatLng(1.3597017,103.8929297);
        Place yongKang = new Place("Yong Kang Restaurant", "1 Greenwich Dr, Singapore", "149544", "120", "Please go to Reception and take the parcel from Lyon", yongKangGeo);

        LatLng happyHawkerGeo = new LatLng(1.3836701,103.8950322);
        Place happyHawker = new Place("Happy Hawker", "267B Compassvale Link, Singapore", "149544", "120", "Please go to Reception and take the parcel from Lyon", happyHawkerGeo);

        LatLng oceanSeafoodGeo = new LatLng(1.3779264,103.8918263);
        Place oceanSeafood = new Place("Ocean Seafood", "435A Hougang Ave 8 #01-01, Singapore 531435", "149544", "120", "Please go to Reception and take the parcel from Lyon", oceanSeafoodGeo);

        LatLng sevenStarsGeo = new LatLng(1.3648079,103.8885655);
        Place sevenStars = new Place("7 Stars Coffee Shop", "23 Hougang Ave 3, Block 23, Singapore 530023", "195070", "10", "Please go to Reception and take the parcel from Monkey", sevenStarsGeo);

        LatLng ikeaTampinesGeo = new LatLng(1.3743002,103.9299968);
        Place ikeaTampines = new Place("IKEA Tampines", "60 Tampines North Drive 2, Singapore 528764", "105070", "140", "Please go to Reception and take the parcel from Monkey", ikeaTampinesGeo);

        List<Place> places = new ArrayList<>();
        places.add(yongKang);
        places.add(happyHawker);
        places.add(oceanSeafood);
        places.add(sevenStars);
        places.add(ikeaTampines);

        //Set Custom InfoWindow Adapter
        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(MapsActivity.this, places);
        mMap.setInfoWindowAdapter(adapter);

        for (Place place : places) {
            //Marker
            MarkerOptions markerOpt = new MarkerOptions();
            markerOpt.position(place.getGeo());
            mMap.addMarker(markerOpt);
        }

        if (mLocation != null) {
            drawMarker(mLocation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 15));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yongKang.getGeo(), 15));
        }
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String strDest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = strOrigin + "&" + strDest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
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

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if (mLine == null) {
                    mLine = mMap.addPolyline(lineOptions);
                } else {
                    mLine.remove();
                    mLine = mMap.addPolyline(lineOptions);
                }
            } else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
