package com.renard.auto_adapter;

import android.view.View;

public interface ViewBinder<ItemType> {

    void createView(View view);

    void bind(ItemType item);

    int getLayoutResourceId();
}
