package de.tu_darmstadt.tk.android.assistance.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.callbacks.DrawerCallback;
import de.tu_darmstadt.tk.android.assistance.models.items.DrawerItem;


/**
 *  Navigation recycler view adapter to manage content
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    private List<DrawerItem> mData;
    private DrawerCallback mDrawerCallback;
    private View mSelectedView;
    private int mSelectedPosition;

    public DrawerAdapter(List<DrawerItem> data) {
        mData = data;
    }

    public DrawerCallback getNavigationDrawerCallbacks() {
        return mDrawerCallback;
    }

    public void setNavigationDrawerCallbacks(DrawerCallback drawerCallback) {
        mDrawerCallback = drawerCallback;
    }

    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_item, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(v);

        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {

                                                       if (mSelectedView != null) {
                                                           mSelectedView.setSelected(false);
                                                       }

                                                       mSelectedPosition = viewHolder.getAdapterPosition();
                                                       v.setSelected(true);

                                                       mSelectedView = v;
                                                       if (mDrawerCallback != null) {
                                                           mDrawerCallback.onNavigationDrawerItemSelected(viewHolder.getAdapterPosition());
                                                       }
                                                   }
                                               }
        );

        viewHolder.itemView.setBackgroundResource(R.drawable.row_selector);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder viewHolder, int i) {

        viewHolder.textView.setText(mData.get(i).getText());
        viewHolder.textView.setCompoundDrawablesWithIntrinsicBounds(mData.get(i).getDrawable(), null, null, null);

        if (mSelectedPosition == i) {
            if (mSelectedView != null) {
                mSelectedView.setSelected(false);
            }

            mSelectedPosition = i;
            mSelectedView = viewHolder.itemView;
            mSelectedView.setSelected(true);
        }
    }


    public void selectPosition(int position) {
        mSelectedPosition = position;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_name)
        protected TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public DrawerItem getDataItem(int position) {
        return mData.get(position);
    }
}