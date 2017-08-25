package com.renard.auto_adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AdapterItem {
    String value();

    Class<? extends ViewBinder> viewBinder() default ViewBinder.class;

}
