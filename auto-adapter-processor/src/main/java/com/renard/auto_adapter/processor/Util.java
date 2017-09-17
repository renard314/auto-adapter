/*
 * Copyright (C) 2013 Google, Inc.
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.renard.auto_adapter.processor;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Utilities for handling types in annotation processors
 */
final class Util {

    private Util() {
    }

    private static PackageElement getPackage(Element type) {
        while (type.getKind() != ElementKind.PACKAGE) {
            type = type.getEnclosingElement();
        }
        return (PackageElement) type;
    }

    /**
     * Returns a string for {@code type}. Primitive types are always boxed.
     */
    static String typeToString(TypeMirror type) {
        StringBuilder result = new StringBuilder();
        typeToString(type, result, '.');
        return result.toString();
    }


    /**
     * Appends a string for {@code type} to {@code result}. Primitive types are
     * always boxed.
     *
     * @param innerClassSeparator either '.' or '$', which will appear in a
     *                            class name like "java.lang.Map.Entry" or "java.lang.Map$Entry".
     *                            Use '.' for references to existing types in code. Use '$' to define new
     *                            class names and for strings that will be used by runtime reflection.
     */
    private static void typeToString(final TypeMirror type, final StringBuilder result,
                                     final char innerClassSeparator) {
        type.accept(new SimpleTypeVisitor6<Void, Void>() {
            @Override
            public Void visitDeclared(DeclaredType declaredType, Void v) {
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                rawTypeToString(result, typeElement, innerClassSeparator);
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    result.append("<");
                    for (int i = 0; i < typeArguments.size(); i++) {
                        if (i != 0) {
                            result.append(", ");
                        }
                        typeToString(typeArguments.get(i), result, innerClassSeparator);
                    }
                    result.append(">");
                }
                return null;
            }

            @Override
            public Void visitTypeVariable(TypeVariable typeVariable, Void v) {
                result.append(typeVariable.asElement().getSimpleName());
                return null;
            }

            @Override
            public Void visitError(ErrorType errorType, Void v) {
                // Error type found, a type may not yet have been generated, but we need the type
                // so we can generate the correct code in anticipation of the type being available
                // to the compiler.

                // Paramterized types which don't exist are returned as an error type whose name is "<any>"
                if ("<any>".equals(errorType.toString())) {
                    throw new CodeGenerationIncompleteException(
                            "Type reported as <any> is likely a not-yet generated parameterized type.");
                }
                // TODO(cgruber): Figure out a strategy for non-FQCN cases.
                result.append(errorType.toString());
                return null;
            }

            @Override
            protected Void defaultAction(TypeMirror typeMirror, Void v) {
                throw new UnsupportedOperationException(
                        "Unexpected TypeKind " + typeMirror.getKind() + " for " + typeMirror);
            }
        }, null);
    }


    private static void rawTypeToString(StringBuilder result, TypeElement type,
                                        char innerClassSeparator) {
        String packageName = getPackage(type).getQualifiedName().toString();
        String qualifiedName = type.getQualifiedName().toString();
        if (packageName.isEmpty()) {
            result.append(qualifiedName.replace('.', innerClassSeparator));
        } else {
            result.append(packageName);
            result.append('.');
            result.append(
                    qualifiedName.substring(packageName.length() + 1).replace('.', innerClassSeparator));
        }
    }


    /**
     * Returns the no-args constructor for {@code type}, or null if no such
     * constructor exists.
     */
    static ExecutableElement getNoArgsConstructor(Element type) {
        for (Element enclosed : type.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            ExecutableElement constructor = (ExecutableElement) enclosed;
            if (constructor.getParameters().isEmpty()) {
                return constructor;
            }
        }
        return null;
    }


    /**
     * An exception thrown when a type is not extant (returns as an error type),
     * usually as a result of another processor not having yet generated its types upon
     * which a dagger-annotated type depends.
     */
    private final static class CodeGenerationIncompleteException extends IllegalStateException {
        CodeGenerationIncompleteException(String s) {
            super(s);
        }
    }
}