package com.renard.auto_adapter.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import java.util.Collections;
import java.util.List;

import javax.tools.JavaFileObject;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

public class AutoAdapterProcessorTest {
    private JavaFileObject viewHolderFactory = JavaFileObjects.forResource("ViewHolderFactory.java");
    private JavaFileObject recyclerView = JavaFileObjects.forResource("RecyclerView.java");
    private JavaFileObject autoAdapter = JavaFileObjects.forResource("AutoAdapter.java");
    private JavaFileObject autoAdapterViewHolder = JavaFileObjects.forResource("AutoAdapterViewHolder.java");
    private JavaFileObject adapterItem = JavaFileObjects.forResource("AdapterItem.java");
    private JavaFileObject unique = JavaFileObjects.forResource("Unique.java");
    private JavaFileObject viewBinder = JavaFileObjects.forResource("ViewBinder.java");
    private JavaFileObject[] libraryFiles = {
        viewBinder, adapterItem, unique, autoAdapter, recyclerView, autoAdapterViewHolder, viewHolderFactory
    };

    @Test
    public void testCompileWithoutViewBinding() {
        JavaFileObject newsArticle = JavaFileObjects.forResource("NewsArticle.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(newsArticle, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Can't find ViewDataBinding for NewsArticle");
    }

    @Test
    public void testCompileOneModelWithViewBinding() {
        JavaFileObject newsArticle = JavaFileObjects.forResource("NewsArticleWithBinding.java");
        JavaFileObject binder = JavaFileObjects.forResource("NewsArticleBinder.java");
        List<JavaFileObject> javaFileObjects = Lists.asList(newsArticle, binder, libraryFiles);
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);

        assertThat(compilation).succeeded();
    }

    @Test
    public void testCompileTwoModelslWithViewBinding() {
        JavaFileObject newsArticle = JavaFileObjects.forResource("NewsArticleWithBinding.java");
        JavaFileObject binder = JavaFileObjects.forResource("NewsArticleBinder.java");
        JavaFileObject advertisement = JavaFileObjects.forResource("AdvertisementWithBinding.java");
        JavaFileObject advertisementBinder = JavaFileObjects.forResource("AdvertisementBinder.java");

        List<JavaFileObject> javaFileObjects = Lists.newArrayList(newsArticle, binder, advertisement, advertisementBinder);
        Collections.addAll(javaFileObjects, libraryFiles);

        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
        assertThat(compilation).succeeded();
    }

}
