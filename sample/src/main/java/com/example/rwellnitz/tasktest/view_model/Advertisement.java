package com.example.rwellnitz.tasktest.view_model;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;

@AdapterItem("NewsArticleAdapter")
public class Advertisement implements Unique {
    private final long id;

    public String message = "This is an advertisement message";

    public Advertisement(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
