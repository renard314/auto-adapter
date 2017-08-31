package com.renard.auto_adapter;

class Model1 implements Unique {
    private final long id;

    Model1(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

}
