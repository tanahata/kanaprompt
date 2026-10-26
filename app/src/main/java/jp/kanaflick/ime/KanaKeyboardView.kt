package jp.kanaflick.ime

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.Gravity
import android.widget.PopupWindow
import kotlin.math.abs

class KanaKeyboardView(context: Context, private val previousKana: () -> String?, private val emit: (String) -> Unit) : View(context) {
    private val layout = KeyboardLayout()
    private val rows get() = layout.rows
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors get() = KeyboardTheme.forNight(
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    )
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        invalidate()
        previewView?.invalidate()
    }

    private val d = resources.displayMetrics.density
    private var pressed: Pair<Int,Int>? = null
    private var pointer = -1
    private var startX = 0f; private var startY = 0f
    private var direction = 0 // center left up right down
    private var preview: PopupWindow? = null
    private var previewView: View? = null

    private fun hidePreview() {
        preview?.dismiss()
        preview = null
        previewView = null
    }

    private fun showPreview(row: Int, column: Int) {
        hidePreview()
        val key = rows[row][column]
        val options = if (key == "゛小") {
            DakutenConverter.options(previousKana() ?: "")
        } else layout.options(key) ?: return
        if (options.all { it.isEmpty() }) return
        if (windowToken == null) return
        val size = (60 * d).toInt()
        val bubble = object : View(context) {
            private val ink = Paint(Paint.ANTI_ALIAS_FLAG)
            override fun onDraw(canvas: Canvas) {
                val cx = width / 2f; val cy = height / 2f
                ink.color = colors.key
                canvas.drawCircle(cx, cy, 28*d, ink)
                if (direction != 0) {
                    // Gboard's directional preview replaces the five-way guide.
                    val char = options[direction]
                    if (char.isNotEmpty()) {
                        ink.color = colors.text
                        ink.textAlign = Paint.Align.CENTER
                        ink.textSize = 32*d
                        ink.isFakeBoldText = false
                        canvas.drawText(char, cx, cy-(ink.ascent()+ink.descent())/2, ink)
                    }
                    return
                }
                val offsets = listOf(0f to 0f, -18f to 0f, 0f to -18f, 18f to 0f, 0f to 18f)
                options.forEachIndexed { index, char ->
                    if (char.isEmpty()) return@forEachIndexed
                    val (dx, dy) = offsets[index]
                    val x = cx + dx*d; val y = cy + dy*d
                    val selected = index == direction
                    ink.color = if (selected) colors.pressed else colors.key
                    canvas.drawCircle(x, y, (if (index == 0) 11 else 8)*d, ink)
                    ink.color = colors.text
                    ink.textAlign = Paint.Align.CENTER
                    ink.textSize = (if (index == 0) 21 else 13)*d
                    ink.isFakeBoldText = selected
                    canvas.drawText(char, x, y-(ink.ascent()+ink.descent())/2, ink)
                }
            }
        }
        val popup = PopupWindow(bubble, size, size, false).apply {
            isTouchable = false
            isClippingEnabled = false
            setIsLaidOutInScreen(true)
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        }
        val position = IntArray(2)
        getLocationOnScreen(position)
        val cx = (layout.keyStart(row, column) + layout.keyWidth(row, column)/2) * width
        val keyTop = topPadding + row * keyAreaHeight/4
        val x = (position[0] + cx - size/2).toInt().coerceIn(position[0], position[0] + width - size)
        val y = (position[1] + keyTop - size - 4*d).toInt().coerceAtLeast(0)
        preview = popup
        previewView = bubble
        popup.showAtLocation(this, Gravity.TOP or Gravity.LEFT, x, y)
    }

    private val deleteRepeat = object : Runnable {
        override fun run() {
            val p = pressed ?: return
            if (rows[p.first][p.second] != "⌫" || pointer == -1) return
            emit("⌫")
            postDelayed(this, 65L)
        }
    }

    private val deleteLeftHold = Runnable {
        val p = pressed
        if (p != null && pointer != -1 && rows[p.first][p.second] == "左全削除") {
            emit("左全削除")
        }
    }

    private fun stopDeleteRepeat() {
        removeCallbacks(deleteRepeat)
        removeCallbacks(deleteLeftHold)
    }

    override fun onDetachedFromWindow() {
        stopDeleteRepeat()
        hidePreview()
        pressed = null
        pointer = -1
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != VISIBLE) {
            stopDeleteRepeat()
            hidePreview()
            pressed = null
            pointer = -1
        }
    }

    private val topPadding get() = 8f * d
    private val gap get() = layout.keyGapDp * d
    // Reserve space below the keys so the entire key grid sits higher on screen.
    private val keyAreaHeight get() = layout.keyAreaHeightDp * d
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), ((layout.keyAreaHeightDp + 48 + 8)*d).toInt())
    }
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(colors.background)
        val rh = keyAreaHeight / 4f
        rows.forEachIndexed { r, row -> row.forEachIndexed { c, label ->
            val left = layout.keyStart(r, c) * width
            val cw = layout.keyWidth(r, c) * width
            val selected = pressed == (r to c) || layout.isShiftActive(label)
            paint.color = if (selected) colors.pressed else if (label in listOf("左全削除", "◀", "▶", "あ1", "かな", "あa", "⇧", "⇪", "⌫", "空白", "↵", "?123", "ABC", "=\\<")) colors.utility else colors.key
            canvas.drawRoundRect(RectF(left+gap/2,topPadding+r*rh+gap/2,left+cw-gap/2,topPadding+(r+1)*rh-gap/2), 8*d,8*d,paint)
            paint.color=if (label in listOf("あa", "↵")) colors.accent else colors.text
            paint.textAlign=Paint.Align.CENTER
            paint.textSize=(if (layout.displayLabel(label).length>2) 13 else 22)*d
            val cx = left + cw/2
            val cy = topPadding + (r+.5f)*rh
            if (label in listOf("⇧", "⇪", "↵", "⌫", "左全削除") || layout.displayLabel(label) == "␣") {
                drawActionIcon(canvas, label, cx, cy)
            } else {
                paint.isFakeBoldText = label in listOf("?123", "ABC", "=\\<")
                canvas.drawText(layout.displayLabel(label),cx,cy-(paint.ascent()+paint.descent())/2,paint)
                paint.isFakeBoldText = false
            }
        }}
        // Identity stays in the existing non-interactive bottom margin.
        paint.color = colors.accent
        paint.textSize = 11*d
        paint.textAlign = Paint.Align.CENTER
        val signatureY = topPadding + keyAreaHeight + 24*d
        val title = context.getString(R.string.app_name)
        canvas.drawText(title, width/2f + 10*d, signatureY-(paint.ascent()+paint.descent())/2, paint)
        val markX = width/2f - paint.measureText(title)/2 - 8*d
        canvas.drawRoundRect(RectF(markX-8*d, signatureY-1.5f*d, markX+8*d, signatureY+1.5f*d), 1.5f*d, 1.5f*d, paint)
    }
    /** Fixed dp outlines avoid font-dependent sizes for Shift lock and Enter. */
    private fun drawActionIcon(canvas: Canvas, key: String, cx: Float, cy: Float) {
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(d, d)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        val path = Path()
        if (key == "⌫" || key == "左全削除") {
            val dx = 0f
            path.moveTo(-4f + dx, -8f)
            path.lineTo(12f + dx, -8f)
            path.lineTo(12f + dx, 8f)
            path.lineTo(-4f + dx, 8f)
            path.lineTo(-12f + dx, 0f)
            path.close()
            path.moveTo(-2f + dx, -4f)
            path.lineTo(6f + dx, 4f)
            path.moveTo(6f + dx, -4f)
            path.lineTo(-2f + dx, 4f)
            if (key == "左全削除") {
                // Same 45-degree edges as the backspace outline.
                path.moveTo(-11f, -8f)
                path.lineTo(-19f, 0f)
                path.lineTo(-11f, 8f)
            }
        } else if (key == "空白") {
            path.moveTo(-10f, -3f)
            path.lineTo(-10f, 4f)
            path.lineTo(10f, 4f)
            path.lineTo(10f, -3f)
        } else if (key == "↵") {
            path.moveTo(11f, -8f)
            path.lineTo(11f, 3f)
            path.lineTo(-11f, 3f)
            path.moveTo(-4f, -4f)
            path.lineTo(-11f, 3f)
            path.lineTo(-4f, 10f)
        } else {
            val dy = if (key == "⇪") -2f else 0f
            path.moveTo(0f, -11f + dy)
            path.lineTo(-10f, -1f + dy)
            path.lineTo(-5f, -1f + dy)
            path.lineTo(-5f, 9f + dy)
            path.lineTo(5f, 9f + dy)
            path.lineTo(5f, -1f + dy)
            path.lineTo(10f, -1f + dy)
            path.close()
            if (key == "⇪") {
                path.moveTo(-5f, 12f)
                path.lineTo(5f, 12f)
            }
        }
        if (key == "⌫" || key == "左全削除") {
            val bounds = RectF()
            path.computeBounds(bounds, true)
            canvas.translate(-bounds.centerX(), -bounds.centerY())
        }
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                stopDeleteRepeat()
                hidePreview()
                pointer=event.getPointerId(0); startX=event.x; startY=event.y; direction=0
                if (event.x < 0 || event.x >= width || event.y < topPadding || event.y >= topPadding + keyAreaHeight) { pressed=null; invalidate(); return true }
                val r=((event.y-topPadding)/(keyAreaHeight/4f)).toInt().coerceIn(0,3)
                val c=layout.keyIndex(r, event.x/width)
                if (c < 0) { pressed=null; pointer=-1; invalidate(); return true }
                pressed=r to c
                showPreview(r, c)
                if (rows[r][c] == "⌫") {
                    emit("⌫")
                    postDelayed(deleteRepeat, 400L)
                } else if (rows[r][c] == "左全削除") {
                    postDelayed(deleteLeftHold, 350L)
                }
                invalidate(); return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.getPointerId(event.actionIndex) != pointer && event.pointerCount == 1) return true
                val i=event.findPointerIndex(pointer); if(i<0) return true
                val p = pressed
                if (p != null && rows[p.first][p.second] in listOf("⌫", "左全削除")) {
                    val x = event.getX(i); val y = event.getY(i)
                    val left = layout.keyStart(p.first, p.second) * width
                    val right = left + layout.keyWidth(p.first, p.second) * width
                    val top = topPadding + p.first * keyAreaHeight / 4f
                    if (x < left || x >= right || y < top || y >= top + keyAreaHeight / 4f) {
                        stopDeleteRepeat()
                        pressed = null
                        invalidate()
                    }
                    return true
                }
                val dx=event.getX(i)-startX; val dy=event.getY(i)-startY
                direction=if(maxOf(abs(dx),abs(dy))<12*d) 0 else if(abs(dx)>abs(dy)) { if(dx<0) 1 else 3 } else { if(dy<0) 2 else 4 }
                previewView?.invalidate()
                invalidate(); return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == pointer) {
                    stopDeleteRepeat()
                    hidePreview()
                    pressed=null; pointer=-1; invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                stopDeleteRepeat()
                hidePreview()
                val p=pressed; if(p!=null && event.getPointerId(event.actionIndex)==pointer) {
                    val key=rows[p.first][p.second]
                    val previousHeight = layout.keyAreaHeightDp
                    val value = layout.press(key, direction)
                    if (layout.keyAreaHeightDp != previousHeight) requestLayout()
                    if (key !in listOf("⌫", "左全削除") && !value.isNullOrEmpty()) emit(value)
                }
                pressed=null; pointer=-1; invalidate(); return true
            }
            MotionEvent.ACTION_CANCEL -> { stopDeleteRepeat(); hidePreview(); pressed=null; pointer=-1; invalidate(); return true }
        }
        return true
    }
}
