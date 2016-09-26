package de.tudarmstadt.informatik.tk.assistance.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.adapter.ModulesAdapter.ViewHolder;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleUninstallEvent;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 18.10.2015
 */
public class ModulesAdapter extends Adapter<ViewHolder> {

    private static final String TAG = ModulesAdapter.class.getSimpleName();

    private static final int EMPTY_VIEW_TYPE = 10;

    private List<DbModule> modules;

    public ModulesAdapter(List<DbModule> modules) {

        if (modules == null) {
            this.modules = Collections.emptyList();
        } else {
            this.modules = modules;
            sort();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == EMPTY_VIEW_TYPE) {
            // list is empty
            view = LayoutInflater.from(parent.getContext())
                    .inflate(layout.item_empty_list_view, parent, false);

            return new EmptyViewHolder(view);
        } else {
            // list has items
            view = LayoutInflater.from(parent.getContext())
                    .inflate(layout.item_module_card, parent, false);

            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // only for non empty list
        if (holder instanceof ViewHolder) {

            final DbModule module = getItem(position);

            final ViewHolder viewHolder = (ViewHolder) holder;

            viewHolder.mMainTitle.setText(module.getTitle());
            viewHolder.mShortDescription.setText(module.getDescriptionShort());

            if (module.getActive()) {

                viewHolder.mUninstallModule.setVisibility(View.VISIBLE);
                viewHolder.mInstallModule.setVisibility(View.GONE);

                viewHolder.mUninstallModule.setOnClickListener(v ->
                        EventBus.getDefault().post(new ModuleUninstallEvent(module.getPackageName())));

            } else {

                viewHolder.mUninstallModule.setVisibility(View.GONE);
                viewHolder.mInstallModule.setVisibility(View.VISIBLE);

                viewHolder.mInstallModule.setOnClickListener(v ->
                        EventBus.getDefault().post(new ModuleInstallEvent(module.getPackageName())));
            }

            viewHolder.mMoreInfoModule.setOnClickListener(v ->
                    EventBus.getDefault().post(new ModuleShowMoreInfoEvent(module.getPackageName())));
        }
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    /**
     * Gives back module items
     *
     * @return
     */
    public List<DbModule> getItems() {
        return modules;
    }

    /**
     * Returns item on given position
     *
     * @param position
     * @return
     */
    @Nullable
    public DbModule getItem(int position) {

        if (position < 0 || position >= modules.size()) {
            return null;
        }

        return modules.get(position);
    }

    /**
     * Returns item by package id
     *
     * @param modulePackageId
     * @return
     */
    @Nullable
    public DbModule getItem(String modulePackageId) {

        if (getItemCount() == 0 || modulePackageId == null) {
            return null;
        }

        for (DbModule module : modules) {

            if (modulePackageId.equals(module.getPackageName())) {
                return module;
            }
        }

        return null;
    }

    /**
     * Sorts module list
     */
    private void sort() {
        // sort modules by title ASC
        Collections.sort(this.modules, (lhs, rhs) -> lhs.getTitle().compareToIgnoreCase(rhs.getTitle()));
    }

    @Override
    public int getItemViewType(int position) {

        if (getItemCount() == 0) {
            return EMPTY_VIEW_TYPE;
        }

        return super.getItemViewType(position);
    }

    /**
     * Swaps out old data with new data in the adapter
     *
     * @param newList
     */
    public void swapData(List<DbModule> newList) {

        if (newList == null) {
            this.modules = Collections.emptyList();
        } else {
            this.modules = Lists.newArrayList(newList);
            sort();
        }

        notifyDataSetChanged();
    }

    /**
     * An empty view holder if no items available
     */
    public static class EmptyViewHolder extends ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    /**
     * View holder for available module
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(id.main_title)
        protected AppCompatTextView mMainTitle;
        @BindView(id.main_short_description)
        protected AppCompatTextView mShortDescription;
        @BindView(id.more_info_module)
        protected AppCompatButton mMoreInfoModule;
        @BindView(id.install_module)
        protected AppCompatButton mInstallModule;
        @BindView(id.uninstall_module)
        protected AppCompatButton mUninstallModule;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
