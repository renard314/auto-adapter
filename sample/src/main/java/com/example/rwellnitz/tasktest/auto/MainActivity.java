package com.example.rwellnitz.tasktest.auto;

import com.example.rwellnitz.tasktest.NewsArticleAdapter;
import com.example.rwellnitz.tasktest.R;
import com.example.rwellnitz.tasktest.view_model.Advertisement;
import com.example.rwellnitz.tasktest.view_model.NewsArticle;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private IdGenerator idGenerator = new IdGenerator();
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
        adapter.addAdvertisement(new Advertisement(idGenerator.nextId()));
        adapter.addNewsArticle(new NewsArticle(idGenerator.nextId()));
        adapter.addAdvertisement(new Advertisement(idGenerator.nextId()));
        adapter.addNewsArticle(new NewsArticle(idGenerator.nextId()));
    }

}
