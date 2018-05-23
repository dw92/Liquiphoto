package pl.dw92.liquiphoto;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class LightRecyclerViewAdapter extends RecyclerView.Adapter<LightRecyclerViewAdapter.ViewHolder> {

    private List<Light> lights;
    private LayoutInflater layoutInflater;

    LightRecyclerViewAdapter(Context context, List<Light> lights) {
        this.layoutInflater = LayoutInflater.from(context);
        this.lights = lights;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recyclerview_light, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Light light = lights.get(position);
        holder.textView.setText(light.getName());
        holder.textView.setTag(position);

        if (light.isReachable()) {
            holder.textView.setEnabled(true);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.light, 0, 0);
            holder.textView.setOnLongClickListener(new LongClickListener());
        }
        else {
            holder.textView.setEnabled(false);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.light_not_reachable, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return lights.size();
    }

    public List<Light> getLights() {
        return lights;
    }

    public void updateLights(List<Light> lights) {
        this.lights = lights;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.lightTextView);
        }
    }

}
