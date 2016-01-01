package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.event.CheckModuleCapabilityPermissionEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.settings.ModuleCapabilityHasChangedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.DtoType;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.ServiceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {

    private static final String TAG = PermissionAdapter.class.getSimpleName();

    private final List<PermissionListItem> mData;

    private final int requiredState;

    public static final int OPTIONAL = 0;
    public static final int REQUIRED = 1;

    public PermissionAdapter(List<PermissionListItem> mData, int requiredState) {

        if (mData == null) {
            this.mData = Collections.emptyList();
        } else {
            this.mData = mData;
        }

        this.requiredState = requiredState;
    }

    @Override
    public PermissionAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_permission, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        PermissionListItem permItem = mData.get(position);
        final DbModuleCapability capability = permItem.getCapability();

        String title = "";

        if (capability != null) {
            title = DtoType.getName(DtoType.getDtoType(capability.getType()), holder.mTitle.getResources());
        }

        holder.mTitle.setText(title);
        holder.mEnablerSwitch.setChecked(permItem.isChecked());

        if (requiredState == REQUIRED) {
            holder.mEnablerSwitch.setChecked(true);
            holder.mEnablerSwitch.setEnabled(false);
        } else {

            holder.mEnablerSwitch.setVisibility(View.VISIBLE);
            holder.mEnablerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        Log.d(TAG, "Optional permission was ENABLED");
                    } else {
                        Log.d(TAG, "Optional permission was DISABLED");
                    }

                    if (isChecked) {
                        EventBus.getDefault().post(new CheckModuleCapabilityPermissionEvent(capability));
                    }

                    // change capability state
                    capability.setActive(isChecked);

                    if (ServiceUtils.isHarvesterAbleToRun(holder.mEnablerSwitch.getContext())) {
                        // fire state changed
                        EventBus.getDefault().post(new ModuleCapabilityHasChangedEvent(capability));
                    }
                }
            });
        }
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * List of current objects in adapter
     *
     * @return
     */
    public List<PermissionListItem> getData() {
        return mData;
    }

    /**
     * View holder for permission dialog
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.permission_item_title)
        protected TextView mTitle;

        @Bind(R.id.permission_item_switcher)
        protected Switch mEnablerSwitch;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

}