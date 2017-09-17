package com.renard.auto_adapter.sample;

import android.view.View;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;
import com.renard.auto_adapter.ViewBinder;


@AdapterItem(value = "Adapter1", viewBinder = Model1.class)
public class Model1 implements ViewBinder<Model1>, Unique {

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void createView(View view) {

    }

    @Override
    public void bind(Model1 item) {

    }

    @Override
    public int getLayoutResourceId() {
        return 0;
    }
}