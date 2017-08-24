package com.example.rwellnitz.tasktest.auto;

import android.view.View;

import com.example.rwellnitz.tasktest.view_model.NewsArticle;
import com.renard.auto_adapter.AutoAdapterViewHolder;

public class MyViewHolder extends AutoAdapterViewHolder<NewsArticle> {

    MyViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(NewsArticle item) {

    }

    @Override
    public int getLayoutResourceId() {
        return android.R.layout.activity_list_item;
    }
}
