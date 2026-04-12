package org.openedx.course.presentation.unit.html

class JsInjectionProviderImpl : JsInjectionProvider {
    override fun getCompletionsJs(): String = COMPLETIONS_JS

    override fun getSurveyCssJs(): String = SURVEY_CSS_JS
}

private const val COMPLETIONS_JS = """
//Injection to intercept completion state for xBlocks
${'$'}(document).on("ajaxSuccess", function(event, request, settings) {
    console.log("loaded url is = " + settings.url);
    if (settings.url.includes("publish_completion") &&
        request.responseText.includes("ok")) {
        javascript:window.callback.completionSet();
    }
});
"""

private const val SURVEY_CSS_JS = """
//Injection to fix CSS issues for Survey xBlock
var css = `
    .survey-table:not(.poll-results) .survey-option label {
        margin-bottom: 0px !important;
    }

    .survey-table:not(.poll-results) .survey-option .visible-mobile-only {
        width: calc(100% - 21px) !important;
    }

    .survey-table:not(.poll-results) .survey-option input {
        width: 13px !important;
        height: 13px !important;
    }

    .survey-percentage .percentage {
        width: 54px !important;
    }`;
var head = document.head || document.getElementsByTagName('head')[0];
var style = document.createElement('style');

head.appendChild(style);
style.type = 'text/css';
if (style.styleSheet) {
    style.styleSheet.cssText = css;
} else {
    style.appendChild(document.createTextNode(css));
}
"""
