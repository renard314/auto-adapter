package com.renard.auto_adapter.processor.code_generation;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.renard.auto_adapter.processor.AutoAdapterProcessor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ViewHolderFactoryGenerator {
    private final ClassName viewHolderFactoryClassName;
    private final ClassName dataBindingClassName;
    private final ClassName viewBinderClassName;
    private final int viewType;
    private final ClassName viewHolderClassName;

    public ViewHolderFactoryGenerator(final ClassName viewHolderFactoryClassName, final ClassName dataBindingClassName,
            final ClassName viewBinderClassName, final int viewType, final ClassName viewHolderClassName) {
        this.viewHolderFactoryClassName = viewHolderFactoryClassName;
        this.dataBindingClassName = dataBindingClassName;
        this.viewBinderClassName = viewBinderClassName;
        this.viewType = viewType;
        this.viewHolderClassName = viewHolderClassName;
    }

    public TypeSpec generate(final TypeElement model) {
        TypeSpec.Builder classBuilder = createBuilder(model);

        addCreateMethod(classBuilder, model);
        addGetViewTypeMethod(classBuilder);

        return classBuilder.build();
    }

    private void addGetViewTypeMethod(final TypeSpec.Builder viewHolderFactoryClass) {
        MethodSpec.Builder getViewTypeMethod = MethodSpec.methodBuilder("getViewType").addCode("return $L;\n", viewType)
                                                         .returns(TypeName.INT).addModifiers(Modifier.PUBLIC);

        viewHolderFactoryClass.addMethod(getViewTypeMethod.build());
    }

    private void addCreateMethod(final TypeSpec.Builder viewHolderFactoryClass, final TypeElement model) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$T from = LayoutInflater.from(parent.getContext());\n", AndroidClassNames.INFLATER)
               .add("$1T binding = $2T.inflate(from, parent, false);\n", dataBindingClassName, dataBindingClassName)
               .add("$1T binder = new $2T(binding);\n", viewBinderClassName, viewBinderClassName)
               .add("return new AutoAdapterViewHolder<>(binding.getRoot(),binder);\n").build();

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(viewHolderClassName,
                TypeName.get(model.asType()));

        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create").returns(parameterizedTypeName)
                                                    .addParameter(AndroidClassNames.VIEW_GROUP, "parent")
                                                    .addCode(builder.build()).addModifiers(Modifier.PUBLIC);

        viewHolderFactoryClass.addMethod(createMethod.build());
    }

    private TypeSpec.Builder createBuilder(final TypeElement model) {
        ClassName factoryClassName = ClassName.get(AutoAdapterProcessor.LIBRARY_PACKAGE, "ViewHolderFactory");

        ParameterizedTypeName factoryTypeName = ParameterizedTypeName.get(factoryClassName,
                ParameterizedTypeName.get(model.asType()));

        return TypeSpec.classBuilder(viewHolderFactoryClassName).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                       .addSuperinterface(factoryTypeName);
    }
}
