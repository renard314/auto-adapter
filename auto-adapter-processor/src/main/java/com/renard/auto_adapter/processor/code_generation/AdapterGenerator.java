package com.renard.auto_adapter.processor.code_generation;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.renard.auto_adapter.processor.AutoAdapterProcessor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class AdapterGenerator {

    private final String adapterName;
    private final Map<TypeElement, ClassName> modelToFactory;

    public AdapterGenerator(final String adapterName, final Map<TypeElement, ClassName> modelToFactory) {
        this.adapterName = adapterName;
        this.modelToFactory = modelToFactory;
    }

    public TypeSpec generate() {
        TypeSpec.Builder adapterBuilder = createClassBuilder();

        addConstructor(adapterBuilder);
        addMethods(adapterBuilder);

        return adapterBuilder.build();
    }

    private TypeSpec.Builder createClassBuilder() {
        ClassName parentClassName = ClassName.get(AutoAdapterProcessor.LIBRARY_PACKAGE, "AutoAdapter");

        return TypeSpec.classBuilder(adapterName).addModifiers(Modifier.PUBLIC, Modifier.FINAL).superclass(
                parentClassName);
    }

    private void addMethods(final TypeSpec.Builder adapterBuilder) {
        for (TypeElement model : modelToFactory.keySet()) {
            MethodSpec addMethod = MethodSpec.methodBuilder("add" + model.getSimpleName())
                                             .addParameter(TypeName.get(model.asType()), "item")
                                             .addCode("addItem(item);\n").addModifiers(Modifier.PUBLIC).build();

            MethodSpec removeMethod = MethodSpec.methodBuilder("remove" + model.getSimpleName())
                                                .addParameter(TypeName.get(model.asType()), "item")
                                                .addCode("removeItem(item);\n").addModifiers(Modifier.PUBLIC).build();

            adapterBuilder.addMethod(addMethod);
            adapterBuilder.addMethod(removeMethod);
        }
    }

    private void addConstructor(final TypeSpec.Builder adapterBuilder) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

        for (TypeElement model : modelToFactory.keySet()) {
            builder.addCode("putMapping($1T.class, new $2T());\n", model, modelToFactory.get(model));
        }

        adapterBuilder.addMethod(builder.build());
    }

}
