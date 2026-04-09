package org.openedx.foundation.presentation

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope

fun TestScope.captureUiMessage(viewModel: BaseViewModel): Deferred<UIMessage?> {
    return async {
        kotlinx.coroutines.withTimeoutOrNull(100) {
            viewModel.uiMessage.first()
        }
    }
}
