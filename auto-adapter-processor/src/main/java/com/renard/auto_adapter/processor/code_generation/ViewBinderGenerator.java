package com.renard.auto_adapter.processor.code_generation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.renard.auto_adapter.processor.AutoAdapterProcessor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ViewBinderGenerator {
    private static final ClassName VIEW_BINDER_NAME = ClassName.get(AutoAdapterProcessor.LIBRARY_PACKAGE, "ViewBinder");

    private final ClassName viewBinderClassName;
    private final ClassName dataBindingName;

    public ViewBinderGenerator(final ClassName viewBinderClassName, final ClassName dataBindingName) {
        this.viewBinderClassName = viewBinderClassName;
        this.dataBindingName = dataBindingName;
    }

    public TypeSpec generate(final TypeElement model, final ExecutableElement setVariableMethod) {

        TypeSpec.Builder builder = createClassBuilder(model);
        addFields(builder);
        addConstructor(builder);
        addBindMethod(setVariableMethod, builder);
        addGetLayoutResourceIdMethod(builder);

        return builder.build();
    }

    private void addBindMethod(final ExecutableElement setVariableMethod, final TypeSpec.Builder viewHolderClass) {
        VariableElement parameter = setVariableMethod.getParameters().get(0);
        MethodSpec.Builder bindMethod = MethodSpec.methodBuilder("bind")
                                                  .addParameter(TypeName.get(parameter.asType()), "model")
                                                  .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).addCode(
                                                      "binding." + setVariableMethod.getSimpleName().toString()
                                                          + "(model);\n");

        viewHolderClass.addMethod(bindMethod.build());
    }

    private void addGetLayoutResourceIdMethod(final TypeSpec.Builder viewHolderClass) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("getLayoutResourceId").addAnnotation(Override.class)
                                              .addCode("return 0;\n").addModifiers(Modifier.PUBLIC).returns(
                                                  TypeName.INT);

        viewHolderClass.addMethod(method.build());
    }

    private void addConstructor(final TypeSpec.Builder viewHolderClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder().addParameter(dataBindingName, "binding")
                                           .addCode("this.binding = binding;\n").build();

        viewHolderClass.addMethod(constructor);
    }

    private void addFields(final TypeSpec.Builder viewHolderClass) {
        FieldSpec.Builder builder = FieldSpec.builder(dataBindingName, "binding", Modifier.PRIVATE, Modifier.FINAL);
        viewHolderClass.addField(builder.build());
    }

    private TypeSpec.Builder createClassBuilder(final TypeElement model) {

        ParameterizedTypeName viewHolderBinderTypeName = ParameterizedTypeName.get(VIEW_BINDER_NAME,
                ParameterizedTypeName.get(model.asType()));

        return TypeSpec.classBuilder(viewBinderClassName).addModifiers(Modifier.FINAL).addSuperinterface(
                viewHolderBinderTypeName);
    }

}
