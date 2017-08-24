package com.renard.auto_adapter;

public interface ViewBinder<ItemType> {
    void bind(ItemType item);

    int getLayoutResourceId();
}
