package pl.dw92.liquiphoto;

import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;

class BridgeHandler {
    private static BridgeHandler bridgeHandler;

    private Bridge bridge;

    public static BridgeHandler getInstance() {
        if (bridgeHandler == null) {
            bridgeHandler = new BridgeHandler();
        }
        return bridgeHandler;
    }

    public Bridge getBridge() {
        if (bridge == null) {
            return null;
        }
        return bridge;
    }

    public void setBridge(Bridge bridge) {
       this.bridge = bridge;
    }
}
