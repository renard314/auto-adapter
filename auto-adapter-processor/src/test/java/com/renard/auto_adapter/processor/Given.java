package com.renard.auto_adapter.processor;

import android.support.annotation.NonNull;

import com.google.common.collect.Lists;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.Compiler.javac;

class Given {

    private JavaFileObject viewHolderFactory = JavaFileObjects.forResource("ViewHolderFactory.java");
    private JavaFileObject recyclerView = JavaFileObjects.forResource("android/RecyclerView.java");
    private JavaFileObject autoAdapter = JavaFileObjects.forResource("AutoAdapter.java");
    private JavaFileObject autoAdapterViewHolder = JavaFileObjects.forResource("AutoAdapterViewHolder.java");
    private JavaFileObject adapterItem = JavaFileObjects.forResource("AdapterItem.java");
    private JavaFileObject unique = JavaFileObjects.forResource("Unique.java");
    private JavaFileObject viewBinder = JavaFileObjects.forResource("ViewBinder.java");
    private JavaFileObject onClick = JavaFileObjects.forResource("OnClick.java");

    private JavaFileObject[] libraryFiles = {
            viewBinder, adapterItem, unique, autoAdapter, recyclerView, autoAdapterViewHolder, viewHolderFactory, onClick
    };

    private final List<JavaFileObject> javaFileObjects = Lists.newArrayList(libraryFiles);

    private Given(List<JavaFileObject> files) {
        this.javaFileObjects.addAll(files);
    }

    @NonNull
    static Given givenJavaFileObjects(String... sourceFiles) {
        List<JavaFileObject> javaFileObjects = new ArrayList<>();
        for (String sourceFile : sourceFiles) {
            javaFileObjects.add(JavaFileObjects.forResource(sourceFile));
        }
        return new Given(javaFileObjects);
    }

    When whenCompiled() {
        Compilation compilation = javac().withProcessors(new AutoAdapterProcessor()).compile(javaFileObjects);
        return new When(compilation);
    }

}
