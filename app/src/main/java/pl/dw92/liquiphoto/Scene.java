package pl.dw92.liquiphoto;


import android.graphics.Point;

import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

import java.util.ArrayList;
import java.util.List;

public class Scene  {

    private int width;
    private int height;
    private Point centre;
    private Point centroid;
    private List<Light> lights;


    private final int BRIGHT_INCREMENT = 15;


    public Scene(int width, int height) {
        this.lights = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.centre = new Point(width/2, height/2);
        this.centroid = calcCentroid();
    }

    public void addLight(Light light) {
        lights.add(light);
    }

    public void removeLight(Light light) {
        lights.remove(light);
    }

    public Light getLight(String id) {
        for (Light light : lights) {
            if (light.getId().equals(id)) {
                return light;
            }
        }
        return null;
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

    public void setColor(HueColor color) {
        for (final Light light : lights) {
            light.setColor(color);
        }
    }

    public void setDimensions(int width, int height) {
        if (this.width == 0 || this.height == 0) {
            this.width = width;
            this.height = height;
            this.centre = new Point(width / 2, height / 2);
            this.centroid = calcCentroid();
        }
    }

    public void updateScene(float x, float z) {

        for (final Light light : lights) {
            float brightness = 0;
            Point position = light.getPosition();
            System.out.println("Scene dimensions: "+ width + ", "+ height);
            System.out.println("SensorX: "+x);
            System.out.println("SensorZ: "+z);
            System.out.println("Light name: "+ light.getName());
            System.out.println("Light position: "+ position.x + ", "+ position.y);
            System.out.println("Centre position: "+ centre.x + ", "+ centre.y);

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
            light.setBrightness(normalizedBrightness);
        }
        //calcCentroid();
    }

    public List<Light> getLights() {
        return lights;
    }

    public Point getCentroid() {
        return centroid;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private Point calcCentroid() {
        if (lights.isEmpty()) {
            return centre;
        }
        int x = 0;
        int y = 0;
        int weights = 0;

        for (final Light light : lights) {
            Point position = light.getPosition();
            int brightness = light.getBrightness();

            x += position.x * brightness;
            y += position.y * brightness;
            weights += brightness;
        }
        return new Point(x / weights, y / weights);
    }
}
