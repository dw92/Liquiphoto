package pl.dw92.liquiphoto;

import android.graphics.Point;
import android.util.Log;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightType;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Light {

    private Point position;
    private LightPoint light;
    private int currentBrightness;
    private String id;

    public Light(LightPoint light, Point position) {
        this.light = light;
        this.position = position;
        this.currentBrightness = getBrightness();
        this.id = light.getIdentifier();
    }

    public LightPoint getLightPoint() {
        return light;
    }

    public String getName() {
        return light.getName();
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public String getId() {
        return id;
    }


    public void turnOn() {
        LightState newLightState = new LightState();
        newLightState.setOn(TRUE);

        updateState(newLightState);
    }

    public void turnOff() {
        LightState newLightState = new LightState();
        newLightState.setOn(FALSE);

        updateState(newLightState);
    }

    public void setColor(HueColor color) {
        if (light.getLightType() == LightType.COLOR || light.getLightType() == LightType.EXTENDED_COLOR) {
            LightState newLightState = new LightState();
            newLightState.setXYBWithColor(color);

            updateState(newLightState);
        }
    }

    public int getBrightness() {
        LightState lightState = light.getLightState();
        return lightState.getBrightness();
    }

    public void setBrightness(int x) {
        LightState lightState = light.getLightState();
       // int brightness = lightState.getBrightness();

        System.out.println("old brightness: " + currentBrightness);


        final LightState newLightState = new LightState();
        int newBrightness = currentBrightness + x;
        if (newBrightness > 255) {
            newBrightness = 255;
        }
        if (newBrightness < 0) {
            newBrightness = 0;
        }
        newLightState.setBrightness(newBrightness);
        currentBrightness = newBrightness;

        System.out.println("new brightness: " + newBrightness);
        updateState(newLightState);
     /*   light.updateState(newLightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                if (returnCode == ReturnCode.SUCCESS) {
                    Log.i("Liquiphoto", "Changed brightness of light " + light.getName() + " to " + newLightState.getBrightness());
                } else {
                    Log.e("Liquiphoto", "Error changing hue of light " + light.getIdentifier());
                    for (HueError error : errorList) {
                        Log.e("Liquiphoto", error.toString());
                    }
                }
            }
        });*/
    }

    private void updateState(final LightState lightState) {
        light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                if (returnCode == ReturnCode.SUCCESS) {
                    //Log.i("Liquiphoto", "Changed brightness of light " + light.getName() + " to " + lightState.getBrightness());
                } else {
                    Log.e("Liquiphoto", "Error changing hue of light " + light.getIdentifier());
                    for (HueError error : errorList) {
                        Log.e("Liquiphoto", error.toString());
                    }
                }
            }
        });
    }
}
