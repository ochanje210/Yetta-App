package com.pingstersapp.VinylMap;

/**
 * Created by jeyoungchan on 2/10/17.
 */

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pingstersapp.R;
import com.pingstersapp.VinylMap.latlnginterpolation.LatLngInterpolator;
import com.pingstersapp.VinylMap.latlnginterpolation.MarkerAnimation;

import java.util.HashMap;
import java.util.Map;

public class VinylMapModule extends MapView implements
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback {
    public static GoogleMap mMap;
    private final VinylMapManager manager;
    private final ThemedReactContext context;
    private Marker nodeMarker;
    private Marker destMarker;
    private HashMap<String, Marker> hashMapRunnerMarker;
    private final Map<Marker, String> markerMap = new HashMap<>();
    private final int baseMapPadding = 50;

    public VinylMapModule(ThemedReactContext reactContext, Context appContext, VinylMapManager manager,
                      GoogleMapOptions googleMapOptions) {
        super(appContext, googleMapOptions);

        this.manager = manager;
        this.context = reactContext;

        hashMapRunnerMarker = new HashMap<>();

        super.onCreate(null);
        super.onResume();
        super.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);

//        marker = this.mMap.addMarker(new MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney")
//                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap())));

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        // mMap.setOnCameraMoveListener(this);
        // mMap.setOnCameraMoveCanceledListener(this);

        this.mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        this.mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                WritableMap event = Arguments.createMap();
                // String _marker_id = markerMap.get(marker);
                event.putString("id", "marker clicked!");
                manager.sendEvent(context, "onMarkerPress", event);
                return false;
            }
        });
        this.mMap.setMyLocationEnabled(true);
        this.mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        /*
         *  The reason can be one of the following:
         *  1. REASON_GESTURE indicates that the camera moved in response to a user's gesture on the
         *      map, such as panning, tilting, pinching to zoom, or rotating the map.
         *  2. REASON_API_ANIMATION indicates that the API has moved the camera in response to a
         *      non-gesture user action, such as tapping the zoom button, tapping the My Location
         *      button, or clicking a marker.
         *  3. REASON_DEVELOPER_ANIMATION indicates that your app has initiated the camera movement.
         */

        WritableMap event = Arguments.createMap();
        event.putString("gesture", Integer.toString(reason));
        manager.sendEvent(context, "onMapMove", event);
    }

    @Override
    public void onCameraMove() {
        // TBD
        Log.d("onCameraMove", "camera center at " + mMap.getCameraPosition().target);
    }

    @Override
    public void onCameraMoveCanceled() {
        // TBD
    }

    @Override
    public void onCameraIdle() {
        LatLng center = mMap.getCameraPosition().target;
        WritableMap event = Arguments.createMap();
        event.putString("lat", Double.toString(center.latitude));
        event.putString("lon", Double.toString(center.longitude));
        manager.sendEvent(context, "onCameraIdle", event);
    }

    private Bitmap getMarkerBitmap() {
        Bitmap markerBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(markerBitmap);
        // todo: resolve the issue: getDrawable is deprecated
        Drawable shape = getResources().getDrawable(R.drawable.shape_marker);
        shape.setBounds(0, 0, markerBitmap.getWidth(), markerBitmap.getHeight());
        shape.draw(canvas);
        return markerBitmap;
    }

    public void animateToLocationHelper(final String latitude, final String longitude) {
        this.mMap.animateCamera(CameraUpdateFactory.newLatLng(
            new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))
        ));
    }

    public void animateToLocationWithZoomHelper(final String latitude, final String longitude, final float zoom) {
        this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), zoom));
    }

    public void updateMarkerHelper(final String latitude, final String longitude) {
//        if (marker != null) {
//            LatLng tmp = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
//            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();
//            MarkerAnimation.animateMarkerToGB(marker, tmp, latLngInterpolator);
//        }
    }

    public void clearMap() {
        if (nodeMarker != null) {
            nodeMarker.remove();
        }
        if (destMarker != null) {
            destMarker.remove();
        }
        mMap.clear();
    }

    public void animateToLocation(final String latitude, final String longitude) {
        if (mMap != null) {
            Handler uiHandler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    animateToLocationHelper(latitude, longitude);
                }
            };
            uiHandler.post(runnable);
        }
    }

    public void animateTolocationWithZoom(final String latitude, final String longitude, final float zoom) {
        if (mMap != null) {
            Handler uiHandler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    animateToLocationWithZoomHelper(latitude, longitude, zoom);
                }
            };
            uiHandler.post(runnable);
        }
    }

    public void addMarkerNode(final String latitude, final String longitude, final String name, final String nodeId, final ReadableArray list) {
        final LatLng nodeLatLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        nodeMarker = mMap.addMarker(new MarkerOptions()
                                        .position(nodeLatLng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.star)));
    }

    public void addMarkerDest(final String latitude, final String longitude, final String name, final String uId) {
        final LatLng destLatLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        destMarker = mMap.addMarker(new MarkerOptions()
                                        .position(destLatLng)
                                        .rotation(45)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dest_marker)));
    }

    public void updateMarker(final String latitude, final String longitude) {
        if (mMap != null) {
            Handler uiHandler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateMarkerHelper(latitude, longitude);
                }
            };
            uiHandler.post(runnable);
        }
    }

    public void fitToCoordinates(final ReadableArray coordinatesArray, final ReadableMap edgePadding, final boolean animated) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Log.d("MAP", "edgePadding: " + edgePadding.toString());
        if (animated) {
            Log.d("MAP", "animated: true");
        } else {
            Log.d("MAP", "animated: false");
        }

        for (int i = 0; i < coordinatesArray.size(); i++) {
            ReadableMap latLng = coordinatesArray.getMap(i);
            Log.d("MAP", latLng.toString());
            Double lat = latLng.getDouble("latitude");
            Double lon = latLng.getDouble("longitude");
            builder.include(new LatLng(lat, lon));
        }

        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, baseMapPadding);

        if (mMap != null) {
            Handler uiHandler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mMap.setPadding(
                            edgePadding.getInt("left"),
                            edgePadding.getInt("top"),
                            edgePadding.getInt("right"),
                            edgePadding.getInt("bottom"));

                    if (animated) {
                        mMap.animateCamera(cu);
                    } else {
                        mMap.moveCamera(cu);
                    }
                    mMap.setPadding(0, 0, 0, 0); // Without this, the Google logo is moved up by the value of edgePadding.bottom
                }
            };
            uiHandler.post(runnable);
        }
    }
}
