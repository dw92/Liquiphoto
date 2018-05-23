package pl.dw92.liquiphoto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Discovery extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Liquiphoto";

    private BridgeHandler bridgeHandler;
    private BridgeDiscovery bridgeDiscovery;
    private List<BridgeDiscoveryResult> bridgeDiscoveryResults;

    private TextView discoveryTextView;
    private Button configureBridgeButton;
    private Button retryDiscoveryButton;
    private ImageView pushlinkImageView;
    private ImageView logoImageView;

    enum UIState {
        Error,
        BridgeDiscoveryRunning,
        BridgeDiscoveryResults,
        Connecting,
        Pushlinking,
        Connected
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        discoveryTextView = findViewById(R.id.discoveryTextView);
        configureBridgeButton = findViewById(R.id.configureBridgeButton);
        configureBridgeButton.setOnClickListener(this);
        retryDiscoveryButton = findViewById(R.id.retryDiscoveryButton);
        retryDiscoveryButton.setOnClickListener(this);
        pushlinkImageView = findViewById(R.id.pushlinkImageView);
        logoImageView = findViewById(R.id.logoImageView);

        bridgeHandler = BridgeHandler.getInstance();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String bridgeIP = getLastUsedBridgeIP();
        if (bridgeIP == null) {
            startBridgeDiscovery();
        }
        else {
            connectToBridge(bridgeIP);
        }
    }


    private String getLastUsedBridgeIP() {
        List<KnownBridge> bridges = KnownBridges.getAll();

        if (bridges.isEmpty()) {
            return null;
        }

        return Collections.max(bridges, new Comparator<KnownBridge>() {
            @Override
            public int compare(KnownBridge a, KnownBridge b) {
                return a.getLastConnected().compareTo(b.getLastConnected());
            }
        }).getIpAddress();
    }


    private void startBridgeDiscovery() {
        disconnectFromBridge();
        bridgeDiscovery = new BridgeDiscovery();
        bridgeDiscovery.search(BridgeDiscovery.BridgeDiscoveryOption.ALL, bridgeDiscoveryCallback);
        updateUI(UIState.BridgeDiscoveryRunning, "Wyszukiwanie dostępnych mostków hue...");
    }


    private void stopBridgeDiscovery() {
        if (bridgeDiscovery != null) {
            bridgeDiscovery.stop();
            bridgeDiscovery = null;
        }
    }


    private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
        @Override
        public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
            bridgeDiscovery = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (returnCode == ReturnCode.SUCCESS) {
                        bridgeDiscoveryResults = results;

                        if (results.isEmpty()) {
                            updateUI(UIState.Error, "Nie znaleziono mostków");
                        }
                        else {
                            updateUI(UIState.BridgeDiscoveryResults, "Znaleziono mostek");
                        }
                    } else if (returnCode == ReturnCode.STOPPED) {
                        Log.i(TAG, "Zatrzymano wyszukiwanie mostków.");
                    } else {
                        updateUI(UIState.Error, "Podczas wyszukiwania mostków wystąpił błąd: " + returnCode);
                    }
                }
            });
        }
    };


    private void connectToBridge(String bridgeIP) {
        stopBridgeDiscovery();
        disconnectFromBridge();

        Bridge bridge = new BridgeBuilder("app name", "device name")
                .setIpAddress(bridgeIP)
                .setConnectionType(BridgeConnectionType.LOCAL)
                .setBridgeConnectionCallback(bridgeConnectionCallback)
                .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                .build();

        bridge.connect();

        bridgeHandler.setBridge(bridge);

        updateUI(UIState.Connecting, "Łączenie z mostkiem...");
    }


    private void disconnectFromBridge() {
        if (bridgeHandler.getBridge() != null) {
            bridgeHandler.getBridge().disconnect();
            bridgeHandler.setBridge(null);
        }
    }


    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
            Log.i(TAG, "Connection event: " + connectionEvent);

            switch (connectionEvent) {
                case LINK_BUTTON_NOT_PRESSED:
                    updateUI(UIState.Pushlinking, "Naciśnij przycisk na mostku w celu uwierzytelnienia.");

                    break;

                case COULD_NOT_CONNECT:
                    updateUI(UIState.Error, "Połączenie nie zostało nawiązane");
                    break;

                case CONNECTION_LOST:
                    updateUI(UIState.Error, "Utracono połączenie. Próba ponownego połączenia.");
                    break;

                case CONNECTION_RESTORED:
                    updateUI(UIState.Connected, "Połączenie wznowione.");
                    break;

                case DISCONNECTED:
                    break;

                default:
                    break;
            }
        }


        @Override
        public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
            for (HueError error : list) {
                Log.e(TAG, "Connection error: " + error.toString());
            }
        }
    };


    private void showMainView() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
        @Override
        public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
            Log.i(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);

            switch (bridgeStateUpdatedEvent) {
                case INITIALIZED:
                    updateUI(UIState.Connected, "Połączono z mostkiem");
                    break;

                case LIGHTS_AND_GROUPS:
                    break;

                default:
                    break;
            }
        }
    };


    @Override
    public void onClick(View view) {
        if (view == configureBridgeButton) {
            connectToBridge(bridgeDiscoveryResults.get(0).getIP());
        }
        if (view == retryDiscoveryButton) {
            startBridgeDiscovery();
        }
    }


    private void updateUI(final UIState state, final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                discoveryTextView.setText(status);
                logoImageView.setVisibility(View.VISIBLE);

                pushlinkImageView.setVisibility(View.GONE);
                configureBridgeButton.setVisibility(View.GONE);
                retryDiscoveryButton.setVisibility(View.GONE);

                switch (state) {
                    case Error:
                        retryDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryRunning:
                        discoveryTextView.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryResults:
                        configureBridgeButton.setVisibility(View.VISIBLE);
                        break;
                    case Pushlinking:
                        logoImageView.setVisibility(View.GONE);
                        pushlinkImageView.setVisibility(View.VISIBLE);
                        break;
                    case Connected:
                        showMainView();
                        break;
                }
            }
        });
    }
}
