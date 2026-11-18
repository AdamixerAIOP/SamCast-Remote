package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RemoteButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ButtonHighlight
import com.example.ui.theme.ButtonShadow
import com.example.ui.theme.ButtonSurface
import com.example.ui.theme.ButtonSurfacePressed
import com.example.ui.theme.DisneyCyan
import com.example.ui.theme.DotBlue
import com.example.ui.theme.DotGreen
import com.example.ui.theme.DotRed
import com.example.ui.theme.DotYellow
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.PowerCrimson
import com.example.ui.theme.PrimeBlue
import com.example.ui.theme.RockerBarBottom
import com.example.ui.theme.RockerBarTop
import com.example.ui.theme.SamsungTvCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoiceEmerald

@Composable
fun TactileRemoteButton(
    button: RemoteButton,
    contentDescription: String,
    testTag: String,
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = CircleShape,
    glowColor: Color? = null,
    onTapOverride: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var hasPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hasPressed = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (onTapOverride == null) {
                onPressStart(button)
            }
        } else if (hasPressed) {
            hasPressed = false
            if (onTapOverride == null) {
                onPressEnd(button)
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_scale"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isPressed && glowColor != null -> glowColor
            isPressed -> AccentCyan.copy(alpha = 0.7f)
            glowColor != null -> glowColor.copy(alpha = 0.45f)
            else -> ButtonHighlight.copy(alpha = 0.65f)
        },
        label = "btn_border"
    )

    val surfaceBrush = if (isPressed) {
        Brush.verticalGradient(listOf(ButtonSurfacePressed, ButtonSurface))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF2D3139), ButtonSurface))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 6.dp,
                shape = shape,
                ambientColor = ButtonShadow,
                spotColor = glowColor ?: ButtonShadow
            )
            .clip(shape)
            .background(surfaceBrush)
            .border(1.dp, borderColor, shape)
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = glowColor ?: AccentCyan),
                role = Role.Button,
                onClick = {
                    if (onTapOverride != null) {
                        onTapOverride()
                    } else if (!hasPressed) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPressStart(button)
                        onPressEnd(button)
                    }
                }
            )
    ) {
        content()
    }
}

