package com.pingstersapp.VinylMap;

/**
 * Created by jeyoungchan on 2/11/17.
 */

import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.Map;

import javax.annotation.Nullable;

public class VinylMapManager extends ViewGroupManager<VinylMapModule> {

    private static final String REACT_CLASS = "VinylMap";

    private ReactContext reactContext;

    private final ReactApplicationContext appContext;

    protected GoogleMapOptions googleMapOtions;

    public VinylMapManager(ReactApplicationContext context) {
        this.appContext = context;
        this.googleMapOtions = new GoogleMapOptions();
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected VinylMapModule createViewInstance(ThemedReactContext context) {
        reactContext = context;

        try {
            MapsInitializer.initialize(this.appContext);
        } catch (RuntimeException e) {
            e.printStackTrace();
            emitMapError("Map initialize error", "map_init_error");
        }

        return new VinylMapModule(context, this.appContext.getCurrentActivity(), this, this.googleMapOtions);
    }

    private void emitMapError(String message, String type) {
        WritableMap error = Arguments.createMap();
        error.putString("message", message);
        error.putString("type", type);

        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onError", error);
    }
}