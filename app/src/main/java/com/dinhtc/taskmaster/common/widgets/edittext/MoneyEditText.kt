package com.dinhtc.taskmaster.common.widgets.edittext

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.dinhtc.taskmaster.R
import com.dinhtc.taskmaster.utils.AndroidUtils
import com.dinhtc.taskmaster.utils.MyTextWatcher
import com.dinhtc.taskmaster.utils.setCursorPosition
import com.dinhtc.taskmaster.utils.showKeyboard
import kotlin.math.pow

class MoneyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var moneyUnit: String = DEFAULT_MONEY_UNIT
    var money: Long
        get() = try {
            AndroidUtils.decodeMoneyStr(text.toString()).toLong()
        } catch (ignore: java.lang.Exception) {
            0L
        }
        set(value) = setText("$value")

    var minAmount: Long = 0L
    var maxAmount: Long = 0L
    var preFillNumberZeroDigits: Int = 0
    private val preFillNumberStr: String
        get() {
            val tenPow = 10.toDouble().pow(preFillNumberZeroDigits.toDouble()).toLong()
            return AndroidUtils.formatMoneyCard("$tenPow", moneyUnit)
        }
    private var block = false

    init {
        setup(attrs)
    }

    private fun setup(attrs: AttributeSet?) {
        var customFont: String? = null
        attrs?.let {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MoneyEditText)
            moneyUnit = a.getString(R.styleable.MoneyEditText_money_unit) ?: DEFAULT_MONEY_UNIT
            a.recycle()
        }

        addTextChangedListener(object : MyTextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (block) return
                block = true
                val tenPow = 10.toDouble().pow(preFillNumberZeroDigits.toDouble()).toLong()
                val tenPowStr = AndroidUtils.formatMoneyCard("$tenPow", moneyUnit)
                var money = AndroidUtils.getMoneyRealValue(s?.toString())
                //quaych: According to updates from BA, ui can show 0.000.000 VND
                //if (money < minAmount) money = minAmount
//                if (money > maxAmount) money = maxAmount
                val newStr: String
                val newSelection: Int
                if ("$money".length >= "$tenPow".length) {
                    newStr = AndroidUtils.formatMoneyCard("$money", moneyUnit)
                    newSelection = newStr.length - (tenPowStr.length - 1)
                } else {
                    newStr = "0${tenPowStr.substring(1)}"
                    newSelection = 1
                }
                setText(newStr)
                setCursorPosition(newSelection, false)
                block = false
            }
        })

        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (preFillNumberZeroDigits > 0) {
                    if (block) return@setOnFocusChangeListener
                    block = true
                    //update cursor position
                    setCursorPosition(text.toString().length - (preFillNumberStr.length - 1))
                    block = false
                }
            } else {
                if (money < minAmount) money = minAmount
            }
        }
    }

    private fun setCustomFont(ctx: Context, asset: String?): Boolean {
        try {
            val typeface = Typeface.createFromAsset(ctx.assets, "fonts/$asset")
            setTypeface(typeface)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun requestFocused() {
        setCursorPosition(text.toString().length - (preFillNumberStr.length - 1))
        showKeyboard()
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        //quaych fixed: IS-985
        if (preFillNumberZeroDigits > 0) {
            if (block) return
            block = true
            val reSelEnd = if (selEnd == 0 && money == 0L && text.toString().isNotEmpty()) 1 else selEnd
            var expectedSelEnd = text.toString().length - (preFillNumberStr.length - 1)
            if (expectedSelEnd < 0) expectedSelEnd = 0
            //val newSelStart = if (selStart > expectedSelEnd) expectedSelEnd else selStart
            val newSelEnd = if (reSelEnd > expectedSelEnd) expectedSelEnd else reSelEnd
            setCursorPosition(newSelEnd, false)
            block = false
        } else super.onSelectionChanged(selStart, selEnd)
    }

    companion object {
        private const val TAG = "MoneyEditText"
        const val DEFAULT_MONEY_UNIT = "VNĐ"
    }
}