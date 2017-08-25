package com.renard.auto_adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.support.annotation.NonNull;

import android.support.v7.widget.RecyclerView;

import android.view.ViewGroup;

abstract class AutoAdapter extends RecyclerView.Adapter<AutoAdapterViewHolder> {
    private Map<Class, ViewHolderFactory> modelToFactoryMapping = new HashMap<>();
    private ArrayList<Unique> items = new ArrayList<>();

    AutoAdapter() {
        setHasStableIds(true);
    }

    <Item extends Unique, Factory extends ViewHolderFactory<Item>> void putMapping(final Class<Item> itemClass,
            final Factory viewHolderFactory) {
        modelToFactoryMapping.put(itemClass, viewHolderFactory);
    }

    @Override
    public AutoAdapterViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return viewHolderFactoryForViewType(viewType).create(parent);
    }

    @NonNull
    private ViewHolderFactory viewHolderFactoryForViewType(final int viewType) {
        for (Map.Entry<Class, ViewHolderFactory> entry : modelToFactoryMapping.entrySet()) {
            if (entry.getValue().getViewType() == viewType) {
                return entry.getValue();
            }
        }

        // can never happen
        throw new IllegalStateException();
    }

    void removeItem(final Unique item) {
        for (int i = 0; i < items.size(); i++) {
            Unique currentItem = items.get(i);
            if (item.getId() == currentItem.getId()) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    void addItem(final Unique item) {
        items.add(item);
        for (int i = 0; i < items.size(); i++) {
            Unique currentItem = items.get(i);
            if (item.getId() == currentItem.getId()) {
                notifyItemChanged(i, item);
                return;
            }
        }

        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(final AutoAdapterViewHolder holder, final int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemViewType(final int position) {
        final Unique unique = items.get(position);
        final ViewHolderFactory viewHolderFactory = modelToFactoryMapping.get(unique.getClass());
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
