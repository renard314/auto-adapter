package com.renard.auto_adapter.processor;

import com.google.common.base.Predicate;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.renard.auto_adapter.processor.AutoAdapterProcessor.ADAPTER_ITEM_ANNOTATION_NAME;

final class Predicates {
    static final Predicate<ExecutableElement> IS_VOID_METHOD = new MyPredicate<ExecutableElement>() {

        @Override
        public boolean test(final ExecutableElement input) {
            return input.getReturnType().getKind() == TypeKind.VOID;
        }
    };
    static final Predicate<ExecutableElement> HAS_ONE_PARAMETER = new MyPredicate<ExecutableElement>() {
        @Override
        public boolean test(final ExecutableElement input) {
            return input.getParameters().size() == 1;
        }

    };
    static final Predicate<ExecutableElement> STARTS_WITH_SET = new MyPredicate<ExecutableElement>() {
        @Override
        public boolean test(final ExecutableElement input) {
            return input.getSimpleName().toString().startsWith("set");
        }
    };
    static final Predicate<ExecutableElement> IS_ANNOTATION_VALUE = new MyPredicate<ExecutableElement>() {
        @Override
        public boolean test(final ExecutableElement input) {
            return "value".equals(input.getSimpleName().toString());
        }
    };
    static final Predicate<ExecutableElement> IS_VIEW_BINDER = new MyPredicate<ExecutableElement>() {
        @Override
        public boolean test(final ExecutableElement input) {
            return "viewBinder".equals(input.getSimpleName().toString());
        }
    };
    static final Predicate<AnnotationMirror> IS_ADAPTER_ITEM = new MyPredicate<AnnotationMirror>() {
        @Override
        public boolean test(final AnnotationMirror input) {
            return Util.typeToString(input.getAnnotationType()).equals(ADAPTER_ITEM_ANNOTATION_NAME);
        }

    };
    private static final String VIEW_DATA_BINDING = "android.databinding.ViewDataBinding";
    static final Predicate<TypeMirror> IS_VIEW_DATA_BINDING = new MyPredicate<TypeMirror>() {

        @Override
        public boolean test(final TypeMirror input) {
            return Util.typeToString(input).equals(VIEW_DATA_BINDING);
        }

    };

    static Predicate<? super ExecutableElement> parameterIsSameType(final TypeElement model) {
        return new MyPredicate<ExecutableElement>() {
            @Override
            public boolean test(final ExecutableElement input) {
                VariableElement variableElement = input.getParameters().get(0);

                String modelAsString = Util.typeToString(model.asType());
                String paramAsString = Util.typeToString(variableElement.asType());
                return modelAsString.equals(paramAsString);
            }
        };
    }

    private static abstract class MyPredicate<T> implements Predicate<T> {

        @Override
        public boolean apply(T input) {
            return test(input);
        }
    }
}
