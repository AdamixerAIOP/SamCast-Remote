package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.model.CastDirectionState
import com.example.model.RemoteUiState
import com.example.ui.RemoteScreen
import com.example.ui.theme.SamCastRemoteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appNameMatchesSamCastRemote() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SamCast Remote", appName)
    }

    @Test
    fun castButtonTriggersDirectionCallbackAndDisplaysBothOptions() {
        var castClicked = false
        composeRule.setContent {
            SamCastRemoteTheme {
                RemoteScreen(
                    uiState = RemoteUiState(castState = CastDirectionState.ChoosingDirection),
                    onButtonPressStart = {},
                    onButtonPressEnd = {},
                    onSettings123Click = {},
                    onVoiceClick = {},
                    onCastClick = { castClicked = true },
                    onOpenBluetoothSheet = {},
                    onDismissBluetoothSheet = {},
                    onRequestBtPermissions = {},
                    onEnableBluetooth = {},
                    onMakeDiscoverable = {},
                    onScanBtDevices = {},
                    onConnectBtDevice = {},
                    onDisconnectBt = {},
                    onDismissKeypadSheet = {},
                    onToggleAppLaunchMacro = {},
                    onSendCustomText = {},
                    onDismissCastPopupOrOverlay = {},
                    onChoosePhoneToTv = {},
                    onChooseTvToPhone = {},
                    onOpenSystemCastAgain = {},
                    onStopPhoneToTvCast = {},
                    onRescanTvStreams = {},
                    onSelectTvStream = {},
                    onConnectCustomStreamUrl = {},
                    onPlaybackReady = {},
                    onPlaybackError = {},
                    onStopActiveTvStream = {}
                )
            }
        }

        composeRule.onNodeWithTag("btn_cast_phone_to_tv").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_cast_tv_to_phone").assertIsDisplayed()
        composeRule.onNodeWithText("Cast Phone to TV").assertIsDisplayed()
        composeRule.onNodeWithText("Cast TV to Phone").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_cast_popup_close").performClick()
        assertTrue(!castClicked)
    }
}
