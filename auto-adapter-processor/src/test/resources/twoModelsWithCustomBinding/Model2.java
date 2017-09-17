package com.renard.auto_adapter.test;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;

@AdapterItem(value = "Adapter", viewBinder = Model2Binder.class)
public class Model2 implements Unique {

    @Override
    public long getId() {
        return 0;
    }

}
