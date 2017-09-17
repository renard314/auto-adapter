package com.renard.auto_adapter.processor;

import com.google.common.collect.Lists;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Collections;
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
        JavaFileObject model = JavaFileObjects.forResource("twoDataBindingAvailable/Model.java");
        JavaFileObject binding1 = JavaFileObjects.forResource("twoDataBindingAvailable/DataBinding1.java");
        JavaFileObject binding2 = JavaFileObjects.forResource("twoDataBindingAvailable/DataBinding2.java");
        JavaFileObject viewDataBinding = JavaFileObjects.forResource("android/ViewDataBinding.java");
        List<JavaFileObject> javaFileObjects = Lists.newArrayList(model, binding1,
                binding2, viewDataBinding);
        Collections.addAll(javaFileObjects, libraryFiles);

        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Can't find ViewDataBinding for Model");
        assertThat(compilation).hadWarningContaining("There is more than one ViewDataBinding for Model");
    }

    @Test
    public void testDataBindingHasMoreThanOneVariable() {
        JavaFileObject model1 = JavaFileObjects.forResource("dataBindingHasTwoVariables/Model1.java");
        JavaFileObject model2 = JavaFileObjects.forResource("dataBindingHasTwoVariables/Model2.java");
        JavaFileObject binding = JavaFileObjects.forResource("dataBindingHasTwoVariables/DataBinding.java");
        JavaFileObject viewDataBinding = JavaFileObjects.forResource("android/ViewDataBinding.java");
        List<JavaFileObject> javaFileObjects = Lists.newArrayList(model1, model2, binding, viewDataBinding);
        Collections.addAll(javaFileObjects, libraryFiles);

        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Can't find ViewDataBinding for Model");
        assertThat(compilation).hadWarningContaining("ViewDataBinding for Model1 has more than one variable");
    }

    @Test
    public void testMixDatabBindingAndCustomBinding() {
        JavaFileObject model1 = JavaFileObjects.forResource("mixDataBinding/Model1.java");
        JavaFileObject model2 = JavaFileObjects.forResource("mixDataBinding/Model2.java");
        JavaFileObject model1Binding = JavaFileObjects.forResource("mixDataBinding/Model1DataBinding.java");
        JavaFileObject model2Binding = JavaFileObjects.forResource("mixDataBinding/Model2Binder.java");
        JavaFileObject viewDataBinding = JavaFileObjects.forResource("android/ViewDataBinding.java");

        List<JavaFileObject> javaFileObjects = Lists.newArrayList(model1, model2,
                model1Binding, model2Binding, viewDataBinding);
        Collections.addAll(javaFileObjects, libraryFiles);

        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("com.renard.auto_adapter.Adapter");

    }

    @Test
    public void testModelIsInnerClass() {
        JavaFileObject model = JavaFileObjects.forResource("modelIsInnerClass/Binder.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(model, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("com.renard.auto_adapter.Adapter");
    }

    @Test
    public void testGenerateTwoAdapters() {
        JavaFileObject model1 = JavaFileObjects.forResource("twoAdapters/Model1.java");
        JavaFileObject model2 = JavaFileObjects.forResource("twoAdapters/Model2.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(model1, model2, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("com.renard.auto_adapter.Adapter1");
        assertThat(compilation).generatedSourceFile("com.renard.auto_adapter.Adapter2");
    }

    @Test
    public void testGenerateWithWrongViewBinder() {
        JavaFileObject model1 = JavaFileObjects.forResource("wrongViewBinder/Model.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(model1, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Specified ViewBinder is not a class.");
    }

    @Test
    public void testModelIsBinder() {
        JavaFileObject modelAsBinder = JavaFileObjects.forResource("modelIsBinder/Model.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(modelAsBinder, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
        assertThat(compilation).succeeded();
    }

    @Test
    public void testCompileWithDataBinding() {
        JavaFileObject advertisement = JavaFileObjects.forResource("modelWithDataBinding/Model.java");
        JavaFileObject viewDataBinding = JavaFileObjects.forResource("android/ViewDataBinding.java");
        JavaFileObject advertisementItemBinding = JavaFileObjects.forResource("modelWithDataBinding/ModelDataBinding.java");
        List<JavaFileObject> javaFileObjects = Lists.newArrayList(advertisement, viewDataBinding,
                advertisementItemBinding);
        Collections.addAll(javaFileObjects, libraryFiles);

        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).succeeded();
    }

    @Test
    public void testCompileWithoutDataBinding() {
        JavaFileObject newsArticle = JavaFileObjects.forResource("modelWithDataBinding/Model.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(newsArticle, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Can't find ViewDataBinding for Model");
    }

    @Test
    public void testCompileOneModelWithViewBinding() {
        JavaFileObject newsArticle = JavaFileObjects.forResource("modelWithCustomBinding/Model.java");
        JavaFileObject binder = JavaFileObjects.forResource("modelWithCustomBinding/ModelBinder.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(newsArticle, binder, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).succeeded();
    }

    @Test
    public void testCompileTwoModelslWithViewBinding() {
        JavaFileObject newsArticle = JavaFileObjects.forResource("twoModelsWithCustomBinding/Model1.java");
        JavaFileObject binder = JavaFileObjects.forResource("twoModelsWithCustomBinding/Model1Binder.java");
        JavaFileObject advertisement = JavaFileObjects.forResource("twoModelsWithCustomBinding/Model2.java");
        JavaFileObject advertisementBinder = JavaFileObjects.forResource("twoModelsWithCustomBinding/Model2Binder.java");

        List<JavaFileObject> javaFileObjects = Lists.newArrayList(newsArticle, binder, advertisement,
                advertisementBinder);
        Collections.addAll(javaFileObjects, libraryFiles);

        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
        assertThat(compilation).succeeded();
    }

}
