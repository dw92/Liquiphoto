package pl.dw92.liquiphoto;

import android.app.Application;

import com.philips.lighting.hue.sdk.wrapper.HueLog;
import com.philips.lighting.hue.sdk.wrapper.Persistence;

public class Liquiphoto extends Application {

    static {
        System.loadLibrary("huesdk");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Persistence.setStorageLocation(getFilesDir().getAbsolutePath(), "Liquiphoto");
        HueLog.setConsoleLogLevel(HueLog.LogLevel.INFO);
    }
}
