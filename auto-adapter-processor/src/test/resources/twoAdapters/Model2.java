package com.renard.auto_adapter.sample;

import android.view.View;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;
import com.renard.auto_adapter.ViewBinder;


@AdapterItem(value = "Adapter2", viewBinder = Model2.class)
public class Model2 implements ViewBinder<Model2>, Unique {

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void createView(View view) {

    }

    @Override
    public void bind(Model2 item) {

    }

    @Override
    public int getLayoutResourceId() {
        return 0;
    }
}