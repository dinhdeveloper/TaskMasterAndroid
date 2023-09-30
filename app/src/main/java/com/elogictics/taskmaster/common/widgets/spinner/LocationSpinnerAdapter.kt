package com.elogictics.taskmaster.common.widgets.spinner

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.elogictics.taskmaster.databinding.ItemSpinnerAdapterBinding
import com.elogictics.taskmaster.utils.AndroidUtils
import java.text.Collator
import java.util.Locale


class LocationSpinnerDPLAdapter<T : LocationData>(mContext: Context, private var data: MutableList<ItemViewLocation<T>>,
                                                  private var mListener: OnItemLocationSpinnerClickedListener<T>)
    : RecyclerView.Adapter<LocationSpinnerDPLViewHolder<T>>() {

    private var layoutInflater: LayoutInflater = (mContext as Activity).layoutInflater

    private var dataFilter: MutableList<ItemViewLocation<T>> = mutableListOf()
    private var mData: MutableList<ItemViewLocation<T>> = mutableListOf()

    var idSelected = -1

    override fun getItemCount(): Int = dataFilter.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationSpinnerDPLViewHolder<T> {

        val inflater = LayoutInflater.from(parent.context)
        //val viewBinding: ItemSpinnerAdapterBinding = DataBindingUtil.inflate(inflater, R.layout.item_spinner_adapter, parent, false)
        val viewBinding = ItemSpinnerAdapterBinding.inflate(layoutInflater, null, false)
        val viewHolder = LocationSpinnerDPLViewHolder<T>(viewBinding, dataFilter.size)
        viewBinding.root.setOnClickListener {
            mListener.onLocationSpinnerClicked(dataFilter[viewHolder.adapterPosition], viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: LocationSpinnerDPLViewHolder<T>, position: Int) {
        holder.bindData(dataFilter[position], idSelected = idSelected)
    }

    fun getItem(position: Int): ItemViewLocation<T>? {
        return dataFilter[position]
    }

    fun updateDate(data: MutableList<ItemViewLocation<T>>) {
        this.dataFilter = data
    }

    fun clear() {
        data.clear()
    }

    fun addAll(data: List<ItemViewLocation<T>>) {
        clear()
        this.data.addAll(data)
    }

    interface OnItemLocationSpinnerClickedListener<T : LocationData> {
        fun onLocationSpinnerClicked(data: ItemViewLocation<T>, position: Int)
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults? {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    dataFilter = data
                } else {
                    val filteredList: MutableList<ItemViewLocation<T>> = ArrayList()
                    for (row in data) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        row.data?.name?.lowercase(Locale.ROOT)?.let {
                            if (it.contains(charString.lowercase(Locale.getDefault()))) {
                                filteredList.add(row)
                            }
                        }
                    }
                    dataFilter = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = dataFilter
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults
            ) {
                dataFilter = filterResults.values as ArrayList<ItemViewLocation<T>>

                // refresh the list with filtered data
                notifyDataSetChanged()
            }
        }
    }

    /**
     * The method compare() returns Returns an integer value.
     * Value is less than zero if source is less than target,
     * value is zero if source and target are equal,
     * value is greater than zero if source is greater than target.
     */
    private fun contains(source: String, target: String): Boolean {
//        if (target.length > source.length) {
//            return false
//        }
        val collator = Collator.getInstance(Locale("vi", "VN"))
        collator.strength = Collator.NO_DECOMPOSITION
//        val end = source.length - target.length + 1
//        for (i in 0 until end) {
//            val sourceSubstring = source.substring(i, i + target.length)
        if (collator.compare(source, target) == 0 ) {
            return true
        }
        return false
    }
}

class LocationSpinnerDPLViewHolder<T : LocationData>(private val viewBinding: ItemSpinnerAdapterBinding,
                                                     private val itemCount: Int)
    : RecyclerView.ViewHolder(viewBinding.root) {

    fun bindData(itemData: ItemViewLocation<T>?, idSelected: Int) {
        viewBinding.let { binding ->
            itemData?.let {
                binding.data = it.data?.name ?: ""
                it.isSelected = idSelected == it.data?.id ?: -1
                binding.isLastItem = adapterPosition == itemCount - 1 || it.data == null
                binding.isSelected = it.isSelected
                var layoutParams = viewBinding.root.layoutParams
                if (layoutParams == null) {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        AndroidUtils.convertDpToPx(viewBinding.root.context, 48f))
                }
                //set height = 1 to place holder item
                if (adapterPosition == 0 && it.data?.name == null) {
                    layoutParams.height = AndroidUtils.convertDpToPx(viewBinding.root.context, 1f)
                    viewBinding.root.layoutParams = layoutParams
                    if (it.isSelected){
                        binding.isSelected = false
                    }
                }
                // set height to layoutParams real item 1 time
                else if (layoutParams.height == AndroidUtils.convertDpToPx(viewBinding.root.context, 1f)) {
                    layoutParams.height = AndroidUtils.convertDpToPx(viewBinding.root.context, 48f)
                    viewBinding.root.layoutParams = layoutParams
                }
            }
        }
    }
}