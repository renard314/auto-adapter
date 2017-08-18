package com.renard.auto_adapter;

public interface Binder<ItemType extends Unique> {
    void bind(final ItemType item);
}
