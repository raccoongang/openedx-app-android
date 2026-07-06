package org.openedx.auth.presentation.startup

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.openedx.auth.R
import org.openedx.auth.presentation.AuthRouter
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.R as coreR

class SandboxSplashFragment : Fragment() {

    private val router by inject<AuthRouter>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                SandboxSplashScreen(
                    onStartExploringClick = {
                        router.navigateToSignIn(parentFragmentManager, null, null)
                    }
                )
            }
        }
    }
}

@Composable
private fun SandboxSplashScreen(onStartExploringClick: () -> Unit) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.3f),
            painter = painterResource(id = coreR.drawable.core_top_header),
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.2f),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = coreR.drawable.core_ic_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .size(width = 263.dp, height = 76.dp),
                )
            }
            Surface(
                color = MaterialTheme.appColors.background,
                shape = MaterialTheme.appShapes.screenBackgroundShape,
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .displayCutoutForLandscape()
                        .padding(horizontal = 24.dp)
                        .padding(top = 50.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        modifier = Modifier
                            .testTag("txt_sandbox_splash_title")
                            .fillMaxWidth(),
                        text = stringResource(id = R.string.auth_sandbox_splash_title),
                        color = MaterialTheme.appColors.textPrimary,
                        style = MaterialTheme.appTypography.displaySmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier
                            .testTag("txt_sandbox_splash_subtitle")
                            .fillMaxWidth(),
                        text = stringResource(id = R.string.auth_sandbox_splash_subtitle),
                        color = MaterialTheme.appColors.textPrimary,
                        style = MaterialTheme.appTypography.titleSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OpenEdXButton(
                        modifier = Modifier
                            .testTag("btn_sandbox_splash_start")
                            .fillMaxWidth()
                            .height(42.dp),
                        onClick = onStartExploringClick,
                    ) {
                        Text(
                            text = stringResource(id = R.string.auth_sandbox_splash_start),
                            color = MaterialTheme.appColors.primaryButtonText,
                            style = MaterialTheme.appTypography.labelLarge,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.primaryButtonText,
                        )
                    }
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "NEXUS_5_Light", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_5_Dark", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SandboxSplashScreenPreview() {
    OpenEdXTheme {
        SandboxSplashScreen(onStartExploringClick = {})
    }
}
