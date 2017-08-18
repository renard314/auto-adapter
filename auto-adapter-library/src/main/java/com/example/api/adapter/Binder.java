package com.example.api.adapter;

public interface Binder<ItemType extends Unique> {
    void bind(final ItemType item);
}
