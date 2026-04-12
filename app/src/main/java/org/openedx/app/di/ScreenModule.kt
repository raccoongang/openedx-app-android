package org.openedx.app.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openedx.app.AppViewModel
import org.openedx.course.presentation.unit.video.EncodedVideoUnitViewModel

/**
 * Android-specific screen bindings.
 * Common ViewModels are in [commonScreenModule] (shared/commonMain).
 */
val screenModule = module {
    includes(commonScreenModule)

    viewModel {
        AppViewModel(
            config = get(),
            appNotifier = get(),
            room = get(),
            preferencesManager = get(),
            dispatcher = get(named("IODispatcher")),
            analytics = get(),
            deepLinkRouter = get(),
            fileUtil = get(),
            downloadNotifier = get(),
            context = get(),
            resourceManager = get(),
        )
    }

    viewModel { (courseId: String, videoUrl: String, blockId: String) ->
        EncodedVideoUnitViewModel(
            courseId, videoUrl, blockId,
            get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
}
