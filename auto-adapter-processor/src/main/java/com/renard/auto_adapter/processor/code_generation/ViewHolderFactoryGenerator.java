package com.renard.auto_adapter.processor.code_generation;

import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.renard.auto_adapter.processor.AutoAdapterProcessor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ViewHolderFactoryGenerator {
    private final ClassName viewHolderFactoryClassName;
    private final ClassName viewBinderClassName;
    private final int viewType;
    private final Set<Integer> viewIds;

    public ViewHolderFactoryGenerator(final ClassName viewHolderFactoryClassName, final ClassName viewBinderClassName,
            final int viewType, final Set<Integer> viewIds) {

        this.viewHolderFactoryClassName = viewHolderFactoryClassName;
        this.viewBinderClassName = viewBinderClassName;
        this.viewType = viewType;
        this.viewIds = viewIds;
    }

    public TypeSpec generate(final TypeElement model, final ClassName dataBindingClassName,
            final ClassName viewHolderClassName) {

        TypeSpec.Builder classBuilder = createBuilder(model);

        addFields(classBuilder);
        addCreateMethod(classBuilder, model, codeBlockForCreate(dataBindingClassName), viewHolderClassName);
        addGetViewTypeMethod(classBuilder);

        return classBuilder.build();
    }

    private void addFields(final TypeSpec.Builder classBuilder) {
        FieldSpec.Builder viewIdsField = FieldSpec.builder(int[].class, "viewIds", Modifier.PRIVATE, Modifier.STATIC,
                Modifier.FINAL);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Integer id : viewIds) {
            sb.append(id).append(",");
        }

        if (viewIds.size() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append("}");
        viewIdsField.initializer(CodeBlock.of(sb.toString()));
        classBuilder.addField(viewIdsField.build());

    }

    public TypeSpec generate(final TypeElement model, final ClassName viewHolderClassName) {
        TypeSpec.Builder classBuilder = createBuilder(model);

        addFields(classBuilder);
        addCreateMethod(classBuilder, model, codeBlockForCreate(), viewHolderClassName);
        addGetViewTypeMethod(classBuilder);

        return classBuilder.build();
    }

    private void addGetViewTypeMethod(final TypeSpec.Builder viewHolderFactoryClass) {
        MethodSpec.Builder getViewTypeMethod = MethodSpec.methodBuilder("getViewType").addCode("return $L;\n", viewType)
                                                         .returns(TypeName.INT).addModifiers(Modifier.PUBLIC);

        viewHolderFactoryClass.addMethod(getViewTypeMethod.build());
    }

    private CodeBlock codeBlockForCreate() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$T from = LayoutInflater.from(parent.getContext());\n", AndroidClassNames.INFLATER)
               .add("$1T binder = new $1T();\n", viewBinderClassName)
               .add("$T view = from.inflate(binder.getLayoutResourceId(), parent, false);\n", AndroidClassNames.VIEW)
               .add("binder.createView(view);\n").add("return new AutoAdapterViewHolder<>(view,binder,viewIds);\n")
               .build();

        return builder.build();
    }

    private CodeBlock codeBlockForCreate(final ClassName dataBindingClassName) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$T from = LayoutInflater.from(parent.getContext());\n", AndroidClassNames.INFLATER)
               .add("$1T binding = $2T.inflate(from, parent, false);\n", dataBindingClassName, dataBindingClassName)
               .add("$1T binder = new $2T(binding);\n", viewBinderClassName, viewBinderClassName)
               .add("return new AutoAdapterViewHolder<>(binding.getRoot(),binder,viewIds);\n").build();
        return builder.build();
    }

    private void addCreateMethod(final TypeSpec.Builder viewHolderFactoryClass, final TypeElement model,
            final CodeBlock codeBlock, final ClassName viewHolderClassName) {

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(viewHolderClassName,
                TypeName.get(model.asType()));

        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create").returns(parameterizedTypeName)
                                                    .addParameter(AndroidClassNames.VIEW_GROUP, "parent")
                                                    .addCode(codeBlock).addModifiers(Modifier.PUBLIC);

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
