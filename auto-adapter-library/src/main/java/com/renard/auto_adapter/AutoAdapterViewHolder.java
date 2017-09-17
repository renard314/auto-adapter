package com.renard.auto_adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

final class AutoAdapterViewHolder<ItemType extends Unique> extends RecyclerView.ViewHolder {
    private final ViewBinder<ItemType> binder;

    AutoAdapterViewHolder(final View itemView, final ViewBinder<ItemType> binder) {
        super(itemView);
        this.binder = binder;
    }

    void bind(final ItemType item) {
        binder.bind(item);
    }

}
