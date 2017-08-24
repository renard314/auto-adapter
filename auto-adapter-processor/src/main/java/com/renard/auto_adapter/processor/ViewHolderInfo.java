package com.renard.auto_adapter.processor;

import static com.renard.auto_adapter.processor.AutoAdapterProcessor.LIBRARY_PACKAGE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.Messager;

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
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.renard.auto_adapter.processor.code_generation.ViewBinderGenerator;
import com.renard.auto_adapter.processor.code_generation.ViewHolderFactoryGenerator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

class ViewHolderInfo {

    private static final String VIEW_DATA_BINDING = "android.databinding.ViewDataBinding";
    private static final ClassName VIEW_HOLDER_CLASS_NAME = ClassName.get(LIBRARY_PACKAGE, "AutoAdapterViewHolder");

    private final ClassName viewBinderClassName;
    final ClassName viewHolderFactoryClassName;
    private final TypeElement model;
    private final Types typeUtils;
    private final Elements elementUtils;
    private final Messager messager;
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    ViewHolderInfo(final TypeElement model, final Types typeUtils, final Elements elementUtils,
            final Messager messager) {
        this.viewBinderClassName = getViewHolderClassName(model);
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.viewHolderFactoryClassName = getViewHolderFactoryClassName(model);
        this.model = model;
    }

    private ClassName getViewHolderClassName(final TypeElement model) {
        return ClassName.get(LIBRARY_PACKAGE, model.getSimpleName() + "ViewBinder");
    }

    private ClassName getViewHolderFactoryClassName(final TypeElement model) {
        return ClassName.get(LIBRARY_PACKAGE, model.getSimpleName() + "ViewBinderFactory");
    }

    Optional<Element> findDataBindingForModel(final Set<? extends Element> rootElements) {
        List<Element> bindingCandidates = new ArrayList<>();
        for (Element element : rootElements) {
            TypeMirror typeMirror = element.asType();
            List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(typeMirror);
            for (TypeMirror superType : supertypes) {
                String classNameOfSuperClass = getClassNameOf((DeclaredType) superType);
                if ((classNameOfSuperClass).equals(VIEW_DATA_BINDING)) {

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
            }
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

    TypeSpec generateViewHolder(final Element element) {
        ClassName className = getClassNameFrom(element);

        List<ExecutableElement> executableElements = ElementFilter.methodsIn(element.getEnclosedElements());
        ExecutableElement setVariableMethod = FluentIterable.from(executableElements)
                                                            .filter(ExecutableElementPredicates.isVoidMethod)
                                                            .filter(ExecutableElementPredicates.hasOneParameter)
                                                            .filter(ExecutableElementPredicates.startsWithSet)
                                                            .filter(ExecutableElementPredicates.parameterIsSameType(
                    model)).first().get();

        ViewBinderGenerator viewBinderGenerator = new ViewBinderGenerator(viewBinderClassName, className);
        return viewBinderGenerator.generate(model, setVariableMethod);
    }

    TypeSpec generateViewHolderFactory(final Element element) {
        ClassName className = getClassNameFrom(element);
        ViewHolderFactoryGenerator viewHolderFactoryGenerator = new ViewHolderFactoryGenerator(
                viewHolderFactoryClassName, className, viewBinderClassName, NEXT_ID.getAndIncrement(),
                VIEW_HOLDER_CLASS_NAME);
        return viewHolderFactoryGenerator.generate(model);
    }

    private ClassName getClassNameFrom(final Element element) {
        PackageElement dataBindingElementPackage = elementUtils.getPackageOf(element);
        String qualifiedName = dataBindingElementPackage.getQualifiedName().toString();
        return ClassName.get(qualifiedName, element.getSimpleName().toString());
    }
}
