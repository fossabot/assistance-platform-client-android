package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.image.ScaledDownTransformation;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.ModuleProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.UiUtils;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.ContentFactory;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ContentDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.FeedbackItemType;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ButtonDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.GroupDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.ImageDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.MapDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.TextDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.10.2015
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnMapReadyCallback {

    private static final String TAG = "NewsAdapter";

    private static final int ICON_SETTINGS_MAX_WIDTH = 100;
    private static final int ICON_SETTINGS_MAX_HEIGHT = 70;

    private static final int EMPTY_VIEW_TYPE = 10;

    private final Context context;

    private final ModuleProvider moduleProvider;

    private final UiUtils uiUtils;

    private List<ClientFeedbackDto> data;

    private GoogleMap googleMap;
    private MapView mapView;
    private LatLng mapPoint;
    private LatLng[] mapPoints;

    public NewsAdapter(List<ClientFeedbackDto> data, Context context) {

        if (data == null) {
            this.data = Collections.emptyList();
        } else {
            this.data = data;
        }

        this.context = context;

        moduleProvider = ModuleProvider.getInstance(context);
        uiUtils = UiUtils.getInstance(context);
    }

    /**
     * Converts to LatLng array of location points
     *
     * @param mapPoints
     */
    protected void setMapPoints(Double[][] mapPoints) {

        if (mapPoints == null) {
            this.mapPoints = new LatLng[0];
        }

        List<LatLng> tmpPointsList = new ArrayList<>(mapPoints.length);

        for (Double[] point : mapPoints) {
            tmpPointsList.add(new LatLng(point[0], point[1]));
        }

        this.mapPoints = tmpPointsList.toArray(new LatLng[tmpPointsList.size()]);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == EMPTY_VIEW_TYPE) {
            // list is empty
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_empty_list_view, parent, false);
            EmptyViewHolder emptyView = new EmptyViewHolder(view);

            return emptyView;
        }

        // list has items
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        NewsViewHolder newsHolder = new NewsViewHolder(view);

        return newsHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof NewsViewHolder) {

            final NewsViewHolder viewHolder = (NewsViewHolder) holder;

            final ClientFeedbackDto newsCard = getItem(position);
            final ContentDto cardContent = newsCard.getContent();

            viewHolder.title.setText(moduleProvider.getModuleTitle(newsCard.getModuleId()));

            int size = (int) Math.ceil(Math.sqrt(ICON_SETTINGS_MAX_WIDTH * ICON_SETTINGS_MAX_HEIGHT));

            Picasso.with(context)
                    .load(R.drawable.ic_more_vert_black_48dp)
                    .placeholder(R.drawable.no_image)
                    .transform(new ScaledDownTransformation(
                            ICON_SETTINGS_MAX_WIDTH,
                            ICON_SETTINGS_MAX_HEIGHT))
                    .skipMemoryCache()
                    .resize(size, size)
                    .centerInside()
                    .into(viewHolder.cardSettings);


            viewHolder.cardSettings.setOnClickListener(v -> {
                Log.d(TAG, "User selected more for " + newsCard.getModuleId() + " module");
            });

            FeedbackItemType feedbackType = FeedbackItemType.getEnum(cardContent.getType());

            switch (feedbackType) {
                case TEXT:
                    TextDto textDto = ContentFactory.getText(cardContent);
                    viewHolder.mContainer.addView(uiUtils.getText(textDto));
                    break;
                case BUTTON:
                    ButtonDto buttonDto = ContentFactory.getButton(cardContent);
                    viewHolder.mContainer.addView(uiUtils.getButton(buttonDto));
                    break;
                case IMAGE:
                    ImageDto imageDto = ContentFactory.getImage(cardContent);
                    viewHolder.mContainer.addView(uiUtils.getImage(imageDto));
                    break;
                case MAP:
                    MapDto mapDto = ContentFactory.getMap(cardContent);
                    mapView = uiUtils.getMap(mapDto);
                    mapView = getSetuppedMap(mapView);
                    viewHolder.mContainer.addView(mapView);
                    break;
                case GROUP:
                    GroupDto groupDto = ContentFactory.getGroup(cardContent);
                    viewHolder.mContainer.addView(uiUtils.getGroup(groupDto));
                    break;
            }
        }
    }

    private MapView getSetuppedMap(MapView mapView) {

        mapView.onCreate(null);
        mapView.getMapAsync(this);

        return mapView;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Nullable
    public ClientFeedbackDto getItem(int position) {

        if (position < 0 || position >= data.size()) {
            return null;
        }

        return data.get(position);
    }

    @Override
    public int getItemViewType(int position) {

        if (getItemCount() == 0) {
            return EMPTY_VIEW_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }

    /**
     * Swaps out old data with new data in the adapter
     *
     * @param newList
     */
    public void swapData(List<ClientFeedbackDto> newList) {

        if (newList == null) {
            data = Collections.emptyList();
        } else {
            data = Lists.newArrayList(newList);
        }

        notifyDataSetChanged();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        MapsInitializer.initialize(mapView.getContext());
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        if (mapPoint != null) {
            updateMapPoint();
        }
    }

    private void updateMapPoint() {
        googleMap.clear();

        // Update the mapView feature data and camera position.
        googleMap.addMarker(new MarkerOptions().position(mapPoint));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mapPoint, 10.0f);

        googleMap.moveCamera(cameraUpdate);
    }

    protected void setLocation() {

        if (googleMap != null) {
            updateMapPoint();
        }
    }

    /**
     * Setting data only if we need map in the view
     *
     * @param mapView
     * @param mapPoint
     */
    protected void setGoogleMap(MapView mapView, LatLng mapPoint) {
        this.mapView = mapView;
        this.mapPoint = mapPoint;
    }

    /**
     * An empty view holder if no items available
     */
    protected static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    /**
     * View holder for available module
     */
    protected static class NewsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.title)
        protected TextView title;

        @Bind(R.id.cardSettings)
        protected ImageView cardSettings;

        @Bind(R.id.newsContainer)
        protected LinearLayout mContainer;

        public NewsViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
