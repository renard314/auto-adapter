package com.renard;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;
import com.renard.NewsArticleBinder;

@AdapterItem(value = "NewsArticleAdapter", viewBinder = AdvertisementBinder.class)
public class AdvertisementWithBinding implements Unique {
    private final long id;

    public String title = "This is a title";
    public String body = "this is the text body";

    public AdvertisementWithBinding(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }

}
