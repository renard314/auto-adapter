package com.renard;

import com.renard.NewsArticleWithBinding;

import com.renard.auto_adapter.ViewBinder;

import android.view.View;

public class NewsArticleBinder implements ViewBinder<NewsArticleWithBinding> {

    @Override
    public void createView(final View view) { }

    @Override
    public void bind(final NewsArticleWithBinding item) { }

    @Override
    public int getLayoutResourceId() {
        return 0;
    }
}
