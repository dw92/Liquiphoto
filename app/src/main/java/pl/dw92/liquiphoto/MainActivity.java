package pl.dw92.liquiphoto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.IndicatorMode;
import com.pavelsikun.vintagechroma.OnColorSelectedListener;
import com.pavelsikun.vintagechroma.colormode.ColorMode;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeState;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Scene scene;
    private boolean isActive = false;
    private long last;

    private BridgeHandler bridgeHandler;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private Context context;

    private float sensorX;
    private float sensorZ;

    private ImageView drawableScene;
    private FloatingActionButton actionButton;
    private RecyclerView lightRecyclerView;
    private SeekBar brightnessSeekBar;
    private ImageView pickColorImageView;
    private ImageView turnOnLights;
    private ImageView turnOffLights;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        context = getApplicationContext();

        scene = new Scene(0, 0);
        last = System.currentTimeMillis();
        bridgeHandler = BridgeHandler.getInstance();

        LightRecyclerViewAdapter adapter = new LightRecyclerViewAdapter(this, getLights());
        lightRecyclerView = findViewById(R.id.lightRecyclerView);
        lightRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        lightRecyclerView.setAdapter(adapter);
        lightRecyclerView.setOnDragListener(new LightDragListener());

        drawableScene = findViewById(R.id.drawableScene);
        drawableScene.setOnDragListener(new LightDragListener());

        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        brightnessSeekBar.setMax(Scene.MAX_BRIGHTNESS);
        brightnessSeekBar.setProgress(Scene.MAX_BRIGHTNESS/2);
        brightnessSeekBar.setOnSeekBarChangeListener(new BrightnessSeekBarListener());

        pickColorImageView = findViewById(R.id.pickColorImageView);

        actionButton = findViewById(R.id.actionButton);
        actionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    isActive = true;

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    isActive = false;
                }
                return true;
            }
        });

        turnOffLights = findViewById(R.id.turnLightsOffButton);

        turnOnLights = findViewById(R.id.turnOnLightsButton);
        turnOnLights.setEnabled(false);

        disableActions();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        bridgeHandler.getBridge().connect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        bridgeHandler.getBridge().disconnect();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        if (isActive) {
            sensorX = event.values[0];
            sensorZ = event.values[2] - 8;


            final long now = System.currentTimeMillis();

            if (now - last > 100) {
                new Thread(new Runnable() {
                    public void run() {
                        last = now;

                        scene.setDimensions(drawableScene.getWidth(), drawableScene.getHeight());
                        scene.updateScene(sensorX, sensorZ);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void turnOnLights(View view) {
        scene.turnOnLights();
        enableActions();
    }


    public void turnOffLights(View view) {
        scene.turnOffLights();
        disableActions();
    }


    public void setColor(View view) {
        new ChromaDialog.Builder()
                .initialColor(scene.getColor())
                .colorMode(ColorMode.RGB)
                .indicatorMode(IndicatorMode.DECIMAL)
                .onColorSelected(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        scene.setColor(color);
                    }
                })
                .create()
                .show(getSupportFragmentManager(), "Color picker");
    }


    private List<Light> getLights() {
        BridgeState bridgeState = bridgeHandler.getBridge().getBridgeState();
        List<LightPoint> lightPoints = bridgeState.getLights();

        List<Light> lights = new ArrayList<>();

        for (final LightPoint lightPoint : lightPoints) {
            Light light = new Light(lightPoint, new Point(0, 0));
            lights.add(light);
        }
        return lights;
    }


    private void enableActions() {
        brightnessSeekBar.setEnabled(true);
        pickColorImageView.setEnabled(true);
        turnOffLights.setEnabled(true);
        turnOnLights.setEnabled(true);
    }


    private void disableActions() {
        brightnessSeekBar.setEnabled(false);
        pickColorImageView.setEnabled(false);
        turnOffLights.setEnabled(false);

        if (scene.getLights().isEmpty()) {
            turnOnLights.setEnabled(false);
        }
    }




    private class LightDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP: {

                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    View viewSource = (View) event.getLocalState();
                    Light light = null;

                    if (viewSource.getParent().getClass() == android.support.v7.widget.RecyclerView.class) {
                        if (v.getId() == R.id.lightRecyclerView) {
                            break;
                        }
                        RecyclerView source = (RecyclerView) viewSource.getParent();

                        LightRecyclerViewAdapter adapter = (LightRecyclerViewAdapter) source.getAdapter();
                        int positionSource = (int) viewSource.getTag();

                        List<Light> lightsSource = adapter.getLights();
                        light = lightsSource.get(positionSource);
                        light.setPosition(new Point(x, y));

                        scene.addLight(light);
                        enableActions();

                        lightsSource.remove(positionSource);
                        adapter.updateLights(lightsSource);
                        adapter.notifyDataSetChanged();
                    }

                    if (viewSource.getParent().getClass() == android.support.constraint.ConstraintLayout.class) {
                        ConstraintLayout constraintLayout = (ConstraintLayout) viewSource.getParent();

                        light = scene.getLight(Integer.toString(viewSource.getId()));
                        light.setPosition(new Point(x, y));
                        constraintLayout.removeView(viewSource);
                    }


                    if (v.getId() == R.id.drawableScene) {
                        ConstraintLayout layout = (ConstraintLayout) v.getParent();
                        drawLightOnScene(layout, v, x, y, Integer.parseInt(light.getId()), light.getName());
                    }

                    if (v.getId() == R.id.lightRecyclerView) {
                        light = scene.getLight(Integer.toString(viewSource.getId()));
                        scene.removeLight(light);

                        if (!scene.isOn()) {
                            disableActions();
                        }

                        RecyclerView target = v.getRootView().findViewById(R.id.lightRecyclerView);
                        LightRecyclerViewAdapter adapterTarget = (LightRecyclerViewAdapter) target.getAdapter();
                        List<Light> lights = adapterTarget.getLights();
                        lights.add(light);

                        adapterTarget.updateLights(lights);
                        adapterTarget.notifyDataSetChanged();
                        v.setVisibility(View.VISIBLE);
                    }
                    break;
                }
                case DragEvent.ACTION_DRAG_ENDED: {
                    View viewSource = (View) event.getLocalState();
                    viewSource.setVisibility(View.VISIBLE);
                    break;
                }
                default:
                    break;
            }
            return true;
        }


        private void drawLightOnScene(ConstraintLayout layout, View view, int x, int y, int id, String text) {
            TextView textView = new TextView(context);
            textView.setCompoundDrawablesWithIntrinsicBounds( 0, R.drawable.light, 0, 0 );
            textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            textView.setId(id);
            textView.setText(text);
            textView.setTextColor(Color.DKGRAY);

            layout.addView(textView, 0);
            textView.measure(0, 0);

            ConstraintSet set = new ConstraintSet();
            set.clone(layout);
            set.constrainHeight(textView.getId(), ConstraintSet.WRAP_CONTENT);
            set.constrainWidth(textView.getId(), ConstraintSet.WRAP_CONTENT);
            set.setElevation(textView.getId(), 2.0f);
            set.connect(textView.getId(), ConstraintSet.TOP, view.getId(), ConstraintSet.TOP, (y - textView.getMeasuredHeight()/2));
            set.connect(textView.getId(), ConstraintSet.LEFT, view.getId(), ConstraintSet.LEFT, (x - textView.getMeasuredWidth()/2));
            set.applyTo(layout);

            textView.setVisibility(View.VISIBLE);
            textView.setOnLongClickListener(new LongClickListener());
        }
    }



    private class BrightnessSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            scene.setBrightness(seekBar.getProgress());
        }
    }
}
