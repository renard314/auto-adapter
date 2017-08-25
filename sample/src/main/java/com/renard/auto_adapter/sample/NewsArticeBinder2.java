package com.renard.auto_adapter.sample;

import android.view.View;
import android.widget.TextView;

import com.renard.auto_adapter.ViewBinder;

public class NewsArticeBinder2 implements ViewBinder<NewsArticle> {
    private TextView headline;
    private TextView body;

    @Override
    public void createView(View view) {
        headline = (TextView) view.findViewById(R.id.headline);
        body = (TextView) view.findViewById(R.id.body);
    }

    @Override
    public void bind(final NewsArticle item) {
        headline.setText(item.title);
        body.setText(item.body);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.news_article_item_no_databinding;
    }
}
