package com.renard.auto_adapter.processor;

import com.google.common.base.Predicate;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

final class ExecutableElementPredicates {

    static final Predicate<ExecutableElement> isVoidMethod = new Predicate<ExecutableElement>() {
        @Override
        public boolean apply(ExecutableElement input) {
            return input.getReturnType().getKind() == TypeKind.VOID;
        }

        @Override
        public boolean test(ExecutableElement input) {
            return apply(input);
        }
    };

    static final Predicate<ExecutableElement> hasOneParameter = new Predicate<ExecutableElement>() {
        @Override
        public boolean apply(ExecutableElement input) {
            return input.getParameters().size() == 1;
        }

        @Override
        public boolean test(ExecutableElement input) {
            return apply(input);
        }
    };

    static final Predicate<ExecutableElement> startsWithSet = new Predicate<ExecutableElement>() {
        @Override
        public boolean apply(ExecutableElement input) {
            return input.getSimpleName().toString().startsWith("set");
        }

        @Override
        public boolean test(ExecutableElement input) {
            return apply(input);
        }
    };

    static Predicate<? super ExecutableElement> parameterIsSameType(final TypeElement model) {
        return new Predicate<ExecutableElement>() {
            @Override
            public boolean apply(ExecutableElement input) {
                VariableElement variableElement = input.getParameters().get(0);

                String modelAsString = Util.typeToString(model.asType());
                String paramAsString = Util.typeToString(variableElement.asType());
                return modelAsString.equals(paramAsString);
            }

            @Override
            public boolean test(ExecutableElement input) {
                return apply(input);
            }
        };
    }
}
