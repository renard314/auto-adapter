package com.renard.auto_adapter.processor;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.renard.auto_adapter.AdapterItem;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.renard.auto_adapter.AdapterItem")
public class AutoAdapterProcessor extends AbstractProcessor {
    static final String LIBRARY_PACKAGE = "com.renard.auto_adapter";

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
            //second round
            List<TypeSpec> typeSpecs = generateViewHoldersAndFactories(roundEnvironment);
            viewHolderInfos.clear();
            saveGeneratedTypes(typeSpecs);
            return true;
        }

        // first round
        Map<String, List<TypeElement>> adaptersWithModels = findAllClassesAnnotatedWithAdapterItem(roundEnvironment);
        if (adaptersWithModels.isEmpty()) {
            return true;
        }

        List<TypeSpec> typeSpecs = generateAdapters(adaptersWithModels);

        saveGeneratedTypes(typeSpecs);

        return true;
    }

    private List<TypeSpec> generateViewHoldersAndFactories(RoundEnvironment roundEnvironment) {

        Set<? extends Element> rootElements = roundEnvironment.getRootElements();
        int viewType = 0;
        List<TypeSpec> typeSpecs = new ArrayList<>();

        for (ViewHolderInfo viewHolderInfo : viewHolderInfos) {

            Optional<ClassName> classNameForDataBinding = findDataBindingForModel(viewHolderInfo.model, rootElements);
            if (!classNameForDataBinding.isPresent()) {
                continue;
            }

            ViewHolderGenerator viewHolderGenerator = new ViewHolderGenerator(viewHolderInfo.viewHolderClassName, classNameForDataBinding.get());

            ViewHolderFactoryGenerator viewHolderFactoryGenerator = new ViewHolderFactoryGenerator(
                    viewHolderInfo.viewHolderFactoryClassName, classNameForDataBinding.get(), viewHolderInfo.viewHolderClassName, viewType++);

            TypeSpec viewHolder = viewHolderGenerator.generate(viewHolderInfo.model);
            TypeSpec viewHolderFactory = viewHolderFactoryGenerator.generate();

            typeSpecs.add(viewHolder);
            typeSpecs.add(viewHolderFactory);
        }
        return typeSpecs;
    }

    private Optional<ClassName> findDataBindingForModel(TypeElement model, Set<? extends Element> rootElements) {
        List<Element> bindingCandidates = new ArrayList<>();
        for (Element element : rootElements) {
            TypeMirror typeMirror = element.asType();
            List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(typeMirror);
            for (TypeMirror superType : supertypes) {
                String classNameOfSuperClass = getClassNameOf((DeclaredType) superType);
                if ((classNameOfSuperClass).equals(AndroidClassNames.VIEW_DATA_BINDING)) {

                    List<ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());
                    ImmutableList<ExecutableElement> setVariableMethod = FluentIterable.from(executableElements).
                            filter(ExecutableElementPredicates.isVoidMethod).
                            filter(ExecutableElementPredicates.hasOneParameter).
                            filter(ExecutableElementPredicates.startsWithSet).toList();


                    boolean hasSetMethodForModel = Collections2.filter(setVariableMethod, ExecutableElementPredicates.parameterIsSameType(model)).size() == 1;

                    if (hasSetMethodForModel && setVariableMethod.size() > 1) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "ViewDataBinding for " + model.getSimpleName() + " has more than one variable.", model);
                        return Optional.absent();
                    } else if (hasSetMethodForModel) {
                        bindingCandidates.add(element);
                    }

                }
            }
        }

        if (bindingCandidates.size() == 1) {
            Element dataBindingElement = bindingCandidates.get(0);
            PackageElement dataBindingElementPackage = elementUtils.getPackageOf(dataBindingElement);
            String qualifiedName = dataBindingElementPackage.getQualifiedName().toString();
            return Optional.of(ClassName.get(qualifiedName, dataBindingElement.getSimpleName().toString()));
        } else if (bindingCandidates.size() > 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, "There is more than one ViewDataBinding for " + model.getSimpleName(), model);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Can't find ViewDataBinding for " + model.getSimpleName(), model);
        }

        return Optional.absent();

    }

    private String getClassNameOf(DeclaredType superType) {
        Element supertypeElement = superType.asElement();
        PackageElement packageOf = elementUtils.getPackageOf(superType.asElement());
        String simpleName = supertypeElement.getSimpleName().toString();
        return packageOf + "." + simpleName;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private List<TypeSpec> generateAdapters(Map<String, List<TypeElement>> adaptersWithModels) {

        List<TypeSpec> typeSpecs = new ArrayList<>();
        for (Map.Entry<String, List<TypeElement>> element : adaptersWithModels.entrySet()) {
            Map<TypeElement, ClassName> modelToFactory = new HashMap<>();
            for (TypeElement model : element.getValue()) {
                ClassName viewHolderClassName = getViewHolderClassName(model);
                ClassName viewHolderFactoryClassName = getViewHolderFactoryClassName(viewHolderClassName);
                viewHolderInfos.add(new ViewHolderInfo(viewHolderClassName, viewHolderFactoryClassName, model));
                modelToFactory.put(model, viewHolderFactoryClassName);
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

    private ClassName getViewHolderClassName(final TypeElement model) {
        return ClassName.get(LIBRARY_PACKAGE, model.getSimpleName() + "ViewHolder");
    }

    private ClassName getViewHolderFactoryClassName(final ClassName viewHolderClassName) {
        return ClassName.get(LIBRARY_PACKAGE, viewHolderClassName.simpleName() + "Factory");
    }

    private Map<String, List<TypeElement>> findAllClassesAnnotatedWithAdapterItem(final RoundEnvironment roundEnvironment) {
        Map<String, List<TypeElement>> result = new HashMap<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(AdapterItem.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.WARNING, "AdapterItem can only be applied to a class.");
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            AdapterItem annotation = element.getAnnotation(AdapterItem.class);
            List<TypeElement> strings = result.get(annotation.value());
            if (strings == null) {
                strings = new ArrayList<>();
                result.put(annotation.value(), strings);
            }

            strings.add(typeElement);
        }
        return result;
    }

}
