package com.renard.auto_adapter.processor;

import com.google.testing.compile.Compilation;

import static com.google.testing.compile.CompilationSubject.assertThat;

class When {
    private final Compilation compilation;

    When(Compilation compilation) {
        this.compilation = compilation;
    }

    Then thenCompilationSucceeded() {
        assertThat(compilation).succeeded();
        return new Then(compilation);
    }

    Then thenCompilationFailed() {
        assertThat(compilation).failed();
        return new Then(compilation);
    }
}