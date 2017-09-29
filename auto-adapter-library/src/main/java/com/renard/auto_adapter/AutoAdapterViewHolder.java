package com.renard.auto_adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v7.widget.RecyclerView;

import android.view.View;

final class AutoAdapterViewHolder<ItemType extends Unique> extends RecyclerView.ViewHolder {

    private final ViewBinder<ItemType> binder;
    @Nullable
    private final View[] views;
    private ItemType item;

    AutoAdapterViewHolder(final View itemView, final ViewBinder<ItemType> binder, @NonNull final int[] viewIds) {
        super(itemView);
        this.binder = binder;
        views = new View[viewIds.length];
        for (int i = 0; i < viewIds.length; i++) {
            int id = viewIds[i];
            View view = itemView.findViewById(id);
            if (view != null) {
                views[i] = view;
            }
        }

    }

    void bind(final ItemType item) {
        this.item = item;
        binder.bind(item);
    }

    void registerForCLick(@NonNull final ItemClickListener itemClickListener) {
        if (views == null) {
            return;
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                itemClickListener.onClickItem(view, item);
            }
        };

        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(onClickListener);
            }
        }
    }

    void onViewDetachedFromWindow() {
        if (views == null) {
            return;
        }

        for (View view : views) {
            if (view != null) {
                view.setOnClickListener(null);
            }
        }
    }

    interface ItemClickListener {
        void onClickItem(View view, Unique item);
    }
}
