package com.renard.auto_adapter.processor

import java.util.*
import javax.lang.model.element.AnnotationValue
import javax.lang.model.util.SimpleAnnotationValueVisitor7


class IntAnnotationValueVisitor : SimpleAnnotationValueVisitor7<Void, Void>() {

    internal val ids: MutableList<Int> = ArrayList()

    override fun visitInt(value: Int, v: Void?): Void? {
        ids.add(value)
        return null
    }

    override fun visitArray(list: List<AnnotationValue>, v: Void?): Void? {
        ids.addAll(list.map { it.value as Int })
        return null
    }

}
