package com.example.rwellnitz.tasktest.view_model;

import com.example.rwellnitz.tasktest.auto.MyViewHolder;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;

@AdapterItem(value = "NewsArticleAdapter", viewHolder = MyViewHolder.class)
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
