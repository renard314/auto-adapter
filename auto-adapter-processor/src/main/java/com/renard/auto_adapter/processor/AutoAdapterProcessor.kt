package com.renard.auto_adapter.processor


import com.google.common.base.Optional
import com.renard.auto_adapter.processor.AutoAdapterProcessor.Companion.ADAPTER_ITEM_ANNOTATION_NAME
import com.renard.auto_adapter.processor.AutoAdapterProcessor.Companion.ON_CLICK_ANNOTATION_NAME
import com.renard.auto_adapter.processor.code_generation.AdapterGenerator
import com.renard.auto_adapter.processor.code_generation.AndroidClassNames
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.collections.LinkedHashSet

fun VariableElement.isAnnotatedModel(model: TypeElement) =
        Util.typeToString(asType()) == Util.typeToString(model.asType())

fun VariableElement.isAndroidView() =
        Util.typeToString(asType()) == AndroidClassNames.VIEW.toString()

fun VariableElement.isIn(models: Iterable<TypeElement>): Boolean {
    return models.count {
        Util.typeToString(asType()) == Util.typeToString(it.asType())
    } > 0
}

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(ON_CLICK_ANNOTATION_NAME, ADAPTER_ITEM_ANNOTATION_NAME)
class AutoAdapterProcessor : AbstractProcessor() {

    private lateinit var typeUtils: Types
    private lateinit var elementUtils: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private val annotatedModels = LinkedHashSet<AnnotatedModel>()

