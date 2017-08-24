package com.renard.auto_adapter;

import android.support.annotation.LayoutRes;

import android.support.v7.widget.RecyclerView;

import android.view.View;

public abstract class AutoAdapterViewHolder<ItemType extends Unique> extends RecyclerView.ViewHolder {

    protected AutoAdapterViewHolder(final View itemView) {
        super(itemView);
    }

    public abstract void bind(ItemType item);

    @LayoutRes
    public abstract int getLayoutResourceId();
}
