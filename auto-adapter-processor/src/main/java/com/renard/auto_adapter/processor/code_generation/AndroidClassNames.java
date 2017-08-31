package com.renard.auto_adapter.processor.code_generation;

import com.squareup.javapoet.ClassName;

final class AndroidClassNames {

    static final ClassName VIEW = ClassName.get("android.view", "View");
    static final ClassName INFLATER = ClassName.get("android.view", "LayoutInflater");
    static final ClassName VIEW_GROUP = ClassName.get("android.view", "ViewGroup");
}
