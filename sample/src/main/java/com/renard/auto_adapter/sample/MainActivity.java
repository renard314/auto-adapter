package com.renard.auto_adapter.sample;

import com.renard.auto_adapter.NewsArticleAdapter;
import com.renard.auto_adapter.OnClick;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.View;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private NewsArticleAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prepareRecyclerView();
        addModelsToAdapter();
    }

    @OnClick(R.id.body)
    public void onBodyClicked() {
        Toast.makeText(this, " onBodyClicked was called", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.body)
    public void onNewsArticleClicked(final NewsArticle newsArticle) {
        Toast.makeText(this, newsArticle.toString() + " was clicked", Toast.LENGTH_LONG).show();
    }

    @OnClick({ R.id.advertisement, R.id.body })
    public void onAdClick(final View view, final Advertisement advertisement) {
        Toast.makeText(this, advertisement.toString() + " was clicked", Toast.LENGTH_LONG).show();
    }

    private void prepareRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new NewsArticleAdapter();
        adapter.registerForEvents(this);

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
