/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 skydoves
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.elogictics.taskmaster.common.widgets.elasticviews

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import androidx.cardview.widget.CardView
import com.elogictics.taskmaster.R

@Suppress("unused")
class ElasticCardView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = androidx.cardview.R.attr.cardViewStyle
) : CardView(context, attrs, defStyle), ElasticInterface {

  /** The target elastic scale size of the animation. */
  var scale = Definitions.DEFAULT_SCALE

  /** The default duration of the animation. */
  var duration = Definitions.DEFAULT_DURATION

  private var onUserClickListener: OnClickListener? = null
  private var onFinishListener: ElasticFinishListener? = null

  init {
    onCreate()
    when {
      attrs != null && defStyle != androidx.cardview.R.attr.cardViewStyle ->
        getAttrs(attrs, defStyle)
      attrs != null -> getAttrs(attrs)
    }
  }

  private fun onCreate() {
    super.setOnClickListener {
      elasticAnimation(this) {
        setDuration(this@ElasticCardView.duration)
        setScaleX(this@ElasticCardView.scale)
        setScaleY(this@ElasticCardView.scale)
        setOnFinishListener { invokeListeners() }
      }.doAction()
    }
  }

  private fun getAttrs(attrs: AttributeSet) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticCardView)
    try {
      setTypeArray(typedArray)
    } finally {
      typedArray.recycle()
    }
  }

  private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticCardView, defStyle, 0)
    try {
      setTypeArray(typedArray)
    } finally {
      typedArray.recycle()
    }
  }

  private fun setTypeArray(typedArray: TypedArray) {
    this.scale = typedArray.getFloat(R.styleable.ElasticCardView_cardView_scale, this.scale)
    this.duration = typedArray.getInt(R.styleable.ElasticCardView_cardView_duration, this.duration)
  }

  override fun setOnClickListener(listener: OnClickListener?) {
    this.onUserClickListener = listener
  }

  override fun setOnFinishListener(listener: ElasticFinishListener?) {
    this.onFinishListener = listener
  }

  override fun setOnClickListener(block: (View) -> Unit) =
    setOnClickListener(OnClickListener(block))

  override fun setOnFinishListener(block: () -> Unit) =
    setOnFinishListener(ElasticFinishListener(block))

  private fun invokeListeners() {
    this.onUserClickListener?.onClick(this)
    this.onFinishListener?.onFinished()
  }

  /** Builder class for creating [ElasticCardView]. */
  class Builder(context: Context) {
    private val elasticCardView = ElasticCardView(context)

    fun setScale(value: Float) = apply { this.elasticCardView.scale = value }
    fun setDuration(value: Int) = apply { this.elasticCardView.duration = value }

    @JvmSynthetic
    fun setOnClickListener(block: (View) -> Unit) = apply {
      setOnClickListener(OnClickListener(block))
    }

    fun setOnClickListener(value: OnClickListener) = apply {
      this.elasticCardView.setOnClickListener(value)
    }

    @JvmSynthetic
    fun setOnFinishListener(block: () -> Unit) = apply {
      setOnFinishListener(ElasticFinishListener(block))
    }

    fun setOnFinishListener(value: ElasticFinishListener) = apply {
      this.elasticCardView.setOnFinishListener(value)
    }

    fun build() = this.elasticCardView
  }
}
