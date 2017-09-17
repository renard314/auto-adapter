package com.renard;

import com.renard.auto_adapter.AdapterItem;
import com.renard.auto_adapter.Unique;

@AdapterItem(value = "Adapter", viewBinder = ModelBinder.class)
public class Model implements Unique {
    @Override
    public long getId() {
        return 0;
    }

}
