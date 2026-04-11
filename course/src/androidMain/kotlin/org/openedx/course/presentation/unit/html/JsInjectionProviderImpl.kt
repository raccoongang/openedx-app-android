package org.openedx.course.presentation.unit.html

import android.content.Context
import org.openedx.foundation.extension.readAsText

class JsInjectionProviderImpl(private val context: Context) : JsInjectionProvider {
    override fun getCompletionsJs(): String? =
        runCatching { context.assets.readAsText("js_injection/completions.js") }.getOrNull()

    override fun getSurveyCssJs(): String? =
        runCatching { context.assets.readAsText("js_injection/survey_css.js") }.getOrNull()
}
