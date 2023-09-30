package com.elogictics.taskmaster.common.widgets.spinner

import android.content.Context
import android.util.AttributeSet

class ProvinceSpinner : LocationSpinner<ProvinceData> {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}