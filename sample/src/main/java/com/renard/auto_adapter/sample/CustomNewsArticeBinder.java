package com.renard.auto_adapter.sample;

import com.renard.auto_adapter.ViewBinder;

import android.view.View;

import android.widget.TextView;

public class CustomNewsArticeBinder implements ViewBinder<NewsArticle> {
    private TextView headline;
    private TextView body;

    @Override
    public void createView(final View view) {
        headline = view.findViewById(R.id.headline);
        body = view.findViewById(R.id.body);
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
