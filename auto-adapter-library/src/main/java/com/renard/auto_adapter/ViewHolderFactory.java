package com.renard.auto_adapter;

import android.support.v7.widget.RecyclerView;

import android.view.ViewGroup;

public interface ViewHolderFactory<ItemType extends RecyclerView.ViewHolder> {
    ItemType create(final ViewGroup parent);

    int getViewType();
}
