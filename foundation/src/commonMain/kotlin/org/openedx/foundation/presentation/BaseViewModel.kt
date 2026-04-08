package org.openedx.foundation.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import org.openedx.foundation.R
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.system.ResourceManager

open class BaseViewModel(
    private val resourceManager: ResourceManager,
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiMessage = MutableSharedFlow<UIMessage>(
        replay = 1,
        extraBufferCapacity = 0,
    )
    val uiMessage: SharedFlow<UIMessage> = _uiMessage.asSharedFlow()

    protected fun resolveErrorMessage(
        throwable: Throwable?,
        noConnectionStringRes: Int = R.string.foundation_error_no_connection,
        defaultErrorRes: Int = R.string.foundation_error_unknown_error,
    ): String {
        return if (throwable?.isInternetError() == true) {
            resourceManager.getString(noConnectionStringRes)
        } else {
            resourceManager.getString(defaultErrorRes)
        }
    }

    protected suspend fun handleErrorUiMessage(
        throwable: Throwable? = null,
        noConnectionStringRes: Int = R.string.foundation_error_no_connection,
        defaultErrorRes: Int = R.string.foundation_error_unknown_error,
    ) {
        val message = resolveErrorMessage(throwable, noConnectionStringRes, defaultErrorRes)
        _uiMessage.emit(UIMessage.SnackBarMessage(message))
    }

    protected suspend fun sendMessage(uiMessage: UIMessage) {
        _uiMessage.emit(uiMessage)
    }

    override fun onCreate(owner: LifecycleOwner) {}
    override fun onStart(owner: LifecycleOwner) {}
    override fun onResume(owner: LifecycleOwner) {}
    override fun onPause(owner: LifecycleOwner) {}
    override fun onStop(owner: LifecycleOwner) {}
    override fun onDestroy(owner: LifecycleOwner) {}
}

fun TestScope.captureUiMessage(viewModel: BaseViewModel): Deferred<UIMessage?> {
    return async {
        kotlinx.coroutines.withTimeoutOrNull(100) {
            viewModel.uiMessage.first()
        }
    }
}
