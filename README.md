# auto-adapter
Create a type safe RecyclerView.Adapter for multiple models using Java Annotations. This is a toy project for me to learn about Annotation Processing

## debugging the processor
1. ./gradlew --no-daemon -Dorg.gradle.debug=true :sample:clean :sample:compileDebugJavaWithJavac
2. create remote run config in Android Studio and run debug
