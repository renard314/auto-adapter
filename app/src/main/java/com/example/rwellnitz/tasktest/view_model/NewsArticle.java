package com.example.rwellnitz.tasktest.view_model;

import com.example.AdapterItem;

import com.example.api.adapter.Unique;

@AdapterItem("NewsArticleAdapter")
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