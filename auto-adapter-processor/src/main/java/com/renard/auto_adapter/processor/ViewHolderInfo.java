package com.renard.auto_adapter.processor;

import static com.renard.auto_adapter.processor.AutoAdapterProcessor.LIBRARY_PACKAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.Messager;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import javax.tools.Diagnostic;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.renard.auto_adapter.processor.code_generation.ViewBinderGenerator;
import com.renard.auto_adapter.processor.code_generation.ViewHolderFactoryGenerator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

class ViewHolderInfo {
    private static final String VIEW_DATA_BINDING = "android.databinding.ViewDataBinding";
    private final Predicate<TypeMirror> isViewHolder = new Predicate<TypeMirror>() {
        @Override
        public boolean apply(final TypeMirror input) {
            return getClassNameOf((DeclaredType) input).equals(VIEW_DATA_BINDING);
        }

        @Override
        public boolean test(final TypeMirror input) {
            return apply(input);
        }
    };

    private static final ClassName VIEW_HOLDER_CLASS_NAME = ClassName.get(LIBRARY_PACKAGE, "AutoAdapterViewHolder");
    private final ClassName viewBinderClassName;
    final ClassName viewHolderFactoryClassName;
    private final TypeElement model;
    private final Optional<AnnotationValue> annotationValue;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final Messager messager;
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    ViewHolderInfo(final TypeElement model, final Types typeUtils, final Elements elementUtils, final Messager messager,
            final Optional<AnnotationValue> annotationValue) {

        this.annotationValue = annotationValue;
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.viewHolderFactoryClassName = getViewHolderFactoryClassName(model);
        this.model = model;
        this.viewBinderClassName = getViewBinderClassName(model);
    }

    private ClassName getViewBinderClassName(final TypeElement model) {
        if (annotationValue.isPresent()) {
            TypeMirror value = (TypeMirror) annotationValue.get().getValue();
            Element element = typeUtils.asElement(value);
            return getClassNameFrom(element);
        } else {
            return ClassName.get(LIBRARY_PACKAGE, model.getSimpleName() + "ViewBinder");
        }
    }

    private ClassName getViewHolderFactoryClassName(final TypeElement model) {
        return ClassName.get(LIBRARY_PACKAGE, model.getSimpleName() + "ViewHolderFactory");
    }

    Optional<Element> findDataBindingForModel(final Set<? extends Element> rootElements) {
        List<Element> bindingCandidates = new ArrayList<>();
        for (Element element : rootElements) {
            TypeMirror typeMirror = element.asType();
            List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(typeMirror);
            if (!Collections2.filter(supertypes, isViewHolder).isEmpty()) {

                List<ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());
                ImmutableList<ExecutableElement> setVariableMethod = FluentIterable.from(executableElements)
                                                                                   .filter(
                                                                                       ExecutableElementPredicates.isVoidMethod)
                                                                                   .filter(
                                                                                       ExecutableElementPredicates.hasOneParameter)
                                                                                   .filter(
                        ExecutableElementPredicates.startsWithSet).toList();

                boolean hasSetMethodForModel = Collections2.filter(setVariableMethod,
                        ExecutableElementPredicates.parameterIsSameType(model)).size() == 1;

                if (hasSetMethodForModel && setVariableMethod.size() > 1) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                        "ViewDataBinding for " + model.getSimpleName() + " has more than one variable.", model);
                    return Optional.absent();
                } else if (hasSetMethodForModel) {
                    bindingCandidates.add(element);
                }
            }
            /*for (TypeMirror superType : supertypes) {
             *  String classNameOfSuperClass = getClassNameOf((DeclaredType) superType);
             *  if ((classNameOfSuperClass).equals(VIEW_DATA_BINDING)) {
             *
             *      List<ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());
             *      ImmutableList<ExecutableElement> setVariableMethod = FluentIterable.from(executableElements)
             *              .filter(ExecutableElementPredicates.isVoidMethod)
             *              .filter(ExecutableElementPredicates.hasOneParameter)
             *              .filter(ExecutableElementPredicates.startsWithSet)
             *              .toList();
             *
             *      boolean hasSetMethodForModel = Collections2.filter(setVariableMethod,
             *              ExecutableElementPredicates.parameterIsSameType(model)).size() == 1;
             *
             *      if (hasSetMethodForModel && setVariableMethod.size() > 1) {
             *          messager.printMessage(Diagnostic.Kind.ERROR,
             *                  "ViewDataBinding for " + model.getSimpleName() + " has more than one variable.", model);
             *          return Optional.absent();
             *      } else if (hasSetMethodForModel) {
             *          bindingCandidates.add(element);
             *      }
             *
             *  }
             *}*/
        }

        if (bindingCandidates.size() == 1) {
            return Optional.of(bindingCandidates.get(0));
        } else if (bindingCandidates.size() > 1) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                "There is more than one ViewDataBinding for " + model.getSimpleName(), model);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Can't find ViewDataBinding for " + model.getSimpleName(),
                model);
        }

        return Optional.absent();
    }

    private String getClassNameOf(final DeclaredType declaredType) {
        Element element = declaredType.asElement();
        PackageElement packageOf = elementUtils.getPackageOf(declaredType.asElement());
        String simpleName = element.getSimpleName().toString();
        return packageOf + "." + simpleName;
    }

    TypeSpec generateViewBinder(final Element element) {
        ClassName className = getClassNameFrom(element);

        List<ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());
        ExecutableElement setVariableMethod = FluentIterable.from(executableElements)
                                                            .filter(ExecutableElementPredicates.isVoidMethod)
                                                            .filter(ExecutableElementPredicates.hasOneParameter)
                                                            .filter(ExecutableElementPredicates.startsWithSet)
                                                            .filter(ExecutableElementPredicates.parameterIsSameType(
                    model)).first().get();

        ViewBinderGenerator viewBinderGenerator = new ViewBinderGenerator();
        return viewBinderGenerator.generate(model, setVariableMethod, viewBinderClassName, className);
    }

    TypeSpec generateViewHolderFactory(final Element dataBinding) {
        ClassName dataBindingClassName = getClassNameFrom(dataBinding);
        ViewHolderFactoryGenerator viewHolderFactoryGenerator = new ViewHolderFactoryGenerator(
                viewHolderFactoryClassName, viewBinderClassName, NEXT_ID.getAndIncrement());
        return viewHolderFactoryGenerator.generate(model, dataBindingClassName, VIEW_HOLDER_CLASS_NAME);
    }

    TypeSpec generateViewHolderFactory() {
        ViewHolderFactoryGenerator viewHolderFactoryGenerator = new ViewHolderFactoryGenerator(
                viewHolderFactoryClassName, viewBinderClassName, NEXT_ID.getAndIncrement());
        return viewHolderFactoryGenerator.generate(model, VIEW_HOLDER_CLASS_NAME);
    }

    private ClassName getClassNameFrom(final Element element) {
        PackageElement dataBindingElementPackage = elementUtils.getPackageOf(element);
        String qualifiedName = dataBindingElementPackage.getQualifiedName().toString();
        return ClassName.get(qualifiedName, element.getSimpleName().toString());
    }

    boolean hasCustomViewBinder() {
        return annotationValue.isPresent();
    }
}
