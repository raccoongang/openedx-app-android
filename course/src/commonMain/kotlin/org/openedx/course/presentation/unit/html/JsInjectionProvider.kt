package org.openedx.course.presentation.unit.html

interface JsInjectionProvider {
    fun getCompletionsJs(): String?
    fun getSurveyCssJs(): String?
}
