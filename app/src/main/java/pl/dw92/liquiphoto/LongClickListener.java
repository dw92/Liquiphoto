package pl.dw92.liquiphoto;

import android.content.ClipData;
import android.view.View;

public class LongClickListener implements View.OnLongClickListener {
    @Override
    public boolean onLongClick(View view) {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(data, shadowBuilder, view, 0);
        return true;
    }
}
