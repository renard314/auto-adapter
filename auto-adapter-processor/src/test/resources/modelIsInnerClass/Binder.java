package com.renard.auto_adapter.test;

import android.view.View;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;
import com.renard.auto_adapter.ViewBinder;


public class Binder implements ViewBinder<Binder.Model> {


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

    @AdapterItem(value = "Adapter", viewBinder = Binder.class)
    public static class Model implements Unique {

        @Override
        public long getId() {
            return 0;
        }

    }

}