    @Synchronized override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
        typeUtils = processingEnvironment.typeUtils
        elementUtils = processingEnvironment.elementUtils
    }

    override fun process(annotations: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

        val typeSpecs = ArrayList<TypeSpec>()
        var hasProcessedAnnotation = false

        if (!annotations.isEmpty()) {
            val adapters = generateAdapters(roundEnvironment)
            typeSpecs.addAll(adapters)
            hasProcessedAnnotation = !adapters.isEmpty()
        }

        val iterator = annotatedModels.iterator()
        while (iterator.hasNext()) {
            val annotatedModel = iterator.next()
            val spec = annotatedModel.generateTypeSpecs(roundEnvironment)
            if (!spec.isEmpty()) {
                iterator.remove()
                typeSpecs.addAll(spec)
            }

        }

        saveGeneratedTypes(typeSpecs)

        return hasProcessedAnnotation
    }


    private fun generateAdapters(roundEnvironment: RoundEnvironment): List<TypeSpec> {

        val adapterNamesToModels = findAllClassesAnnotatedWithAdapterItem(roundEnvironment)
        return if (adapterNamesToModels.isEmpty()) {
            emptyList()
        } else {
            val onClickElement = elementUtils.getTypeElement(ON_CLICK_ANNOTATION_NAME)
            val methodsWithOnClick: MutableSet<out Element> = roundEnvironment.getElementsAnnotatedWith(onClickElement)
            generateAdapters(adapterNamesToModels, methodsWithOnClick)
        }

    }




    private fun generateAdapters(adaptersWithModels: Map<String, List<TypeElement>>, methodsWithOnClick: Set<Element>): List<TypeSpec> {

        val copyMethodsWithOnClick: MutableSet<Element> = LinkedHashSet(methodsWithOnClick)

        val typeSpecs = ArrayList<TypeSpec>()
        for ((adapterNames, models) in adaptersWithModels) {
            val modelToFactory = HashMap<TypeElement, ClassName>()
            for (model in models) {

                val annotation = model.annotationMirrors.first { isAdapterItemAnnotation(it); }
                val values = annotation.elementValues.filterKeys { it.simpleName.toString() == "viewBinder" }.values

                val value = when {
                    values.isEmpty() -> Optional.absent()
                    else -> {
                        val firstValue = values.iterator().next()
                        if (firstValue.value == "<error>") {
                            messager.printMessage(Diagnostic.Kind.ERROR, "Specified ViewBinder is not a class.", model, annotation)
                            return emptyList()
                        }

                        Optional.of(firstValue)
                    }
                }

                val viewIds: Set<Int> = getObservedViewIds(copyMethodsWithOnClick, model)
                val annotatedModel = AnnotatedModel(model, typeUtils, elementUtils, messager, value, viewIds)
                annotatedModels.add(annotatedModel)
                modelToFactory.put(model, annotatedModel.viewHolderFactoryClassName)
            }

            // enclosing type -> List<{annotatedmethod}>
            val listenerToMethodAndIds: Map<Element, List<ExecutableElement>> = methodsWithOnClick.filter {
                val method = it as ExecutableElement


                val hasModel = method.parameters.count { it.isIn(models) } == 1
                val hasView = method.parameters.count { it.isAndroidView() } == 1
                method.parameters.isEmpty()
                        || (hasView && method.parameters.size == 1)
                        || (hasModel && method.parameters.size==1)
                        || (hasView && hasModel && method.parameters.size==2)
            }.groupBy(keySelector = { it.enclosingElement }, valueTransform = { it as ExecutableElement })

            val adapterGenerator = AdapterGenerator(adapterNames, modelToFactory, listenerToMethodAndIds)
            val adapter = adapterGenerator.generate()
            typeSpecs.add(adapter)

        }

        return typeSpecs
    }

    private fun getObservedViewIds(methodsWithOnClick: MutableSet<Element>, model: TypeElement): Set<Int> {
        //all click listeners for this model
        val methodsForThis: List<Element> = methodsWithOnClick.filter {
            val method = it as ExecutableElement
            when {
                method.parameters.size == 2 -> {
                    val viewParamsCount = method.parameters.count { it.isAndroidView() }
                    val modelParamsCount = method.parameters.count { it.isAnnotatedModel(model) }
                    viewParamsCount == 1 && modelParamsCount == 1
                }
                method.parameters.size == 1 -> {
                    method.parameters.first().isAndroidView() || method.parameters.first().isAnnotatedModel(model)
                }
                else -> {
                    method.parameters.isEmpty()
                }
            }
        }

        //get all viewIds for this model
        return methodsForThis.flatMap { method ->
            val clickAnnotations = method.annotationMirrors
                    .filter { Util.typeToString(it.annotationType) == ON_CLICK_ANNOTATION_NAME }

            val valueListener = IntAnnotationValueVisitor()
            clickAnnotations.forEach {
                it.elementValues.filterKeys { "value" == it.simpleName.toString() }.values.first().accept(valueListener, null)
            }
            valueListener.ids
        }.toSet()
    }

    private fun isAdapterItemAnnotation(it: AnnotationMirror) =
            Util.typeToString(it.annotationType) == ADAPTER_ITEM_ANNOTATION_NAME

    private fun saveGeneratedTypes(typeSpecs: List<TypeSpec>) {
        for (spec in typeSpecs) {
            val builder = JavaFile.builder(LIBRARY_PACKAGE, spec)
            val file = builder.build()
            try {
                file.writeTo(filer)
            } catch (e: IOException) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.message)
                e.printStackTrace()
            }

        }
    }

    private fun findAllClassesAnnotatedWithAdapterItem(
            roundEnvironment: RoundEnvironment): Map<String, List<TypeElement>> {
        val result = HashMap<String, MutableList<TypeElement>>()

        val adapterItemElement = elementUtils.getTypeElement(ADAPTER_ITEM_ANNOTATION_NAME)
        for (element in roundEnvironment.getElementsAnnotatedWith(adapterItemElement)) {

            if (element.kind != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.WARNING, "AdapterItem can only be applied to a class.")
                continue
            }

            val annotation = (element as TypeElement).annotationMirrors.first { isAdapterItemAnnotation(it); }
            val annotationValue = annotation.elementValues.filterKeys { "value" == it.simpleName.toString() }.values.first()
            val value = annotationValue.value
            var strings: MutableList<TypeElement>? = result[value.toString()]
            if (strings == null) {
                strings = ArrayList()
                result.put(value.toString(), strings)
            }

            strings.add(element)
        }

        return result
    }

    companion object {
        const val LIBRARY_PACKAGE = "com.renard.auto_adapter"
        const val ADAPTER_ITEM_ANNOTATION_NAME = "com.renard.auto_adapter.AdapterItem"
        const val ON_CLICK_ANNOTATION_NAME = "com.renard.auto_adapter.OnClick"
    }
}
