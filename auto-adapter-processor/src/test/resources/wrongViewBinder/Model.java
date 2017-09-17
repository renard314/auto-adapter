package com.renard.auto_adapter.sample;

import android.view.View;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;
import com.renard.auto_adapter.ViewBinder;


@AdapterItem(value = "Adapter1", viewBinder = com.renard.auto_adapter.sample.ModelAsBinder.class)
public class Model implements ViewBinder<Model>, Unique {

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void createView(View view) {

    }

    @Override
    public void bind(Model item) {

    }

    @Override
    public int getLayoutResourceId() {
        return 0;
    }
}