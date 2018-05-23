package pl.dw92.liquiphoto;


import android.graphics.Color;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Scene  {

    public final static int MAX_BRIGHTNESS = 255;

    private int width;
    private int height;
    private Point centre;
    private List<Light> lights;
    private int color;


    Scene(int width, int height) {
        this.lights = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.centre = new Point(width/2, height/2);
        this.color = Color.parseColor("#FFFDE7");
    }


    public List<Light> getLights() {
        return lights;
    }


    public Light getLight(String id) {
        for (Light light : lights) {
            if (light.getId().equals(id)) {
                return light;
            }
        }
        return null;
    }


    public void addLight(Light light) {
        light.turnOn();
        lights.add(light);
    }


    public void removeLight(Light light) {
        light.turnOff();
        lights.remove(light);
    }


    public boolean isOn() {
        for (Light light : lights) {
            if (light.isOn()) {
                return true;
            }
        }
        return false;
    }


    public void turnOnLights() {
        for (Light light : lights) {
            light.turnOn();
        }
    }


    public void turnOffLights(){
        for (Light light : lights) {
            light.turnOff();
        }
    }


    public void setBrightness(int brightness) {
        for (final Light light : lights) {
            light.setBrightness(brightness);
        }
    }


    public int getColor() {
        return color;
    }


    public void setColor(int color) {
        for (final Light light : lights) {
            light.setColor(color);
        }
        this.color = color;
    }

    public void setDimensions(int width, int height) {
        if (this.width == 0 || this.height == 0) {
            this.width = width;
            this.height = height;
            this.centre = new Point(width / 2, height / 2);
        }
    }

    public void updateScene(float x, float z) {
        final int BRIGHT_INCREMENT = 15;

        for (final Light light : lights) {
            float brightness = 0;
            Point position = light.getPosition();

            if ((x > 0 && position.x < centre.x) || (x < 0 && position.x > centre.x)) {
                brightness += Math.abs(x);
            }
            if ((x > 0 && position.x > centre.x) || (x < 0 && position.x < centre.x)) {
                brightness -= Math.abs(x);
            }

            if ((z > 0 && position.y < centre.y) || (z < 0 && position.y > centre.y)) {
                brightness += Math.abs(z);
            }
            if ((z > 0 && position.y > centre.y) || (z < 0 && position.y < centre.y)) {
                brightness -= Math.abs(z);
            }

            int normalizedBrightness = brightness > 0 ? BRIGHT_INCREMENT : -BRIGHT_INCREMENT;
            light.setBrightness(light.getBrightness()+normalizedBrightness);
        }
    }
}
