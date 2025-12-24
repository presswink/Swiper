package moe.tlaster.swiper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.withSign

@Composable
fun rememberSwiperState(
    onStart: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onEnd: () -> Unit = {},
): SwiperState {
    return rememberSaveable(
        saver = SwiperState.Saver(
            onStart, onDismiss, onEnd
        )
    ) {
        SwiperState(
            onStart = onStart,
            onDismiss = onDismiss,
            onEnd = onEnd,
        )
    }
}

@Stable
class SwiperState(
    internal val onStart: () -> Unit = {},
    internal val onDismiss: () -> Unit = {},
    internal val onEnd: () -> Unit = {},
    initialOffset: Float = 0f,
) {
    internal var maxHeight: Int = 0
        set(value) {
            field = value
            _offset.updateBounds(lowerBound = -value.toFloat(), upperBound = value.toFloat())
        }
    internal var dismissed by mutableStateOf(false)
    private var _offset = Animatable(initialOffset)
    private lateinit var _direction: Direction
    private var _dismissHeight: Float = 0f
    private var _animDuration: Int = 0

    val offset: Float
        get() = _offset.value

    val progress: Float
        get() = (offset.absoluteValue / (if (maxHeight == 0) 1 else maxHeight)).coerceIn(
            maximumValue = 1f,
            minimumValue = 0f
        )

    internal suspend fun snap(value: Float) {
        _offset.snapTo(value)
    }

    internal var direction: Direction = Direction.Up
        set(value) {
            _direction = value
            field = value
        }

    internal var dismissHeight: Float = 0f
        set(value) {
            _dismissHeight = value
            field = value
        }

    internal var animDuration: Int = 0
        set(value) {
            _animDuration = value
            field = value
        }

    internal suspend fun fling(velocity: Float) {
        val value = _offset.value
        val calcCurrentHeight =  abs(value) / 1000
        if(velocity.absoluteValue >= 0.0f && calcCurrentHeight > dismissHeight){
            when {
                _direction == Direction.Up && value < 0.0 -> {
                    dismiss(velocity)
                }
                _direction == Direction.Down && value > 0.0 -> {
                    dismiss(velocity)
                }
                _direction == Direction.Left && value < 0.0 -> {
                    dismiss(velocity)
                }
                _direction == Direction.Right && value > 0.0 -> {
                    dismiss(velocity)
                }
                else -> {
                    restore()
                }
            }
        }else {
            restore()
        }
    }

    private suspend fun dismiss(velocity: Float) {
        dismissed = true
        _offset.animateTo(maxHeight.toFloat().withSign(_offset.value), initialVelocity = velocity)
        onDismiss.invoke()
        restore()
    }

    private suspend fun restore() {
        onEnd.invoke()
        _offset.animateTo(1f)
        dismissed = false
    }

    private suspend fun clickToDismiss(){
        dismissed = true
        when (_direction) {
            Direction.Right, Direction.Up -> {
                _offset.animateTo(( - maxHeight.toFloat()), initialVelocity = 0.0f, animationSpec = tween(_animDuration))
                onDismiss.invoke()
            }
            Direction.Left, Direction.Down -> {
                _offset.animateTo(maxHeight.toFloat(), initialVelocity = 0.0f, animationSpec = tween(_animDuration))
                onDismiss.invoke()
            }
        }
        restore()
    }
    suspend fun dismissIt(){
       clickToDismiss()
    }

    companion object {
        fun Saver(
            onStart: () -> Unit = {},
            onDismiss: () -> Unit = {},
            onEnd: () -> Unit = {},
        ): Saver<SwiperState, *> = listSaver(
            save = {
                listOf(
                    it.offset,
                )
            },
            restore = {
                SwiperState(
                    onStart = onStart,
                    onDismiss = onDismiss,
                    onEnd = onEnd,
                    initialOffset = it[0]
                )
            }
        )
    }
}

