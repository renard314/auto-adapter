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
import com.squareup.javapoet.WildcardTypeName;

public class ViewHolderGenerator {
    private static final ClassName AUTO_ADAPTER_VIEW_HOLDER_NAME = ClassName.get(AutoAdapterProcessor.LIBRARY_PACKAGE,
            "AutoAdapterViewHolder");
    private final ClassName viewHolderSimpleName;
    private final ClassName dataBindingName;

    public ViewHolderGenerator(final ClassName viewHolderSimpleName, final ClassName dataBindingName) {
        this.viewHolderSimpleName = viewHolderSimpleName;
        this.dataBindingName = dataBindingName;
    }

    public TypeSpec generate(final TypeElement model, final ExecutableElement setVariableMethod) {

        TypeSpec.Builder viewHolderClass = createClassBuilder(model);
        addFields(viewHolderClass);
        addConstructor(viewHolderClass);
        addBindMethod(setVariableMethod, viewHolderClass);
        addGetLayoutResourceIdMethod(viewHolderClass);

        return viewHolderClass.build();
    }

    private void addGetLayoutResourceIdMethod(final TypeSpec.Builder viewHolderClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getLayoutResourceId").addCode("return 0;\n")
                                               .addAnnotation(Override.class)
                                               .addAnnotation(AndroidClassNames.LAYOUT_RES).returns(TypeName.INT)
                                               .addModifiers(Modifier.PUBLIC);

        viewHolderClass.addMethod(builder.build());

    }

    private void addBindMethod(final ExecutableElement setVariableMethod, final TypeSpec.Builder viewHolderClass) {
        VariableElement parameter = setVariableMethod.getParameters().get(0);
        MethodSpec.Builder bindMethod = MethodSpec.methodBuilder("bind")
                                                  .addParameter(TypeName.get(parameter.asType()), "model")
                                                  .addAnnotation(Override.class)
                                                  .addCode("binding." + setVariableMethod.getSimpleName().toString()
                    + "(model);\n").addModifiers(Modifier.PUBLIC);

        viewHolderClass.addMethod(bindMethod.build());
    }

    private void addConstructor(final TypeSpec.Builder viewHolderClass) {
        MethodSpec constructor = MethodSpec.constructorBuilder().addParameter(AndroidClassNames.VIEW, "itemView")
                                           .addParameter(dataBindingName, "binding")
                                           .addCode("super(itemView);\nthis.binding = binding;\n").build();

        viewHolderClass.addMethod(constructor);
    }

    private void addFields(final TypeSpec.Builder viewHolderClass) {
        viewHolderClass.addField(FieldSpec.builder(dataBindingName, "binding", Modifier.PRIVATE, Modifier.FINAL)
                .build());
    }

    private TypeSpec.Builder createClassBuilder(final TypeElement model) {

        ParameterizedTypeName viewHolderBinderTypeName = ParameterizedTypeName.get(AUTO_ADAPTER_VIEW_HOLDER_NAME,
                WildcardTypeName.get(model.asType()));

        return TypeSpec.classBuilder(viewHolderSimpleName).addModifiers(Modifier.PUBLIC, Modifier.FINAL).superclass(
                viewHolderBinderTypeName);
    }

}
