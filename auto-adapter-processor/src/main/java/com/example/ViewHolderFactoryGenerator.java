package com.example;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

class ViewHolderFactoryGenerator {
    private final ClassName viewHolderFactoryClassName;
    private final ClassName dataBindingClassName;
    private final ClassName viewHolderClassName;
    private final int viewType;

    ViewHolderFactoryGenerator(ClassName viewHolderFactoryClassName, ClassName dataBindingClassName, ClassName viewHolderClassName, int viewType) {
        this.viewHolderFactoryClassName = viewHolderFactoryClassName;
        this.dataBindingClassName = dataBindingClassName;
        this.viewHolderClassName = viewHolderClassName;
        this.viewType = viewType;
    }

    TypeSpec generate() {
        TypeSpec.Builder classBuilder = createBuilder();

        addCreateMethod(classBuilder);
        addGetViewTypeMethod(classBuilder);

        return classBuilder.build();
    }

    private void addGetViewTypeMethod(TypeSpec.Builder viewHolderFactoryClass) {
        MethodSpec.Builder getViewTypeMethod = MethodSpec.methodBuilder("getViewType").
                addCode("return $L;\n", viewType).
                returns(TypeName.INT).
                addModifiers(Modifier.PUBLIC);

        viewHolderFactoryClass.addMethod(getViewTypeMethod.build());
    }

    private void addCreateMethod(TypeSpec.Builder viewHolderFactoryClass) {
        ClassName layoutInflaterClassName = ClassName.get("android.view", "LayoutInflater");
        ClassName viewGroupClassName = ClassName.get("android.view", "ViewGroup");

        CodeBlock code = CodeBlock.builder().add("$T from = LayoutInflater.from(parent.getContext());\n", layoutInflaterClassName).
                add("$1T binding = $2T.inflate(from, parent, false);\n", dataBindingClassName, dataBindingClassName).
                add("return new $T(binding.getRoot(), binding);\n", viewHolderClassName).build();

        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create").
                returns(viewHolderClassName).
                addParameter(viewGroupClassName, "parent").
                addCode(code).
                addModifiers(Modifier.PUBLIC);

        viewHolderFactoryClass.addMethod(createMethod.build());
    }

    private TypeSpec.Builder createBuilder() {
        ClassName factoryClassName = ClassName.get("com.example.api.adapter", "ViewHolderFactory");
        ParameterizedTypeName factoryTypeName = ParameterizedTypeName.get(factoryClassName, viewHolderClassName);

        return TypeSpec
                .classBuilder(viewHolderFactoryClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(factoryTypeName);
    }
}