@Composable
fun PowerButton(
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    contentDescription: String
) {
    TactileRemoteButton(
        button = RemoteButton.POWER,
        contentDescription = contentDescription,
        testTag = "btn_power",
        onPressStart = onPressStart,
        onPressEnd = onPressEnd,
        size = 50.dp,
        glowColor = PowerCrimson
    ) {
        Icon(
            imageVector = Icons.Rounded.PowerSettingsNew,
            contentDescription = null,
            tint = PowerCrimson,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun Settings123Button(
    onClick: () -> Unit,
    contentDescription: String
) {
    TactileRemoteButton(
        button = RemoteButton.SETTINGS_123,
        contentDescription = contentDescription,
        testTag = "btn_settings_123",
        onPressStart = {},
        onPressEnd = {},
        onTapOverride = onClick,
        size = 56.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(13.dp)) {
                val r = size.minDimension / 2f
                drawCircle(
                    color = TextPrimary,
                    radius = r * 0.65f,
                    style = Stroke(width = 2.2f)
                )
                for (i in 0 until 8) {
                    val angle = Math.toRadians(i * 45.0)
                    val start = Offset(
                        x = center.x + (r * 0.62f * kotlin.math.cos(angle)).toFloat(),
                        y = center.y + (r * 0.62f * kotlin.math.sin(angle)).toFloat()
                    )
                    val end = Offset(
                        x = center.x + (r * kotlin.math.cos(angle)).toFloat(),
                        y = center.y + (r * kotlin.math.sin(angle)).toFloat()
                    )
                    drawLine(TextPrimary, start, end, strokeWidth = 2.2f)
                }
            }
            Text(
                text = "123",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                listOf(DotRed, DotGreen, DotYellow, DotBlue).forEach { dotColor ->
                    Box(
                        modifier = Modifier
                            .size(4.5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    TactileRemoteButton(
        button = RemoteButton.VOICE,
        contentDescription = contentDescription,
        testTag = "btn_voice",
        onPressStart = {},
        onPressEnd = {},
        onTapOverride = onClick,
        size = 56.dp,
        glowColor = if (isListening) VoiceEmerald else null
    ) {
        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription = null,
            tint = if (isListening) VoiceEmerald else TextPrimary,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
fun SamsungDpadRing(
    pressedButton: RemoteButton?,
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    cdUp: String,
    cdDown: String,
    cdLeft: String,
    cdRight: String,
    cdOk: String
) {
    val outerRingBrush = Brush.radialGradient(
        colors = listOf(Color(0xFF282B33), Color(0xFF1D1F25), Color(0xFF16181C))
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(196.dp)
            .shadow(10.dp, CircleShape, ambientColor = ButtonShadow, spotColor = ButtonShadow)
            .clip(CircleShape)
            .background(outerRingBrush)
            .border(1.2.dp, ButtonHighlight.copy(alpha = 0.7f), CircleShape)
    ) {
        // Subtle inner directional cross indicator
        Canvas(modifier = Modifier.fillMaxSize()) {
            val active = pressedButton
            val highlight = AccentCyan.copy(alpha = 0.35f)
            val centerPt = center
            when (active) {
                RemoteButton.DPAD_UP -> drawCircle(
                    color = highlight,
                    radius = size.width * 0.16f,
                    center = Offset(centerPt.x, size.height * 0.16f)
                )
                RemoteButton.DPAD_DOWN -> drawCircle(
                    color = highlight,
                    radius = size.width * 0.16f,
                    center = Offset(centerPt.x, size.height * 0.84f)
                )
                RemoteButton.DPAD_LEFT -> drawCircle(
                    color = highlight,
                    radius = size.width * 0.16f,
                    center = Offset(size.width * 0.16f, centerPt.y)
                )
                RemoteButton.DPAD_RIGHT -> drawCircle(
                    color = highlight,
                    radius = size.width * 0.16f,
                    center = Offset(size.width * 0.84f, centerPt.y)
                )
                else -> {}
            }
        }

        // Up touch target
        DpadZone(
            button = RemoteButton.DPAD_UP,
            contentDescription = cdUp,
            testTag = "btn_dpad_up",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(64.dp, 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp, 2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextSecondary.copy(alpha = 0.55f))
            )
        }

        // Down touch target
        DpadZone(
            button = RemoteButton.DPAD_DOWN,
            contentDescription = cdDown,
            testTag = "btn_dpad_down",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .size(64.dp, 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp, 2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextSecondary.copy(alpha = 0.55f))
            )
        }

        // Left touch target
        DpadZone(
            button = RemoteButton.DPAD_LEFT,
            contentDescription = cdLeft,
            testTag = "btn_dpad_left",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(48.dp, 64.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(2.5.dp, 6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextSecondary.copy(alpha = 0.55f))
            )
        }

        // Right touch target
        DpadZone(
            button = RemoteButton.DPAD_RIGHT,
            contentDescription = cdRight,
            testTag = "btn_dpad_right",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(48.dp, 64.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(2.5.dp, 6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextSecondary.copy(alpha = 0.55f))
            )
        }

        // Center Select/OK button
        TactileRemoteButton(
            button = RemoteButton.DPAD_OK,
            contentDescription = cdOk,
            testTag = "btn_dpad_ok",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            size = 84.dp
        ) {
            // Clean recessed center disc matching Samsung Smart Remote photo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(0.75.dp, ButtonHighlight.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

@Composable
private fun DpadZone(
    button: RemoteButton,
    contentDescription: String,
    testTag: String,
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    modifier: Modifier = Modifier,
    indicator: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var hasPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hasPressed = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onPressStart(button)
        } else if (hasPressed) {
            hasPressed = false
            onPressEnd(button)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 32.dp, color = AccentCyan),
                role = Role.Button,
                onClick = {
                    if (!hasPressed) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPressStart(button)
                        onPressEnd(button)
                    }
                }
            )
    ) {
        indicator()
    }
}

@Composable
fun NavigationMediaRow(
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    cdBack: String,
    cdHome: String,
    cdPlayPause: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TactileRemoteButton(
            button = RemoteButton.BACK,
            contentDescription = cdBack,
            testTag = "btn_back",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            size = 54.dp
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardReturn,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        TactileRemoteButton(
            button = RemoteButton.HOME,
            contentDescription = cdHome,
            testTag = "btn_home",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            size = 56.dp
        ) {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(26.dp)
            )
        }

        TactileRemoteButton(
            button = RemoteButton.PLAY_PAUSE,
            contentDescription = cdPlayPause,
            testTag = "btn_play_pause",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            size = 54.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.Pause,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DualRockerControl(
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    cdVolUp: String,
    cdVolDown: String,
    cdMute: String,
    cdChUp: String,
    cdChDown: String,
    cdGuide: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Volume Rocker (+, bar mute, -)
        SingleRockerColumn(
            topButton = RemoteButton.VOL_UP,
            centerButton = RemoteButton.MUTE,
            bottomButton = RemoteButton.VOL_DOWN,
            topCd = cdVolUp,
            centerCd = cdMute,
            bottomCd = cdVolDown,
            topTag = "btn_vol_up",
            centerTag = "btn_mute",
            bottomTag = "btn_vol_down",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            topContent = {
                Text(
                    text = "+",
                    color = TextSecondary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                )
            },
            bottomContent = {
                Text(
                    text = "−",
                    color = TextSecondary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                )
            }
        )

        // Right Channel Rocker (^, bar guide, v)
        SingleRockerColumn(
            topButton = RemoteButton.CH_UP,
            centerButton = RemoteButton.CH_GUIDE,
            bottomButton = RemoteButton.CH_DOWN,
            topCd = cdChUp,
            centerCd = cdGuide,
            bottomCd = cdChDown,
            topTag = "btn_ch_up",
            centerTag = "btn_ch_guide",
            bottomTag = "btn_ch_down",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            topContent = {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            },
            bottomContent = {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        )
    }
}

@Composable
private fun SingleRockerColumn(
    topButton: RemoteButton,
    centerButton: RemoteButton,
    bottomButton: RemoteButton,
    topCd: String,
    centerCd: String,
    bottomCd: String,
    topTag: String,
    centerTag: String,
    bottomTag: String,
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(84.dp)
    ) {
        RockerTapZone(
            button = topButton,
            contentDescription = topCd,
            testTag = topTag,
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            topContent()
        }

        // Physical metallic rocker bar (tap center for Mute / Guide)
        RockerTapZone(
            button = centerButton,
            contentDescription = centerCd,
            testTag = centerTag,
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .shadow(4.dp, RoundedCornerShape(5.dp))
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.verticalGradient(listOf(RockerBarTop, RockerBarBottom)))
                    .border(0.6.dp, ButtonHighlight, RoundedCornerShape(5.dp))
            )
        }

        RockerTapZone(
            button = bottomButton,
            contentDescription = bottomCd,
            testTag = bottomTag,
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            bottomContent()
        }
    }
}

@Composable
private fun RockerTapZone(
    button: RemoteButton,
    contentDescription: String,
    testTag: String,
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var hasPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hasPressed = true
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onPressStart(button)
        } else if (hasPressed) {
            hasPressed = false
            onPressEnd(button)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AccentCyan),
                role = Role.Button,
                onClick = {
                    if (!hasPressed) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPressStart(button)
                        onPressEnd(button)
                    }
                }
            )
    ) {
        content()
    }
}

@Composable
fun AppShortcutDiamond(
    onPressStart: (RemoteButton) -> Unit,
    onPressEnd: (RemoteButton) -> Unit,
    cdSamsungTvPlus: String,
    cdNetflix: String,
    cdPrimeVideo: String,
    cdDisneyPlus: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-6).dp)
    ) {
        // Top Center: Samsung TV Plus
        TactileRemoteButton(
            button = RemoteButton.SAMSUNG_TV_PLUS,
            contentDescription = cdSamsungTvPlus,
            testTag = "btn_samsung_tv_plus",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            size = 54.dp,
            glowColor = SamsungTvCyan
        ) {
            Text(
                text = "Samsung\nTV Plus",
                color = TextPrimary,
                fontSize = 8.5.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // Middle Row: Netflix (left) and Prime Video (right)
        Row(
            horizontalArrangement = Arrangement.spacedBy(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileRemoteButton(
                button = RemoteButton.NETFLIX,
                contentDescription = cdNetflix,
                testTag = "btn_netflix",
                onPressStart = onPressStart,
                onPressEnd = onPressEnd,
                size = 54.dp,
                glowColor = NetflixRed
            ) {
                Text(
                    text = "NETFLIX",
                    color = TextPrimary,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            TactileRemoteButton(
                button = RemoteButton.PRIME_VIDEO,
                contentDescription = cdPrimeVideo,
                testTag = "btn_prime_video",
                onPressStart = onPressStart,
                onPressEnd = onPressEnd,
                size = 54.dp,
                glowColor = PrimeBlue
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "prime\nvideo",
                        color = TextPrimary,
                        fontSize = 9.sp,
                        lineHeight = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Center: Disney+
        TactileRemoteButton(
            button = RemoteButton.DISNEY_PLUS,
            contentDescription = cdDisneyPlus,
            testTag = "btn_disney_plus",
            onPressStart = onPressStart,
            onPressEnd = onPressEnd,
            size = 54.dp,
            glowColor = DisneyCyan
        ) {
            Text(
                text = "Disney+",
                color = TextPrimary,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CastActionButton(
    isCastActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(24.dp)
    val glow = if (isCastActive) AccentCyan else AccentCyan.copy(alpha = 0.45f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .height(48.dp)
            .shadow(
                elevation = if (isCastActive) 10.dp else 4.dp,
                shape = shape,
                ambientColor = glow,
                spotColor = glow
            )
            .clip(shape)
            .background(
                if (isCastActive) {
                    Brush.horizontalGradient(listOf(Color(0xFF1A3A5C), Color(0xFF17283D)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF2B2F38), ButtonSurface))
                }
            )
            .border(1.dp, glow, shape)
            .testTag("btn_cast")
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 22.dp)
    ) {
        Icon(
            imageVector = if (isCastActive) Icons.Rounded.CastConnected else Icons.Rounded.Cast,
            contentDescription = null,
            tint = AccentCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isCastActive) "CAST ACTIVE" else "CAST",
            style = MaterialTheme.typography.labelLarge,
            color = if (isCastActive) AccentCyan else TextPrimary
        )
    }
}
