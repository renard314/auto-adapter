package com.renard.auto_adapter.processor;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.renard.auto_adapter.processor.code_generation.AdapterGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.renard.auto_adapter.processor.AutoAdapterProcessor.ADAPTER_ITEM_ANNOTATION_NAME;

@SupportedAnnotationTypes(ADAPTER_ITEM_ANNOTATION_NAME)
public class AutoAdapterProcessor extends AbstractProcessor {

    public static final String LIBRARY_PACKAGE = "com.renard.auto_adapter";
    static final String ADAPTER_ITEM_ANNOTATION_NAME = "com.renard.auto_adapter.AdapterItem";

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Set<AnnotatedModel> annotatedModelses = new LinkedHashSet<>();

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnvironment) {

        List<TypeSpec> typeSpecs = new ArrayList<>();
        boolean hasProcessedAnnotation = false;

        if (!annotations.isEmpty()) {
            List<TypeSpec> adapters = generateAdapters(roundEnvironment);
            typeSpecs.addAll(adapters);
            hasProcessedAnnotation = !adapters.isEmpty();
        }

        for (Iterator<AnnotatedModel> iterator = annotatedModelses.iterator(); iterator.hasNext(); ) {
            AnnotatedModel annotatedModel = iterator.next();
            List<TypeSpec> spec = annotatedModel.generateTypeSpecs(roundEnvironment);
            if (!spec.isEmpty()) {
                iterator.remove();
                typeSpecs.addAll(spec);
            }

        }

        saveGeneratedTypes(typeSpecs);

        return hasProcessedAnnotation;
    }

    private List<TypeSpec> generateAdapters(final RoundEnvironment roundEnvironment) {
        Map<String, List<TypeElement>> adapterNamesToModels = findAllClassesAnnotatedWithAdapterItem(roundEnvironment);
        if (adapterNamesToModels.isEmpty()) {
            return Collections.emptyList();
        }

        return generateAdapters(adapterNamesToModels);
    }

    private List<TypeSpec> generateAdapters(final Map<String, List<TypeElement>> adaptersWithModels) {

        List<TypeSpec> typeSpecs = new ArrayList<>();
        for (Map.Entry<String, List<TypeElement>> element : adaptersWithModels.entrySet()) {
            Map<TypeElement, ClassName> modelToFactory = new HashMap<>();
            for (TypeElement model : element.getValue()) {

                AnnotationMirror annotation = Collections2.filter(model.getAnnotationMirrors(), Predicates.IS_ADAPTER_ITEM)
                        .iterator().next();
                Collection<? extends AnnotationValue> values = Maps.filterKeys(annotation.getElementValues(),
                        Predicates.IS_VIEW_BINDER).values();


                Optional<AnnotationValue> value;
                if (values.isEmpty()) {
                    value = Optional.absent();
                } else {
                    AnnotationValue firstValue = values.iterator().next();
                    if (firstValue.getValue().equals("<error>")) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Specified ViewBinder is not a class.", model, annotation);
                        return Collections.emptyList();
                    }
                    value = Optional.of(firstValue);

                }

                AnnotatedModel annotatedModel = new AnnotatedModel(model, typeUtils, elementUtils, messager, value);
                annotatedModelses.add(annotatedModel);
                modelToFactory.put(model, annotatedModel.viewHolderFactoryClassName);
            }

            AdapterGenerator adapterGenerator = new AdapterGenerator(element.getKey(), modelToFactory);
            TypeSpec adapter = adapterGenerator.generate();
            typeSpecs.add(adapter);

        }

        return typeSpecs;
    }

    private void saveGeneratedTypes(final List<TypeSpec> typeSpecs) {
        for (TypeSpec spec : typeSpecs) {
            JavaFile.Builder builder = JavaFile.builder(LIBRARY_PACKAGE, spec);
            JavaFile file = builder.build();
            try {
                file.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Map<String, List<TypeElement>> findAllClassesAnnotatedWithAdapterItem(
            final RoundEnvironment roundEnvironment) {
        Map<String, List<TypeElement>> result = new HashMap<>();

        TypeElement adapterItemElement = elementUtils.getTypeElement(ADAPTER_ITEM_ANNOTATION_NAME);
        for (Element element : roundEnvironment.getElementsAnnotatedWith(adapterItemElement)) {

            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.WARNING, "AdapterItem can only be applied to a class.");
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            AnnotationMirror annotation = Collections2.filter(typeElement.getAnnotationMirrors(), Predicates.IS_ADAPTER_ITEM)
                    .iterator().next();
            AnnotationValue annotationValue = Maps.filterKeys(annotation.getElementValues(),
                    Predicates.IS_ANNOTATION_VALUE).values().iterator().next();
            Object value = annotationValue.getValue();
            List<TypeElement> strings = result.get(value.toString());
            if (strings == null) {
                strings = new ArrayList<>();
                result.put(value.toString(), strings);
            }

            strings.add(typeElement);
        }

        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
