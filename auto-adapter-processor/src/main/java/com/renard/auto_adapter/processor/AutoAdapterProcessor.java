package com.renard.auto_adapter.processor;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import javax.tools.Diagnostic;

import com.google.common.base.Optional;

import com.renard.auto_adapter.processor.code_generation.AdapterGenerator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

@SupportedAnnotationTypes("com.renard.auto_adapter.AdapterItem")
public class AutoAdapterProcessor extends AbstractProcessor {
    public static final String LIBRARY_PACKAGE = "com.renard.auto_adapter";
    private static final String ADAPTER_ITEM_NAME = "com.renard.auto_adapter.AdapterItem";
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Set<ViewHolderInfo> viewHolderInfos = new LinkedHashSet<>();

    public AutoAdapterProcessor() {
        super();
    }

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

        if (!viewHolderInfos.isEmpty()) {
            generateViewHoldersAndFactories(roundEnvironment);
        } else {
            generateAdapters(roundEnvironment);
        }

        return true;
    }

    /**
     * generate adapters and prepare to generate the ViewHolders and ViewHolderFactories.
     */
    private void generateAdapters(final RoundEnvironment roundEnvironment) {
        Map<String, List<TypeElement>> adaptersWithModels = findAllClassesAnnotatedWithAdapterItem(roundEnvironment);
        if (adaptersWithModels.isEmpty()) {
            return;
        }

        List<TypeSpec> typeSpecs = generateAdapters(adaptersWithModels);

        saveGeneratedTypes(typeSpecs);
    }

    /**
     * generate the ViewHolders and ViewHolderFactories.
     */
    private void generateViewHoldersAndFactories(final RoundEnvironment roundEnvironment) {
        List<TypeSpec> typeSpecs = generateViewHoldersAndFactories(roundEnvironment.getRootElements());
        viewHolderInfos.clear();
        saveGeneratedTypes(typeSpecs);
    }

    private List<TypeSpec> generateViewHoldersAndFactories(final Set<? extends Element> rootElements) {

        List<TypeSpec> typeSpecs = new ArrayList<>();

        for (ViewHolderInfo viewHolderInfo : viewHolderInfos) {

            Optional<Element> classNameForDataBinding = viewHolderInfo.findDataBindingForModel(rootElements);
            if (!classNameForDataBinding.isPresent()) {
                return Collections.emptyList();
            }

            TypeSpec viewHolderFactory = viewHolderInfo.generateViewHolderFactory(classNameForDataBinding.get());
            TypeSpec viewHolder = viewHolderInfo.generateViewHolder(classNameForDataBinding.get());

            typeSpecs.add(viewHolder);
            typeSpecs.add(viewHolderFactory);
        }

        return typeSpecs;
    }

    private List<TypeSpec> generateAdapters(final Map<String, List<TypeElement>> adaptersWithModels) {

        List<TypeSpec> typeSpecs = new ArrayList<>();
        for (Map.Entry<String, List<TypeElement>> element : adaptersWithModels.entrySet()) {
            Map<TypeElement, ClassName> modelToFactory = new HashMap<>();
            for (TypeElement model : element.getValue()) {
                ViewHolderInfo viewHolderInfo = new ViewHolderInfo(model, typeUtils, elementUtils, messager);
                viewHolderInfos.add(viewHolderInfo);
                modelToFactory.put(model, viewHolderInfo.viewHolderFactoryClassName);
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
                e.printStackTrace();
            }
        }
    }

    private Map<String, List<TypeElement>> findAllClassesAnnotatedWithAdapterItem(
            final RoundEnvironment roundEnvironment) {
        Map<String, List<TypeElement>> result = new HashMap<>();

        TypeElement annotation = elementUtils.getTypeElement(ADAPTER_ITEM_NAME);
        for (Element element : roundEnvironment.getElementsAnnotatedWith(annotation)) {

            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.WARNING, "AdapterItem can only be applied to a class.");
                continue;
            }

            TypeElement typeElement = (TypeElement) element;

            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                if (Util.typeToString(annotationMirror.getAnnotationType()).equals(ADAPTER_ITEM_NAME)) {
                    for (ExecutableElement executableElement : annotationMirror.getElementValues().keySet()) {
                        if ("viewHolder".equals(executableElement.getSimpleName().toString())) {
                            // TODO
                        } else if ("value".equals(executableElement.getSimpleName().toString())) {
                            AnnotationValue annotationValue = annotationMirror.getElementValues().get(
                                    executableElement);
                            Object value = annotationValue.getValue();
                            List<TypeElement> strings = result.get(value.toString());
                            if (strings == null) {
                                strings = new ArrayList<>();
                                result.put(value.toString(), strings);
                            }

                            strings.add(typeElement);
                        }
                    }

                }
            }
        }

        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
