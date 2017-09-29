package com.renard.auto_adapter.processor;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.renard.auto_adapter.processor.code_generation.ViewBinderGenerator;
import com.renard.auto_adapter.processor.code_generation.ViewHolderFactoryGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.renard.auto_adapter.processor.AutoAdapterProcessor.LIBRARY_PACKAGE;

class AnnotatedModel {

    private static final ClassName VIEW_HOLDER_CLASS_NAME = ClassName.get(LIBRARY_PACKAGE, "AutoAdapterViewHolder");
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
    final ClassName viewHolderFactoryClassName;
    private final ClassName viewBinderClassName;
    private final Set<Integer> viewIds;
    private final TypeElement model;
    private final Optional<AnnotationValue> annotationValue;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final Messager messager;

    TypeElement getModelType() {
        return model;
    }

    AnnotatedModel(final TypeElement model, final Types typeUtils, final Elements elementUtils, final Messager messager,
                   final Optional<AnnotationValue> annotationValue, Set<Integer> viewIds) {

        this.annotationValue = annotationValue;
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.viewHolderFactoryClassName = getViewHolderFactoryClassName(model);
        this.model = model;
        this.viewBinderClassName = getViewBinderClassName(model);
        this.viewIds = viewIds;
    }

    private ClassName getViewBinderClassName(final TypeElement model) {
        if (annotationValue.isPresent()) {
            Object value = annotationValue.get().getValue();
            if (value instanceof String) {
                return ClassName.bestGuess((String) value);
            } else {
                TypeMirror mirror = (TypeMirror) annotationValue.get().getValue();
                return getClassNameFrom(typeUtils.asElement(mirror));
            }

        } else {
            return ClassName.get(LIBRARY_PACKAGE, model.getSimpleName() + "ViewBinder");
        }
    }

    private ClassName getViewHolderFactoryClassName(final TypeElement model) {
        return ClassName.get(LIBRARY_PACKAGE, model.getSimpleName() + "ViewHolderFactory");
    }

    private Optional<Element> findDataBinding(final Set<? extends Element> rootElements) {
        List<Element> bindingCandidates = new ArrayList<>();
        for (Element element : rootElements) {
            TypeMirror typeMirror = element.asType();
            List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(typeMirror);
            if (!Collections2.filter(supertypes, Predicates.IS_VIEW_DATA_BINDING).isEmpty()) {

                List<ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());
                ImmutableList<ExecutableElement> setVariableMethod = FluentIterable.from(executableElements)
                        .filter(Predicates.IS_VOID_METHOD)
                        .filter(Predicates.HAS_ONE_PARAMETER)
                        .filter(Predicates.STARTS_WITH_SET)
                        .toList();

                boolean hasSetMethodForModel = Collections2.filter(setVariableMethod,
                        Predicates.parameterIsSameType(model)).size() == 1;

                if (hasSetMethodForModel && setVariableMethod.size() > 1) {
                    messager.printMessage(Diagnostic.Kind.WARNING,
                            "ViewDataBinding for " + model.getSimpleName() + " has more than one variable.", model);
                    return Optional.absent();
                } else if (hasSetMethodForModel) {
                    bindingCandidates.add(element);
                }
            }

        }

        if (bindingCandidates.size() == 1) {
            return Optional.of(bindingCandidates.get(0));
        } else if (bindingCandidates.size() > 1) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "There is more than one ViewDataBinding for " + model.getSimpleName(), model);
        }

        return Optional.absent();
    }

    private TypeSpec generateViewBinder(final Element element) {
        ClassName className = getClassNameFrom(element);

        List<ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());
        ExecutableElement setVariableMethod = FluentIterable.from(executableElements).filter(Predicates.IS_VOID_METHOD)
                .filter(Predicates.HAS_ONE_PARAMETER)
                .filter(Predicates.STARTS_WITH_SET)
                .filter(Predicates.parameterIsSameType(model)).first()
                .get();

        ViewBinderGenerator viewBinderGenerator = new ViewBinderGenerator();
        return viewBinderGenerator.generate(model, setVariableMethod, viewBinderClassName, className);
    }

    private TypeSpec generateViewHolderFactoryWithDataBinding(final Element dataBinding) {
        ClassName dataBindingClassName = getClassNameFrom(dataBinding);
        ViewHolderFactoryGenerator viewHolderFactoryGenerator = new ViewHolderFactoryGenerator(
                viewHolderFactoryClassName, viewBinderClassName, NEXT_ID.getAndIncrement(), viewIds);
        return viewHolderFactoryGenerator.generate(model, dataBindingClassName, VIEW_HOLDER_CLASS_NAME);
    }

    private TypeSpec generateViewHolderFactory() {
        ViewHolderFactoryGenerator viewHolderFactoryGenerator = new ViewHolderFactoryGenerator(
                viewHolderFactoryClassName, viewBinderClassName, NEXT_ID.getAndIncrement(), viewIds);
        return viewHolderFactoryGenerator.generate(model, VIEW_HOLDER_CLASS_NAME);
    }

    private ClassName getClassNameFrom(final Element element) {
        PackageElement dataBindingElementPackage = elementUtils.getPackageOf(element);
        String qualifiedName = dataBindingElementPackage.getQualifiedName().toString();
        return ClassName.get(qualifiedName, element.getSimpleName().toString());
    }

    List<TypeSpec> generateTypeSpecs(final RoundEnvironment roundEnvironment) {
        List<TypeSpec> typeSpecs = new ArrayList<>();
        if (annotationValue.isPresent()) {
            if (annotationValue.get() instanceof TypeMirror) {
                TypeMirror mirror = (TypeMirror) annotationValue.get().getValue();
                Element viewBinder = typeUtils.asElement(mirror);
                ExecutableElement noArgsConstructor = Util.getNoArgsConstructor(viewBinder);
                if (noArgsConstructor != null) {
                    TypeSpec viewHolderFactory = generateViewHolderFactory();
                    typeSpecs.add(viewHolderFactory);
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, viewBinder + " has no no-argument constructor.");
                }

            } else {
                TypeSpec viewHolderFactory = generateViewHolderFactory();
                typeSpecs.add(viewHolderFactory);
            }

        } else {
            Optional<Element> viewDataBinding = findDataBinding(roundEnvironment.getRootElements());
            if (viewDataBinding.isPresent()) {
                TypeSpec viewHolderFactory = generateViewHolderFactoryWithDataBinding(viewDataBinding.get());
                TypeSpec viewBinder = generateViewBinder(viewDataBinding.get());

                typeSpecs.add(viewBinder);
                typeSpecs.add(viewHolderFactory);
            } else {
                if (roundEnvironment.processingOver()) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            "Can't find ViewDataBinding for " + model.getSimpleName(), model);
                } else {
                    // try again next round
                }

            }
        }

        return typeSpecs;
    }
}
