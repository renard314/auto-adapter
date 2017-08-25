package com.renard.auto_adapter.sample;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;

@AdapterItem(value = "NewsArticleAdapter", viewBinder = NewsArticeBinder2.class)
public class NewsArticle implements Unique {
    private final long id;

    public String title = "This is a title";
    public String body = "this is the text body";

    public NewsArticle(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
