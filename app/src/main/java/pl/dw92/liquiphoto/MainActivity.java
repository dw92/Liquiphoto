package pl.dw92.liquiphoto;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
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
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.domain.BridgeState;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener, LightRecyclerViewAdapter.ItemClickListener {

    private static Scene scene;
    private static Boolean isActive = false;

    private BridgeHandler bridgeHandler;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private static LayoutInflater layoutInflater;
    private static Context context;

    private static float sensorX;
    private static float sensorZ;

    private FloatingActionButton actionButton;
    private RecyclerView lightRecyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        context = getApplicationContext();

        layoutInflater = LayoutInflater.from(getApplicationContext());
        scene = new Scene(0, 0);

        bridgeHandler = BridgeHandler.getInstance();

        LightRecyclerViewAdapter adapter = new LightRecyclerViewAdapter(this, getLights());
        adapter.setClickListener(this);
        lightRecyclerView = findViewById(R.id.lightRecyclerView);
        lightRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        lightRecyclerView.setAdapter(adapter);
        lightRecyclerView.setOnDragListener(new LightDragListener());


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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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


    public void turnOnLights(View view) {
        scene.turnOnLights();
    }


    public void turnOffLights(View view) {
        scene.turnOffLights();
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
            sensorZ = event.values[2] - 5;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onItemClick(View view, int position) {
       // Log.i("TAG", "You clicked number " + adapter.getItem(position) + ", which is at cell position " + position);
    }



    public static class LightDragListener implements View.OnDragListener {

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
                case DragEvent.ACTION_DROP:

                    int x = (int)event.getX();
                    int y = (int)event.getY();

                    View viewSource = (View) event.getLocalState();
                    Light light = null;

                    if ( viewSource.getParent().getClass() == android.support.v7.widget.RecyclerView.class) {
                        System.out.println("ID: "+ v.getId());

                        if (v.getId() == R.id.lightRecyclerView) {
                            break;
                        }
                        RecyclerView source = (RecyclerView) viewSource.getParent();

                        LightRecyclerViewAdapter adapter = (LightRecyclerViewAdapter) source.getAdapter();
                        int positionSource = (int) viewSource.getTag();

                        List<Light> lightsSource = (List<Light>) adapter.getLights();
                        light = lightsSource.get(positionSource);
                        light.setPosition(new Point(x, y));
                        scene.addLight(light);

                        lightsSource.remove(positionSource);
                        adapter.updateLights(lightsSource);
                        adapter.notifyDataSetChanged();
                    }

                    if (viewSource.getParent().getClass() == android.support.constraint.ConstraintLayout.class) {
                        ConstraintLayout constraintLayout = (ConstraintLayout) viewSource.getParent();
                        System.out.println(Integer.toString(viewSource.getId()));
                        light = scene.getLight(Integer.toString(viewSource.getId()));
                        light.setPosition(new Point(x, y));
                        constraintLayout.removeView(viewSource);
                    }


                    if (v.getId() == R.id.drawableScene) {
                       /* if (viewSource.getClass() == android.support.constraint.ConstraintLayout.class){
                            System.out.println(Integer.toString(viewSource.getId()));
                            light = scene.getLight(Integer.toString(viewSource.getId()));
                            light.setPosition(new Point(x, y));
                        }*/

                        ConstraintLayout layout = (ConstraintLayout) v.getParent();

                        TextView textView = new TextView(context);
                        textView.setCompoundDrawablesWithIntrinsicBounds( 0,
                                R.drawable.light, 0, 0 );
                        textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                        if(light != null) {
                            textView.setId(Integer.parseInt(light.getId()));
                            textView.setText(light.getName());
                            textView.setTextColor(Color.DKGRAY);
                        }
                        else {
                            textView.setId(viewSource.getId());
                            TextView text = (TextView)viewSource;
                            textView.setText(text.getText());
                            textView.setTextColor(Color.DKGRAY);
                        }

                        ConstraintSet set = new ConstraintSet();
                        layout.addView(textView, 0);
                        set.clone(layout);

                        set.constrainHeight(textView.getId(), ConstraintSet.WRAP_CONTENT);
                        set.constrainWidth(textView.getId(), ConstraintSet.WRAP_CONTENT);
                        set.setHorizontalBias(textView.getId(), 0.5f);
                        set.setVerticalBias(textView.getId(), 0.34f);
                        set.setElevation(textView.getId(), 2.0f);
                        set.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, y +  150);
                        set.connect(textView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, x + 120);
                        set.applyTo(layout);

                        textView.setVisibility(View.VISIBLE);
                        textView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                ClipData data = ClipData.newPlainText("", "");
                                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                                view.startDrag(data, shadowBuilder, view, 0);
                                view.setVisibility(View.INVISIBLE);
                                return true;
                            }
                        });
                    }

                    if (v.getId() == R.id.lightRecyclerView) {
                        System.out.println("Dragged from scene to list, id: "+viewSource.getId());
                        light = scene.getLight(Integer.toString(viewSource.getId()));
                        scene.removeLight(light);

                        RecyclerView target = (RecyclerView)
                                v.getRootView().findViewById(R.id.lightRecyclerView);
                        LightRecyclerViewAdapter adapterTarget = (LightRecyclerViewAdapter) target.getAdapter();
                        List<Light> lights = adapterTarget.getLights();
                        lights.add(light);

                        adapterTarget.updateLights(lights);
                        adapterTarget.notifyDataSetChanged();
                        v.setVisibility(View.VISIBLE);
                    }

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
                default:
                    break;
            }
            return true;
        }

    }

    public static class DrawableScene extends View {

        private long last;
        private Paint scenePaint;
        private Paint pathPaint;
        private Path horizontalPath;
        private Path verticalPath;

        public DrawableScene( Context context) {
            super(context);
            this.last = System.currentTimeMillis();
            this.scenePaint = setPaint(Paint.Style.FILL, Color.parseColor(	"#fffaf0"));
            this.pathPaint = setPaint(Paint.Style.STROKE, Color.DKGRAY);
            this.horizontalPath = new Path();
            this.verticalPath = new Path();

            setOnDragListener(new LightDragListener());
        }

        public DrawableScene(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.last = System.currentTimeMillis();
            this.scenePaint = setPaint(Paint.Style.FILL, Color.parseColor(	"#fffaf0"));
            this.pathPaint = setPaint(Paint.Style.STROKE, Color.DKGRAY);
            this.horizontalPath = new Path();
            this.verticalPath = new Path();

            setOnDragListener(new LightDragListener());
        }

        public DrawableScene(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            this.last = System.currentTimeMillis();
            this.scenePaint = setPaint(Paint.Style.FILL, Color.parseColor(	"#fffaf0"));
            this.pathPaint = setPaint(Paint.Style.STROKE, Color.GREEN);
            this.horizontalPath = new Path();
            this.verticalPath = new Path();

            setOnDragListener(new LightDragListener());
        }

        private Paint setPaint(Paint.Style style, int color) {
            Paint paint = new Paint();
            paint.setStyle(style);
            paint.setColor(color);
            return paint;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            scene.setDimensions(getWidth(), getHeight());
            Rect rect = new Rect(0, 0, getWidth(), getHeight());
            canvas.drawRect(rect, scenePaint);

            horizontalPath.moveTo(0, getHeight() / 2);
            horizontalPath.lineTo(getWidth(), getHeight() / 2);
            canvas.drawPath(horizontalPath, pathPaint);

            verticalPath.moveTo(getWidth() / 2, 0);
            verticalPath.lineTo(getWidth() / 2, getHeight());
            canvas.drawPath(verticalPath, pathPaint);

            final long now = System.currentTimeMillis();

            if (isActive && now - last > 100) {
                last = now;
                scene.updateScene(sensorX, sensorZ);
            }
            invalidate();
        }
    }


}
