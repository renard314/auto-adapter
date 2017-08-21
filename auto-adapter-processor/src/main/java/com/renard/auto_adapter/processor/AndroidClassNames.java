package com.renard.auto_adapter.processor;

import com.squareup.javapoet.ClassName;

final class AndroidClassNames {

    static final ClassName VIEW_HOLDER = ClassName.get("android.support.v7.widget", "RecyclerView").nestedClass("ViewHolder");
    static final ClassName VIEW = ClassName.get("android.view", "View");
    static final ClassName INFLATER = ClassName.get("android.view", "LayoutInflater");
    static final ClassName VIEW_GROUP = ClassName.get("android.view", "ViewGroup");
    static final String VIEW_DATA_BINDING = "android.databinding.ViewDataBinding";
}
