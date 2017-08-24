package com.renard.auto_adapter;

import android.view.ViewGroup;

interface ViewHolderFactory<ItemType extends Unique, ViewHolderType extends AutoAdapterViewHolder<ItemType>> {
    ViewHolderType create(final ViewGroup parent);

    int getViewType();
}
