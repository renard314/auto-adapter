package com.example;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import javax.tools.Diagnostic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

@SupportedAnnotationTypes("com.example.AdapterItem")
public class AutoAdapterProcessor extends AbstractProcessor {
    public AutoAdapterProcessor() {
        super();
    }

    private Filer filer;
    private Messager messager;
    private Map<String, List<TypeElement>> adaptersWithModels;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        adaptersWithModels = new HashMap<>();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
        if (findAllAnnotatedClasses(roundEnvironment)) {
            return true;
        }

        List<TypeSpec> typeSpecs = generateTypes();

        try {
            saveGeneratedTypes(typeSpecs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private List<TypeSpec> generateTypes() {

        List<TypeSpec> typeSpecs = new ArrayList<>();
        for (Map.Entry<String, List<TypeElement>> element : adaptersWithModels.entrySet()) {
            Map<TypeElement, ClassName> modelToFactory = new HashMap<>();
            int viewType = 0;
            for (TypeElement model : element.getValue()) {
                ClassName viewHolderClassName = getViewHolderClassName(model);
                ClassName viewHolderFactoryClassName = getViewHolderFactoryClassName(viewHolderClassName);
                ClassName classNameForDataBinding = getClassNameForDataBinding(model);

                ViewHolderGenerator viewHolderGenerator = new ViewHolderGenerator(viewHolderClassName,
                        classNameForDataBinding);
                ViewHolderFactoryGenerator viewHolderFactoryGenerator = new ViewHolderFactoryGenerator(
                        viewHolderFactoryClassName, classNameForDataBinding, viewHolderClassName, viewType++);

                TypeSpec viewHolder = viewHolderGenerator.generate(model);
                TypeSpec viewHolderFactory = viewHolderFactoryGenerator.generate();

                typeSpecs.add(viewHolder);
                typeSpecs.add(viewHolderFactory);

                modelToFactory.put(model, viewHolderFactoryClassName);
            }

            AdapterGenerator adapterGenerator = new AdapterGenerator(element.getKey(), modelToFactory);
            TypeSpec adapter = adapterGenerator.generate();
            typeSpecs.add(adapter);

        }

        return typeSpecs;
    }

    private void saveGeneratedTypes(final List<TypeSpec> typeSpecs) throws IOException {
        for (TypeSpec spec : typeSpecs) {
            JavaFile.Builder builder = JavaFile.builder(getRootPackage(), spec);
            JavaFile file = builder.build();
            file.writeTo(filer);
        }
    }

    // TODO find automatically
    // https://stackoverflow.com/questions/38005102/get-android-application-package-name-during-annotation-processing
    // https://stackoverflow.com/questions/43477024/get-android-r-class-when-passing-value-with-annotation?rq=1
    private String getRootPackage() {
        return "com.example.rwellnitz.tasktest";
    }

    // TODO get databinding name from parameter
    private ClassName getClassNameForDataBinding(final TypeElement model) {
        return ClassName.get(getRootPackage() + ".databinding", model.getSimpleName() + "ItemBinding");
    }

    private ClassName getViewHolderClassName(final TypeElement model) {
        return ClassName.get(getRootPackage(), model.getSimpleName() + "ViewHolder");
    }

    private ClassName getViewHolderFactoryClassName(final ClassName viewHolderClassName) {
        return ClassName.get(getRootPackage(), viewHolderClassName.simpleName() + "Factory");
    }

    private boolean findAllAnnotatedClasses(final RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(AdapterItem.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            AdapterItem annotation = element.getAnnotation(AdapterItem.class);
            List<TypeElement> strings = adaptersWithModels.get(annotation.value());
            if (strings == null) {
                strings = new ArrayList<>();
                adaptersWithModels.put(annotation.value(), strings);
            }

            strings.add(typeElement);
        }

        return false;
    }

}
