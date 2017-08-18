package com.example;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

class AdapterGenerator {
    private final String adapterName;
    private final Map<TypeElement, ClassName> modelToFactory;

    AdapterGenerator(String adapterName, Map<TypeElement, ClassName> modelToFactory) {
        this.adapterName = adapterName;
        this.modelToFactory = modelToFactory;
    }

    TypeSpec generate() {
        TypeSpec.Builder adapterBuilder = createClassBuilder();
        addStaticCodeBlock(adapterBuilder);
        addFields(adapterBuilder);
        addConstructor(adapterBuilder);
        addMethods(adapterBuilder);

        return adapterBuilder.build();
    }

    private TypeSpec.Builder createClassBuilder() {
        ClassName parentClassName = ClassName.get("com.example.api.adapter", "AutoAdapter");

        return TypeSpec
                .classBuilder(adapterName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(parentClassName);
    }

    private void addMethods(TypeSpec.Builder adapterBuilder) {
        for (TypeElement model : modelToFactory.keySet()) {
            MethodSpec addMethod = MethodSpec.methodBuilder("add" + model.getSimpleName()).
                    addParameter(TypeName.get(model.asType()), "item").
                    addCode("addItem(item);\n").
                    addModifiers(Modifier.PUBLIC).build();
            adapterBuilder.addMethod(addMethod);
        }
    }

    private void addConstructor(TypeSpec.Builder adapterBuilder) {
        MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addCode("super(MAPPING);\n").build();
        adapterBuilder.addMethod(constructor);
    }

    private void addStaticCodeBlock(TypeSpec.Builder adapterBuilder) {
        CodeBlock.Builder staticCodeBlockBuilder = CodeBlock.builder();
        for (TypeElement model : modelToFactory.keySet()) {
            staticCodeBlockBuilder.add("MAPPING.put($1T.class, new $2T());\n", model, modelToFactory.get(model));
        }
        adapterBuilder.addStaticBlock(staticCodeBlockBuilder.build());
    }

    private void addFields(TypeSpec.Builder adapterBuilder) {
        adapterBuilder.addField(FieldSpec.builder(HashMap.class, "MAPPING", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC).initializer("new HashMap<>()").build());
    }
}