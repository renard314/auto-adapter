package com.renard.auto_adapter.processor;

import com.google.testing.compile.Compilation;

import static com.google.testing.compile.CompilationSubject.assertThat;

class Then {
    private final Compilation compilation;

    Then(Compilation compilation) {
        this.compilation = compilation;
    }

    Then withErrors(String... errorStrings) {
        for (String error : errorStrings) {
            assertThat(compilation).hadErrorContaining(error);
        }
        return this;
    }

    Then withGeneratedSourceFiles(String... sourceFile) {
        for (String file : sourceFile) {
            assertThat(compilation).generatedSourceFile(file);
        }
        return this;
    }

    Then withWarning(String... warnings) {
        for (String warning : warnings) {
            assertThat(compilation).hadWarningContaining(warning);
        }
        return this;
    }
}