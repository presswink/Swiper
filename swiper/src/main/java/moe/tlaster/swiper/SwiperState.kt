package moe.tlaster.swiper

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.withSign

@Composable
fun rememberSwiperState(
    onStart: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onEnd: () -> Unit = {},
    key: String? = null
): SwiperState {
   val scope = rememberCoroutineScope()
    return rememberSaveable(
        key = key,
        saver = SwiperState.Saver(
            onStart, onDismiss, onEnd, scope
        )
    ) {
        SwiperState(
            onStart = onStart,
            onDismiss = onDismiss,
            onEnd = onEnd,
            scope= scope
        )
    }
}

@Stable
class SwiperState(
    internal val onStart: () -> Unit = {},
    internal val onDismiss: () -> Unit = {},
    internal val onEnd: () -> Unit = {},
    initialOffset: Float = 0f,
    val scope: CoroutineScope,
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

    internal fun snap(value: Float) {
        scope.launch {
            _offset.snapTo(value)
        }
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

    internal fun fling(velocity: Float) {
        val value = _offset.value
        val progress = (value.absoluteValue / maxHeight).coerceIn(0f, 1f)
        if(progress > dismissHeight){
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

    private fun dismiss(velocity: Float) {
        dismissed = true
        scope.launch {
            _offset.animateTo(maxHeight.toFloat().withSign(_offset.value), initialVelocity = velocity)
        }
        restore()
        onDismiss.invoke()
    }

    private fun restore() {
        onEnd.invoke()
        scope.launch {
            _offset.animateTo(0f)
        }
        dismissed = false
    }

    private fun clickToDismiss(){
        dismissed = true
        val targetValue = when (_direction) {
            Direction.Down, Direction.Right -> maxHeight.toFloat()
            Direction.Up, Direction.Left -> -maxHeight.toFloat()
        }
        scope.launch {
            _offset.animateTo(targetValue, animationSpec = tween(_animDuration))
        }
        onDismiss.invoke()
        restore()
    }

    @Suppress("unused")
    fun dismissIt(){
        clickToDismiss()
    }

    companion object {
        fun Saver(
            onStart: () -> Unit = {},
            onDismiss: () -> Unit = {},
            onEnd: () -> Unit = {},
            scope: CoroutineScope,
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
                    initialOffset = it[0],
                    scope = scope
                )
            }
        )
    }
}

