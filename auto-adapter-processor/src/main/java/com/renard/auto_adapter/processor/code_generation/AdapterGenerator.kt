package com.renard.auto_adapter.processor.code_generation

import com.renard.auto_adapter.processor.*
import com.squareup.javapoet.*
import javax.lang.model.element.*

class AdapterGenerator(private val adapterName: String, private val modelToFactory: Map<TypeElement, ClassName>, private val listenersWithMethods: Map<Element, List<ExecutableElement>>) {

    fun generate(): TypeSpec {
        val adapterBuilder = createClassBuilder()

        addConstructor(adapterBuilder)
        addMethodsForEachModel(adapterBuilder)
        addOnCLickItemMethod(adapterBuilder)
        addRegisterListenerMethods(adapterBuilder)

        return adapterBuilder.build()
    }

    private fun addRegisterListenerMethods(adapterBuilder: TypeSpec.Builder) {
        listenersWithMethods.keys.forEach {
            val registerMethod = MethodSpec.methodBuilder("registerForEvents")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(it.asType()), "listener")
                    .addCode("super.registerForEvents(listener);\n")
                    .build()
            adapterBuilder.addMethod(registerMethod)
        }
    }

    private fun addOnCLickItemMethod(adapterBuilder: TypeSpec.Builder) {
        val code = CodeBlock.builder()
        listenersWithMethods.forEach { listener, methods ->

            code.beginControlFlow("if(listener instanceof \$T)", listener)
                    .beginControlFlow("switch(view.getId())")


            val idToMethod: MutableMap<Int, MutableList<ExecutableElement>> = HashMap()

            //collect all ids for each method
            methods.forEach { method ->
                val valueListener = IntAnnotationValueVisitor()
                method.annotationMirrors
                        .filter { Util.typeToString(it.annotationType) == AutoAdapterProcessor.ON_CLICK_ANNOTATION_NAME }
                        .forEach {
                            it.elementValues
                                    .filterKeys { "value" == it.simpleName.toString() }
                                    .values
                                    .first()
                                    .accept(valueListener, null)
                        }
                //create a map of id to list<method>
                valueListener.ids.groupByTo(idToMethod, { it }, { method })
            }

            idToMethod.forEach { viewId, methodsById ->
                code.beginControlFlow("case \$L : ", viewId)
                methodsById.forEach { methodById ->

                    var codeString = "((\$T) listener).\$L("
                    val args = mutableListOf<Any>(listener, methodById.simpleName)

                    methodById.parameters.forEachIndexed { index, variableElement ->


                        if (variableElement.isAndroidView()) {
                            codeString += "view, "
                        } else if (variableElement.isIn(modelToFactory.keys)) {
                            codeString += "(\$T) item, "
                            modelToFactory.keys.find { Util.typeToString(it.asType()) == Util.typeToString(methodById.parameters[index].asType()) }?.let {
                                args.add(it.asType())
                            }
                        }
                    }

                    val let: VariableElement? = methodById.parameters.find { it.isIn(modelToFactory.keys) }
                    let?.let {
                        code.beginControlFlow("if(item instanceof \$T)", it.asType())
                    }

                    code.add(codeString.removeSuffix(", ") + ");\n", *args.toTypedArray())

                    let?.let {
                        code.endControlFlow()
                    }


                }
                code.add("break;\n")
                code.endControlFlow()
            }
            code.endControlFlow()
            code.endControlFlow()
        }


        val onClickMethod = MethodSpec.methodBuilder("onClickItem")
                .addModifiers(Modifier.PROTECTED)
                .addParameter(AndroidClassNames.VIEW, "view")
                .addParameter(ClassName.get(Any::class.java), "item")
                .addParameter(ClassName.get(Any::class.java), "listener")
                .addCode(code.build())
                .build()
        adapterBuilder.addMethod(onClickMethod)

    }

    private fun createClassBuilder(): TypeSpec.Builder {
        return TypeSpec.classBuilder(adapterName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(AUTO_ADAPTER_CLASSNAME)
    }


    private fun addMethodsForEachModel(adapterBuilder: TypeSpec.Builder) {
        for (model in modelToFactory.keys) {
            val addMethod = MethodSpec.methodBuilder("add" + model.simpleName)
                    .addParameter(TypeName.get(model.asType()), "item")
                    .addCode("addItem(item);\n").addModifiers(Modifier.PUBLIC).build()

            val removeMethod = MethodSpec.methodBuilder("remove" + model.simpleName)
                    .addParameter(TypeName.get(model.asType()), "item")
                    .addCode("removeItem(item);\n").addModifiers(Modifier.PUBLIC).build()



            adapterBuilder.addMethod(addMethod)
            adapterBuilder.addMethod(removeMethod)
        }
    }

    private fun addConstructor(adapterBuilder: TypeSpec.Builder) {
        val builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)

        for (model in modelToFactory.keys) {
            builder.addCode("putMapping($1T.class, new $2T());\n", model, modelToFactory[model])
        }

        adapterBuilder.addMethod(builder.build())
    }

    companion object {
        private val AUTO_ADAPTER_CLASSNAME = ClassName.get(AutoAdapterProcessor.LIBRARY_PACKAGE, "AutoAdapter")
    }

}
