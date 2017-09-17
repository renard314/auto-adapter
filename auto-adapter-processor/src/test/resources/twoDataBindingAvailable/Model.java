package com.renard.auto_adapter.test;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;

@AdapterItem(value = "Adapter")
public class Model implements Unique {

    @Override
    public long getId() {
        return 0;
    }

}
