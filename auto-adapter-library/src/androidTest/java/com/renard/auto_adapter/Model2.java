package com.renard.auto_adapter;

class Model2 implements Unique {
    private final long id;

    Model2(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

}
