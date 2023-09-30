package com.elogictics.taskmaster.common.widgets.spinner

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.widget.PopupWindowCompat
import androidx.recyclerview.widget.RecyclerView
import com.elogictics.taskmaster.R
import dagger.hilt.android.internal.managers.FragmentComponentManager

open class LocationSpinner<T : LocationData> : androidx.appcompat.widget.AppCompatTextView, View.OnClickListener {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    var mItemSelectedId = -1

    private var mOnItemSelectedListener: OnItemSelectedListener<T>? = null
    private val mData: MutableList<ItemViewLocation<T>> = mutableListOf()
    private var mAdapter: LocationSpinnerDPLAdapter<T>? = null
    private var mPopupWindow: SpinnerPopupWindow? = null
    private var mInitialized = false
    private var provinceName: String = ""

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPopupWindow?.dismissRightAway()
    }
    fun updateDataProvince(provinceName: String){
        this.provinceName = provinceName
    }
    fun showPopup(v: View?, init: Boolean, heightQuestion: Int){
        mPopupWindow?.dismissRightAway()
        val layoutInflater = LayoutInflater.from(context)
        val popupView = layoutInflater.inflate(R.layout.view_popup_spinner, null, false)
        val rcvItem = popupView.findViewById<RecyclerView>(R.id.rcv_location)
        val cvContainer = popupView.findViewById<CardView>(R.id.cv_container)
        val searchView = popupView.findViewById<SearchView>(R.id.search_view_province_dpl)

        cvContainer.pivotY = 0f
        cvContainer.scaleY = 0f
        cvContainer.animate().scaleY(1f).setDuration(150).start()
        mPopupWindow = SpinnerPopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
            .WRAP_CONTENT, true).also {
            it.isOutsideTouchable = false
            it.isTouchable = true
            it.setBackgroundDrawable(ColorDrawable(0))
        }
        rcvItem.adapter = mAdapter
        if (Build.VERSION.SDK_INT >= 24) {
            //getLocationInWindow required array of size 2
//            val a = IntArray(2)
//            v!!.getLocationInWindow(a)
//            mPopupWindow!!.showAtLocation((context as Activity).window.decorView, Gravity.NO_GRAVITY, 0, a[1] + v.height)
            val rectf = Rect()
            v!!.getGlobalVisibleRect(rectf)
            var offsetY = (rectf.top + v.height)
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val screenHeight = wm.defaultDisplay.height
            mPopupWindow!!.height = (screenHeight - offsetY)*3/4
            mPopupWindow!!.showAsDropDown(v)
        } else {
            PopupWindowCompat.showAsDropDown(mPopupWindow!!, v!!, 0, 0, Gravity.TOP)
        }
        val item = mData.find { provinceName.equals(it.data?.name, true) }
        item?.data?.id?.let {
            rcvItem.scrollToPosition(setItemSelectedFirst(it))
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mAdapter?.getFilter()?.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mAdapter?.getFilter()?.filter(newText)
                return false
            }
        })

       // searchView.queryHint = "Tìm kiếm loại"

        searchView.setOnQueryTextFocusChangeListener { view, queryTextFocused ->
            if(!queryTextFocused) {
//                hideKeyboard()
                searchView.setQuery("", false)
            }
        }
    }

    fun hideKeyboard(){
        try {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setItemSelectedFirst(idSelected: Int): Int {
        mAdapter?.idSelected = idSelected
        //notify data changed
        mAdapter?.notifyDataSetChanged()
        val item = findItemById(idSelected, mData)
        val position = mData.indexOf(item)
//        mOnItemSelectedListener?.onItemSelected(this, position, item)
        return position
    }

    override fun onClick(v: View?) {
        if (mData.isNullOrEmpty()) return
        showPopup(v, false, 0)
    }

    fun setData(data: List<ItemViewLocation<T>>) {
        mData.clear()
        mData.addAll(data)
    }

    fun setItemSelected(idSelected: Int): Int {
        mAdapter?.idSelected = idSelected
        //notify data changed
        mAdapter?.notifyDataSetChanged()
        val item = findItemById(idSelected, mData)
        item?.data?.name?.let {
            provinceName = it
        }
        this@LocationSpinner.text = item?.data?.name ?: ""
        val position = mData.indexOf(item)
        mOnItemSelectedListener?.onItemSelected(this, position, item)
        return position
    }

    fun setCurrentSelection(text: String?): Int {
        val item = mData.find { text?.equals(it.data?.name, true) == true }
        return setItemSelected(item?.data?.id ?: -1)
    }

    fun getItem(position: Int): ItemViewLocation<T>? {
        if (position < 0) return null
        return mData[position]
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener<T>?) {
        mOnItemSelectedListener = onItemSelectedListener
    }

    fun reset() {
        text = ""
        setItemSelected(-1)
    }

    fun updateProvinceDataDPL(idProvinceDPL: Int, nameProvinceDPL: String) {
        text = nameProvinceDPL
        setItemSelected(idProvinceDPL)
    }

    private fun init(context: Context) {
        if (mInitialized)
            return
        mInitialized = true
        mAdapter = LocationSpinnerDPLAdapter(
            FragmentComponentManager.findActivity(context) as Activity, mData,
            object : LocationSpinnerDPLAdapter.OnItemLocationSpinnerClickedListener<T> {
                override fun onLocationSpinnerClicked(data: ItemViewLocation<T>, position: Int) {
                    setItemSelected(data.data?.id ?: 0)
                    if (data.data?.id != 0) {
                        val item = data.data?.id?.let { findItemById(it, mData) }
                        val mPosition = mData.indexOf(item)
                        mOnItemSelectedListener?.onItemSelected(this@LocationSpinner, mPosition, data)
                    }  else {
                        mOnItemSelectedListener?.onItemSelected(
                            this@LocationSpinner,
                            position,
                            data
                        )
                    }
                    mPopupWindow?.dismissRightAway()
                }

            })
        mAdapter?.updateDate(mData)
        setOnClickListener(this)
    }

    private fun <T : LocationData> findItemById(id: Int, data: List<ItemViewLocation<T>>): ItemViewLocation<T>? {
        return data.find<ItemViewLocation<T>> { item -> item.data?.id == id }
    }

    interface OnItemSelectedListener<T : LocationData> {
        fun onItemSelected(parent: LocationSpinner<T>, position: Int, item: ItemViewLocation<T>?)
    }
}