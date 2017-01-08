package com.academiaexpress.Activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Selection
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.academiaexpress.Base.BaseActivity
import com.academiaexpress.R
import com.academiaexpress.Server.DeviceInfoStore
import com.academiaexpress.Utils.AndroidUtilities
import com.academiaexpress.Utils.Image

class AuthActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.email_screen)

        DeviceInfoStore.resetToken(this)
        DeviceInfoStore.resetUser(this)

        (findViewById(R.id.phone) as EditText).addTextChangedListener(PhoneNumberFormattingTextWatcher())

        val span = Spannable.Factory.getInstance().newSpannable(
                "Нажимая на кнопку \"ДАЛЕЕ\", вы принимаете правила")
        span.setSpan(object : TouchableSpan(
                Color.parseColor("#212121"),
                Color.parseColor("#212121"),
                Color.TRANSPARENT) {
            override fun onClick(v: View) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://academia-delivery.herokuapp.com/docs/terms_of_service.html"))
                startActivity(browserIntent)
            }
        }, 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        (findViewById(R.id.textView61) as TextView).movementMethod = LinkTouchMovementMethod()
        (findViewById(R.id.textView61) as TextView).text = span

        Image.loadPhoto(R.drawable.back1, findViewById(R.id.imageView21) as ImageView)

        findViewById(R.id.textView7).setOnClickListener(View.OnClickListener {
            if (!AndroidUtilities.isConnected(this@AuthActivity)) return@OnClickListener

            if ((findViewById(R.id.phone) as EditText).text.toString().length < 5) {
                Snackbar.make(findViewById(R.id.main), "Неверный формат номера.", Snackbar.LENGTH_SHORT).show()
                return@OnClickListener
            }

            val intent = Intent(this@AuthActivity, CodeActivity::class.java)
            intent.putExtra("first", true)
            intent.putExtra("phone", (findViewById(R.id.phone) as EditText).text.toString())
            startActivity(intent)
        })
    }

    abstract inner class TouchableSpan(private val mNormalTextColor: Int, private val mPressedTextColor: Int, private val mPressedBackgroundColor: Int) : ClickableSpan() {
        private var mIsPressed: Boolean = false

        fun setPressed(isSelected: Boolean) {
            mIsPressed = isSelected
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = if (mIsPressed) mPressedTextColor else mNormalTextColor
            ds.bgColor = if (mIsPressed) mPressedBackgroundColor else Color.TRANSPARENT
            ds.isUnderlineText = true
        }
    }

    private inner class LinkTouchMovementMethod : LinkMovementMethod() {
        private var mPressedSpan: TouchableSpan? = null

        override fun onTouchEvent(textView: TextView, spannable: Spannable, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                mPressedSpan = getPressedSpan(textView, spannable, event)
                if (mPressedSpan != null) {
                    mPressedSpan!!.setPressed(true)
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan))
                }
            } else if (event.action == MotionEvent.ACTION_MOVE) {
                val touchedSpan = getPressedSpan(textView, spannable, event)
                if (mPressedSpan != null && touchedSpan !== mPressedSpan) {
                    mPressedSpan!!.setPressed(false)
                    mPressedSpan = null
                    Selection.removeSelection(spannable)
                }
            } else {
                if (mPressedSpan != null) {
                    mPressedSpan!!.setPressed(false)
                    super.onTouchEvent(textView, spannable, event)
                }
                mPressedSpan = null
                Selection.removeSelection(spannable)
            }
            return true
        }

        private fun getPressedSpan(textView: TextView, spannable: Spannable, event: MotionEvent): TouchableSpan {

            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= textView.totalPaddingLeft
            y -= textView.totalPaddingTop

            x += textView.scrollX
            y += textView.scrollY

            val layout = textView.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = spannable.getSpans(off, off, TouchableSpan::class.java)
            var touchedSpan: TouchableSpan? = null
            if (link.isNotEmpty()) {
                touchedSpan = link[0]
            }
            return touchedSpan!!
        }

    }
}
