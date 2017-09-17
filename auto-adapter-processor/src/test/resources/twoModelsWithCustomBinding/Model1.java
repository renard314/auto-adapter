package com.renard.auto_adapter.test;


import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;

@AdapterItem(value = "Adapter", viewBinder = Model1Binder.class)
public class Model1 implements Unique {

    @Override
    public long getId() {
        return 0;
    }

}
