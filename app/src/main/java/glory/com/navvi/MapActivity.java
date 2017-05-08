package glory.com.navvi;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    private static final String TAG = "MapActivity";

    static final LatLng AlgorismOffice=new LatLng(6.441337, 3.417991);
    private GoogleMap gMap;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    Double lat;
    Double lng;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    Button getDirections;
    LocationRequest mLocationRequest;
    String mLastUpdateTime;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    String API_KEY="AIzaSyBCTlI-WNiKq8EKjfDJPdA8iph1l0rQzwc";

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Where do you want to go?");
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("NG")
                .build();
        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                getUrl(loc, place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                gMap.setMyLocationEnabled(true);
            }
        }
        else{
            gMap.setMyLocationEnabled(true);
        }

        // Add a marker in Algorism or current location, and move the camera.
        if(lat==null&&lng==null) {
            gMap.addMarker(new MarkerOptions().position(AlgorismOffice).title("Algorism"));
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(AlgorismOffice, 16));
        }
        /*else{
            LatLng loc = new LatLng(lat, lng);
            gMap.addMarker(new MarkerOptions().position(loc).title("Algorism"));
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
        }*/
    }

    @Override
    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else{
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    protected void checkLocationServices() {
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                PendingResult<LocationSettingsResult> result=LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                                builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        final LocationSettingsStates state= result.getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can
                                // initialize location requests here.
                                startLocationUpdates();
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(
                                            MapActivity.this,
                                            0x1);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way
                                // to fix the settings so we won't show the dialog.
                                Log.d(TAG, "checkLocationServices() called. Settings Change Unavailable.");
                                Toast.makeText(getApplicationContext(), "Unable to satisfy Location Settings.", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }
        }
        else{
            PendingResult<LocationSettingsResult> result=LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                    builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state= result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location requests here.

                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        MapActivity.this,
                                        0x1);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            Log.d(TAG, "checkLocationServices() called. Settings Change Unavailable.");
                            Toast.makeText(getApplicationContext(), "Unable to satisfy Location Settings.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }

        Log.d(TAG, "checkLocationServices() called. See if we get here.");

    }

    //@Override
    public void onConnected(Bundle connectionHint) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
            }
        }
        else{
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        }
        if (mCurrentLocation != null) {
            if(gMap!=null){
                LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                //gMap.addMarker(new MarkerOptions().position(loc).title("You are here"));
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
            }
        }
        checkLocationServices();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            double lat = mCurrentLocation.getLatitude();
            double lng = mCurrentLocation.getLongitude();

            if(gMap!=null){
                LatLng loc = new LatLng(lat, lng);
                //gMap.addMarker(new MarkerOptions().position(loc).title("You are here"));
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
            }
        }
        else {
            Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }


    @Override
    public void onConnectionFailed(ConnectionResult result){
        Log.d(TAG, "onConnectionFailed() called. Trying to reconnect.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        Toast.makeText(getApplicationContext(), "onConnectionSuspended() called. Trying to reconnect.", Toast.LENGTH_SHORT).show();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                getUrl(loc, place);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }*/

    private void getUrl(LatLng origin, Place dest) {
        GeoApiContext context = new GeoApiContext().setApiKey(API_KEY);

        DirectionsApiRequest apiRequest = DirectionsApi.newRequest(context);
        apiRequest.origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude));
        apiRequest.destination(new com.google.maps.model.LatLng(dest.getLatLng().latitude, dest.getLatLng().longitude));
        apiRequest.mode(TravelMode.DRIVING); //set travelling mode

        apiRequest.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                DirectionsRoute[] routes = result.routes;
                DrawLines drawLines=new DrawLines();
                drawLines.execute(routes);
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });

        /*try {
            apiRequest.await();
        }
        catch (ApiException |InterruptedException|IOException e) {
            // Ignore the error.
        }*/
    }

    private class DrawLines extends AsyncTask<DirectionsRoute, Void, PolylineOptions> {
        @Override
        protected PolylineOptions doInBackground(DirectionsRoute... routes) {
            //DirectionsRoute[] routes = result.routes;
            PolylineOptions options=new PolylineOptions();
            if (routes != null) {
                ArrayList<LatLng> points = new ArrayList<>();
                for (DirectionsRoute dr : routes) {
                    List<com.google.maps.model.LatLng> ltls = dr.overviewPolyline.decodePath();
                    for (com.google.maps.model.LatLng ltl : ltls) {
                        LatLng pos = new LatLng(ltl.lat, ltl.lng);
                        points.add(pos);
                    }
                }
                options.addAll(points);
                options.width(8);
                options.color(Color.BLUE);
                options.visible(true);
            }
            return options;
        }

        @Override
        protected void onPostExecute(PolylineOptions options) {
            gMap.addPolyline(options);
            moveToBounds(options);
        }

    }

    private void moveToBounds(PolylineOptions options){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i < options.getPoints().size();i++){
            builder.include(options.getPoints().get(i));
        }

        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels

        gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

}
