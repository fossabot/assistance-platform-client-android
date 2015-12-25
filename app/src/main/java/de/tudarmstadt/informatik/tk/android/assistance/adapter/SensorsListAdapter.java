package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.settings.SensorsListFragment;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.SensorsListItem;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 17.07.2015
 */
public class SensorsListAdapter extends RecyclerView.Adapter<SensorsListAdapter.ViewHolder> {

    private static final String TAG = SensorsListFragment.class.getSimpleName();

    private List<SensorsListItem> mData;

    public SensorsListAdapter(List<SensorsListItem> mData) {
        this.mData = mData;
    }

    @Override
    public SensorsListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sensor, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setBackgroundResource(R.drawable.row_selector);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SensorsListAdapter.ViewHolder viewHolder, int position) {

        SensorsListItem currentSensor = mData.get(position);

        viewHolder.textView.setText(currentSensor.getName());
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    /**
     * Sensors list item holder
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.sensor_list_item)
        protected TextView textView;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
