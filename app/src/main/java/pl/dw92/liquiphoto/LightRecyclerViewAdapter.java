package pl.dw92.liquiphoto;

import android.content.ClipData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class LightRecyclerViewAdapter extends RecyclerView.Adapter<LightRecyclerViewAdapter.ViewHolder> {

    private List<Light> lights;
    private LayoutInflater layoutInflater;
    private ItemClickListener clickListener;

    LightRecyclerViewAdapter(Context context, List<Light> lights) {
        this.layoutInflater = LayoutInflater.from(context);
        this.lights = lights;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recyclerview_light, parent, false);
        final ViewHolder holder = new ViewHolder(view);
       /* holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Light light = lights.get(holder.getAdapterPosition());
                final DragData state = new DragData(light, view.getWidth(), view.getHeight());
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                //ViewCompat.startDragAndDrop(view, data, shadowBuilder, state, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            }
        });*/
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = lights.get(position).getName();
        holder.textView.setText(name);
        holder.textView.setTag(position);
        holder.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return lights.size();
    }

    public Light getItem(int id) {
        return lights.get(id);
    }

    public List<Light> getLights() {
        return lights;
    }

    public void updateLights(List<Light> lights) {
        this.lights = lights;
    }


    public void removeItem(int position) {
        lights.remove(position);
        notifyItemRemoved(position);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;
        //ImageView imageView;

        ViewHolder(View view) {
            super(view);
            //imageView = view.findViewById(R.id.lightImageView);
            textView = view.findViewById(R.id.lightTextView);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

}
