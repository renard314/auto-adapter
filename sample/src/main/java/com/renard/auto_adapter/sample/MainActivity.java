package com.renard.auto_adapter.sample;

import com.renard.auto_adapter.NewsArticleAdapter;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private NewsArticleAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareRecyclerView();
        addModelsToAdapter();
    }

    private void prepareRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new NewsArticleAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void addModelsToAdapter() {
        adapter.addAdvertisement(new Advertisement(0));
        adapter.addNewsArticle(new NewsArticle(1));
        adapter.addAdvertisement(new Advertisement(2));
        adapter.addNewsArticle(new NewsArticle(3));
    }

}
