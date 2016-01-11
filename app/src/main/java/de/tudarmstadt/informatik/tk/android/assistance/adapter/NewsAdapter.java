package de.tudarmstadt.informatik.tk.android.assistance.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.util.UiUtils;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.ContentFactory;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ContentDto;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.enums.FeedbackItemType;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.item.TextDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.10.2015
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_VIEW_TYPE = 10;

    private final Context context;

    private final UiUtils uiUtils;

    private List<ClientFeedbackDto> news;

    public NewsAdapter(List<ClientFeedbackDto> news, Context context) {

        if (news == null) {
            this.news = Collections.emptyList();
        } else {
            this.news = news;
        }

        this.context = context;

        uiUtils = UiUtils.getInstance(context);
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

            FeedbackItemType feedbackType = FeedbackItemType.getEnum(cardContent.getType());

            switch (feedbackType) {
                case TEXT:
                    TextDto textDto = ContentFactory.getText(cardContent);
                    viewHolder.mContainer.addView(uiUtils.getText(textDto));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    @Nullable
    public ClientFeedbackDto getItem(int position) {

        if (position < 0 || position >= news.size()) {
            return null;
        }

        return news.get(position);
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
    public void swapData(List<ClientFeedbackDto> newList) {

        if (newList == null) {
            news = Collections.emptyList();
        } else {
            news = Lists.newArrayList(newList);
        }

        notifyDataSetChanged();
    }

    /**
     * An empty view holder if no items available
     */
    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View view) {
            super(view);
        }
    }

    /**
     * View holder for available module
     */
    protected static class NewsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.newsContainer)
        protected LinearLayout mContainer;

        public NewsViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
