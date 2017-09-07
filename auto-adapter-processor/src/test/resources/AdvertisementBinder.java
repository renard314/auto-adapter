package com.renard;

import com.renard.NewsArticleWithBinding;

import com.renard.auto_adapter.ViewBinder;

import android.view.View;

public class AdvertisementBinder implements ViewBinder<AdvertisementWithBinding> {

    @Override
    public void createView(final View view) { }

    @Override
    public void bind(final AdvertisementWithBinding item) { }

    @Override
    public int getLayoutResourceId() {
        return 0;
    }
}
