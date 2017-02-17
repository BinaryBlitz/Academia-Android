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
import com.academiaexpress.Utils.AppConfig
import com.academiaexpress.Utils.Image

class AuthActivity : BaseActivity() {

    private val DEFAULT_COLOR = "#212121"
    private val EXTRA_PHONE = "phone"
    private val EXTRA_FIRST = "first"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_input)

        resetData()
        initElements()
        setOnClickListeners()
    }

    private fun resetData() {
        DeviceInfoStore.resetToken(this)
        DeviceInfoStore.resetUser(this)
    }

    private fun initSpan() {
        val span = Spannable.Factory.getInstance().newSpannable("Нажимая на кнопку \"ДАЛЕЕ\", вы принимаете правила")
        span.setSpan(object : TouchableSpan(Color.parseColor(DEFAULT_COLOR), Color.parseColor(DEFAULT_COLOR), Color.TRANSPARENT) {
            override fun onClick(v: View) { browseTerms() } }, 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        (findViewById(R.id.help_text) as TextView).movementMethod = LinkTouchMovementMethod()
        (findViewById(R.id.help_text) as TextView).text = span
    }

    private fun browseTerms() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.termsUrl))
        startActivity(browserIntent)
    }

    private fun initElements() {
        Image.loadPhoto(R.drawable.back1, findViewById(R.id.background) as ImageView)
        (findViewById(R.id.phone) as EditText).addTextChangedListener(PhoneNumberFormattingTextWatcher())

        initSpan()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.next_btn).setOnClickListener(View.OnClickListener {
            if (!AndroidUtilities.isConnected(this@AuthActivity) || !check()) {
                return@OnClickListener
            }

            openCodeActivity()
        })
    }

    private fun check(): Boolean {
        if ((findViewById(R.id.phone) as EditText).text.toString().length < 5) {
            Snackbar.make(findViewById(R.id.main), getString(R.string.wrong_phone_format), Snackbar.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun openCodeActivity() {
        val intent = Intent(this@AuthActivity, CodeActivity::class.java)
        intent.putExtra(EXTRA_FIRST, true)
        intent.putExtra(EXTRA_PHONE, (findViewById(R.id.phone) as EditText).text.toString())
        startActivity(intent)
    }

    abstract inner class TouchableSpan(private val mNormalTextColor: Int, private val mPressedTextColor: Int,
                                       private val mPressedBackgroundColor: Int) : ClickableSpan() {
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
            if (event.action == MotionEvent.ACTION_DOWN) processDown(spannable)
            else if (event.action == MotionEvent.ACTION_MOVE) processMove(textView, spannable, event)
            else processStandart(textView, spannable, event)

            return true
        }

        private fun processStandart(textView: TextView, spannable: Spannable, event: MotionEvent) {
            if (mPressedSpan != null) {
                mPressedSpan!!.setPressed(false)
                super.onTouchEvent(textView, spannable, event)
            }
            mPressedSpan = null
            Selection.removeSelection(spannable)
        }

        private fun processMove(textView: TextView, spannable: Spannable, event: MotionEvent) {
            val touchedSpan = getPressedSpan(textView, spannable, event)
            if (mPressedSpan != null && touchedSpan !== mPressedSpan) {
                mPressedSpan!!.setPressed(false)
                mPressedSpan = null
                Selection.removeSelection(spannable)
            }
        }

        private fun processDown(spannable: Spannable) {
            if (mPressedSpan != null) {
                mPressedSpan!!.setPressed(true)
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan))
            }
        }

        private fun getPressedSpan(textView: TextView, spannable: Spannable, event: MotionEvent): TouchableSpan {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x = setX(x, textView)
            y = setY(y, textView)

            val layout = textView.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = spannable.getSpans(off, off, TouchableSpan::class.java)
            var touchedSpan: TouchableSpan? = null

            if (link.isNotEmpty()) touchedSpan = link[0]

            return touchedSpan!!
        }

        private fun setX(x: Int, textView: TextView): Int {
            var newX = x

            newX -= textView.totalPaddingLeft
            newX += textView.scrollX

            return newX
        }

        private fun setY(y: Int, textView: TextView): Int {
            var newY = y

            newY -= textView.totalPaddingTop
            newY += textView.scrollY

            return newY
        }
    }
}
