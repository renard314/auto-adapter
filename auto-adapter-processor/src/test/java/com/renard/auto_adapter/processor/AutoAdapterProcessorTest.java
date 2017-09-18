package com.renard.auto_adapter.processor;

import org.junit.Test;

import static com.renard.auto_adapter.processor.Given.*;

public class AutoAdapterProcessorTest {

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


}
