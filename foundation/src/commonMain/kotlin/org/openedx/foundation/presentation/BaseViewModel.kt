package org.openedx.foundation.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.openedx.foundation.extension.isInternetError

open class BaseViewModel(
    private val noConnectionMessage: String = NO_CONNECTION_MESSAGE,
    private val defaultErrorMessage: String = UNKNOWN_ERROR_MESSAGE,
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiMessage = MutableSharedFlow<UIMessage>(
        replay = 1,
        extraBufferCapacity = 0,
    )
    val uiMessage: SharedFlow<UIMessage> = _uiMessage.asSharedFlow()

    protected fun resolveErrorMessage(
        throwable: Throwable?,
        noConnectionMessage: String = this.noConnectionMessage,
        defaultErrorMessage: String = this.defaultErrorMessage,
    ): String {
        return if (throwable?.isInternetError() == true) {
            noConnectionMessage
        } else {
            defaultErrorMessage
        }
    }

    protected suspend fun handleErrorUiMessage(
        throwable: Throwable? = null,
        noConnectionMessage: String = this.noConnectionMessage,
        defaultErrorMessage: String = this.defaultErrorMessage,
    ) {
        val message = resolveErrorMessage(throwable, noConnectionMessage, defaultErrorMessage)
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

    companion object {
        const val NO_CONNECTION_MESSAGE =
            "You are not connected to the Internet. Please check your Internet connection."
        const val UNKNOWN_ERROR_MESSAGE =
            "Something went wrong. Please try again later."
    }
}
