package com.renard.auto_adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;

import android.view.ViewGroup;

public abstract class AutoAdapter extends RecyclerView.Adapter {
    private Map<Class, ViewHolderFactory> itemClassToViewFactoryMapping = new HashMap<>();
    private ArrayList<Unique> items = new ArrayList<>();

    protected AutoAdapter(final Map<Class, ViewHolderFactory> mapping) {
        setHasStableIds(true);
        itemClassToViewFactoryMapping = mapping;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return viewHolderFactoryForViewType(viewType).create(parent);
    }

    private ViewHolderFactory viewHolderFactoryForPosition(final int position) {
        Unique unique = items.get(position);
        return itemClassToViewFactoryMapping.get(unique.getClass());
    }

    @NonNull
    private ViewHolderFactory viewHolderFactoryForViewType(final int viewType) {
        for (Map.Entry<Class, ViewHolderFactory> entry : itemClassToViewFactoryMapping.entrySet()) {
            if (entry.getValue().getViewType() == viewType) {
                return entry.getValue();
            }
        }

        // can never happen
        return null;
    }

    protected void addItem(final Unique item) {
        items.add(item);
        for (int i = 0; i < items.size(); i++) {
            Unique currentItem = items.get(i);
            if (currentItem.getId() == currentItem.getId()) {
                notifyItemChanged(i, item);
                return;
            }
        }

        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((Binder) holder).bind(items.get(position));
    }

    @Override
    public int getItemViewType(final int position) {
        ViewHolderFactory viewHolderFactory = viewHolderFactoryForPosition(position);
        return viewHolderFactory.getViewType();
    }

    @Override
    public long getItemId(final int position) {
        return items.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
