package com.example;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

class ViewHolderGenerator {
    private final ClassName viewHolderSimpleName;
    private final ClassName dataBindingName;

    ViewHolderGenerator(ClassName viewHolderSimpleName, ClassName dataBindingName) {
        this.viewHolderSimpleName = viewHolderSimpleName;
        this.dataBindingName = dataBindingName;
    }

    TypeSpec generate(TypeElement model) {

        TypeSpec.Builder viewHolderClass = createClassBuilder(model);
        addFields(viewHolderClass);
        addConstructor(viewHolderClass);
        addBindMethod(model, viewHolderClass);

        return viewHolderClass.build();
    }

    private void addBindMethod(TypeElement model, TypeSpec.Builder viewHolderClass) {
        MethodSpec.Builder bindMethod = MethodSpec.methodBuilder("bind").
                addParameter(TypeName.get(model.asType()), "model").
                addCode("binding.set" + model.getSimpleName() + "(model);\n").
                addModifiers(Modifier.PUBLIC);


        viewHolderClass.addMethod(bindMethod.build());
    }

    private void addConstructor(TypeSpec.Builder viewHolderClass) {
        ClassName viewClassName = ClassName.get("android.view", "View");
        MethodSpec constructor = MethodSpec.constructorBuilder().
                addParameter(viewClassName, "itemView").
                addParameter(dataBindingName, "binding").
                addCode("super(itemView);\nthis.binding = binding;\n").build();

        viewHolderClass.addMethod(constructor);
    }

    private void addFields(TypeSpec.Builder viewHolderClass) {
        viewHolderClass.addField(FieldSpec.builder(dataBindingName, "binding", Modifier.PRIVATE, Modifier.FINAL).build());
    }

    private TypeSpec.Builder createClassBuilder(TypeElement model) {
        ClassName recyclerViewClassName = ClassName.get("android.support.v7.widget", "RecyclerView");

        ClassName viewHolderClassName = recyclerViewClassName.nestedClass("ViewHolder");

        ClassName viewHolderBinderClassName = ClassName.get("com.example.api.adapter", "Binder");
        ParameterizedTypeName viewHolderBinderTypeName = ParameterizedTypeName.get(viewHolderBinderClassName, WildcardTypeName.get(model.asType()));

        return TypeSpec
                .classBuilder(viewHolderSimpleName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(viewHolderClassName)
                .addSuperinterface(viewHolderBinderTypeName);
    }

}
