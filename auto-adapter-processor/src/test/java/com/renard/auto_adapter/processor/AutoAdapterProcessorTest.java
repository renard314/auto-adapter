package com.renard.auto_adapter.processor;

import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.List;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class AutoAdapterProcessorTest {

    private JavaFileObject viewHolderFactory = JavaFileObjects.forResource("ViewHolderFactory.java");
    private JavaFileObject recyclerView = JavaFileObjects.forResource("android/RecyclerView.java");
    private JavaFileObject autoAdapter = JavaFileObjects.forResource("AutoAdapter.java");
    private JavaFileObject autoAdapterViewHolder = JavaFileObjects.forResource("AutoAdapterViewHolder.java");
    private JavaFileObject adapterItem = JavaFileObjects.forResource("AdapterItem.java");
    private JavaFileObject unique = JavaFileObjects.forResource("Unique.java");
    private JavaFileObject viewBinder = JavaFileObjects.forResource("ViewBinder.java");

    private JavaFileObject[] libraryFiles = {
            viewBinder, adapterItem, unique, autoAdapter, recyclerView, autoAdapterViewHolder, viewHolderFactory
    };


    @Test
    public void testMoreThanOneDataBindingAvailable() {
        givenJavaFileObjects(
                "twoDataBindingAvailable/Model.java",
                "twoDataBindingAvailable/DataBinding1.java",
                "twoDataBindingAvailable/DataBinding2.java",
                "android/ViewDataBinding.java")
                .whenCompiled()
                .thenCompilationFailed()
                .withErrors("Can't find ViewDataBinding for Model")
                .withWarning("There is more than one ViewDataBinding for Model");
    }

    @Test
    public void testDataBindingHasMoreThanOneVariable() {
        givenJavaFileObjects(
                "dataBindingHasTwoVariables/Model1.java",
                "dataBindingHasTwoVariables/Model2.java",
                "dataBindingHasTwoVariables/DataBinding.java",
                "android/ViewDataBinding.java")
                .whenCompiled()
                .thenCompilationFailed()
                .withErrors("Can't find ViewDataBinding for Model")
                .withWarning("ViewDataBinding for Model1 has more than one variable");
    }

    @Test
    public void testMixDatabBindingAndCustomBinding() {
        givenJavaFileObjects(
                "mixDataBinding/Model1.java",
                "mixDataBinding/Model2.java",
                "mixDataBinding/Model1DataBinding.java",
                "mixDataBinding/Model2Binder.java",
                "android/ViewDataBinding.java")
                .whenCompiled()
                .thenCompilationSucceeded()
                .withGeneratedSourceFiles("com.renard.auto_adapter.Adapter");
    }

    @Test
    public void testModelIsInnerClass() {
        givenJavaFileObjects("modelIsInnerClass/Binder.java")
                .whenCompiled()
                .thenCompilationSucceeded()
                .withGeneratedSourceFiles("com.renard.auto_adapter.Adapter");
    }

    @Test
    public void testGenerateTwoAdapters() {
        givenJavaFileObjects(
                "twoAdapters/Model1.java",
                "twoAdapters/Model2.java")
                .whenCompiled()
                .thenCompilationSucceeded()
                .withGeneratedSourceFiles(
                        "com.renard.auto_adapter.Adapter1",
                        "com.renard.auto_adapter.Adapter2");
    }

    @Test
    public void testGenerateWithWrongViewBinder() {
        givenJavaFileObjects("wrongViewBinder/Model.java")
                .whenCompiled()
                .thenCompilationFailed()
                .withErrors("Specified ViewBinder is not a class.");
    }

    @Test
    public void testModelIsBinder() {
        givenJavaFileObjects("modelIsBinder/Model.java")
                .whenCompiled()
                .thenCompilationSucceeded();
    }

    @Test
    public void testCompileWithDataBinding() {
        givenJavaFileObjects(
                "modelWithDataBinding/Model.java",
                "android/ViewDataBinding.java",
                "modelWithDataBinding/ModelDataBinding.java")
                .whenCompiled()
                .thenCompilationSucceeded();
    }

    @Test
    public void testCompileWithoutDataBinding() {
        givenJavaFileObjects("modelWithDataBinding/Model.java")
                .whenCompiled()
                .thenCompilationFailed()
                .withErrors("Can't find ViewDataBinding for Model");
    }

    @Test
    public void testCompileOneModelWithViewBinding() {
        givenJavaFileObjects(
                "modelWithCustomBinding/Model.java",
                "modelWithCustomBinding/ModelBinder.java")
                .whenCompiled()
                .thenCompilationSucceeded();
    }

    @Test
    public void testCompileTwoModelslWithViewBinding() {
        givenJavaFileObjects(
                "twoModelsWithCustomBinding/Model1.java",
                "twoModelsWithCustomBinding/Model1Binder.java",
                "twoModelsWithCustomBinding/Model2.java",
                "twoModelsWithCustomBinding/Model2Binder.java")
                .whenCompiled()
                .thenCompilationSucceeded();
    }


    @NonNull
    private TestSubject givenJavaFileObjects(String... sourceFiles) {
        List<JavaFileObject> javaFileObjects = Lists.newArrayList(libraryFiles);
        for (String sourceFile : sourceFiles) {
            javaFileObjects.add(JavaFileObjects.forResource(sourceFile));
        }
        return new TestSubject(javaFileObjects);
    }

    private static class TestSubject {

        private final List<JavaFileObject> javaFileObjects;
        private Compilation compilation;

        TestSubject(List<JavaFileObject> javaFileObjects) {

            this.javaFileObjects = javaFileObjects;
        }

        TestSubject whenCompiled() {
            compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
            return this;
        }

        TestSubject thenCompilationSucceeded() {
            assertThat(compilation).succeeded();
            return this;
        }

        TestSubject thenCompilationFailed() {
            assertThat(compilation).failed();
            return this;
        }

        TestSubject withErrors(String... errorStrings) {
            for (String error : errorStrings) {
                assertThat(compilation).hadErrorContaining(error);
            }
            return this;
        }

        TestSubject withGeneratedSourceFiles(String... sourceFile) {
            for (String file : sourceFile) {
                assertThat(compilation).generatedSourceFile(file);
            }
            return this;
        }

        TestSubject withWarning(String... warnings) {
            for (String warning : warnings) {
                assertThat(compilation).hadWarningContaining(warning);
            }
            return this;
        }
    }
}
