package com.renard.auto_adapter.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

class ViewHolderInfo {
    final ClassName viewHolderClassName;
    final ClassName viewHolderFactoryClassName;
    final TypeElement model;

    ViewHolderInfo(ClassName viewHolderClassName, ClassName viewHolderFactoryClassName, TypeElement model) {
        this.viewHolderClassName = viewHolderClassName;
        this.viewHolderFactoryClassName = viewHolderFactoryClassName;
        this.model = model;
    }
}
