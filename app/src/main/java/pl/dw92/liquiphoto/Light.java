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
    private String id;
    private int currentBrightness;
    private boolean isOn;

    Light(LightPoint light, Point position) {
        this.light = light;
        this.position = position;
        this.currentBrightness = getBrightness();
        this.isOn = false;
        this.id = light.getIdentifier();
    }


    public String getName() {
        return light.getName();
    }


    public Point getPosition() {
        return position;
    }


    public void setPosition(Point position) {
        this.position = position;
    }


    public String getId() {
        return id;
    }


    public boolean isOn() {
        return isOn;
    }


    public boolean isReachable() {
        return light.getLightState().isReachable();
    }


    public void turnOn() {
        LightState newLightState = new LightState();
        newLightState.setOn(TRUE);

        updateState(newLightState);
        isOn = true;
    }


    public void turnOff() {
        LightState newLightState = new LightState();
        newLightState.setOn(FALSE);

        updateState(newLightState);
        isOn = false;
    }


    public void setColor(int color) {
        if (light.getLightType() == LightType.COLOR || light.getLightType() == LightType.EXTENDED_COLOR) {
            int[] colors = {color};
            double[][] convertedColor = HueColor.bulkConvertToXY(colors , light);

            LightState newLightState = new LightState();
            newLightState.setXY(convertedColor[0][0], convertedColor[0][1]);
            updateState(newLightState);
        }
    }


    public int getBrightness() {
        if (currentBrightness < 0) {
            LightState lightState = light.getLightState();
            return lightState.getBrightness();
        }
        return currentBrightness;
    }


    public void setBrightness(int brightness) {
        final LightState newLightState = new LightState();
        if (brightness > 255) {
            brightness = 255;
        }
        if (brightness < 0) {
            brightness = 0;
        }
        newLightState.setBrightness(brightness);
        currentBrightness = brightness;

        updateState(newLightState);
    }


    private void updateState(final LightState lightState) {
        light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
                if (returnCode != ReturnCode.SUCCESS) {
                    Log.e("Liquiphoto", "Error updating light: " + light.getIdentifier());
                    for (HueError error : errorList) {
                        Log.e("Liquiphoto", error.toString());
                    }
                }
            }
        });
    }
}
