package com.renard.auto_adapter;

import android.view.ViewGroup;

interface ViewHolderFactory<ItemType extends Unique> {

    AutoAdapterViewHolder<ItemType> create(final ViewGroup parent);

    int getViewType();
}
