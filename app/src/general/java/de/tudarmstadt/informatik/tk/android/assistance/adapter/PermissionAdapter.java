package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {

    private static final String TAG = PermissionAdapter.class.getSimpleName();

    private final List<PermissionListItem> mData;

    public PermissionAdapter(List<PermissionListItem> mData) {

        if (mData == null) {
            this.mData = new ArrayList<>();
        } else {
            this.mData = mData;
        }
    }

    @Override
    public PermissionAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_permission, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        PermissionListItem item = mData.get(position);

        holder.mTitle.setText(item.getTitle());
        holder.mEnablerSwitch.setChecked(item.isEnabled());
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return this.mData.size();
    }

    /**
     * View holder for permission dialog
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.permission_item_title)
        protected TextView mTitle;

        @Bind(R.id.permission_item_enabler)
        protected SwitchCompat mEnablerSwitch;

        public ViewHolder(View view) {
            super(view);
            Log.d(TAG, "Sukabla");
            ButterKnife.bind(this, view);
        }
    }

}
