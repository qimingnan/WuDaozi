package com.josephuszhou.wudaozi.widget

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.widget.ListPopupWindow
import android.widget.TextView
import com.josephuszhou.wudaozi.R
import com.josephuszhou.wudaozi.adapter.AlbumAdapter
import com.josephuszhou.wudaozi.entity.AlbumEntity
import com.josephuszhou.wudaozi.util.AttrUtil

class AlbumSpinner(context: Context) {

    companion object {
        const val MAX_SHOW_COUNT = 6
    }

    private var mListPopupWindow: ListPopupWindow = ListPopupWindow(context, null)

    private var mAlbumAdapter: AlbumAdapter? = null
    private var mOnItemSelectedListener: OnItemSelectedListener? = null
    private var mSelectedTextView: TextView? = null

    init {
        mListPopupWindow.isModal = true
        mListPopupWindow.setContentWidth(context.resources.getDimensionPixelSize(R.dimen.wudaozi_spinner_width))
        mListPopupWindow.setOnItemClickListener { _, _, position, _ ->
            onselected(position)
            mOnItemSelectedListener?.onItemSelected(position)
        }
    }

    private fun onselected(position: Int) {
        mListPopupWindow.dismiss()
        val albumEntity = mAlbumAdapter?.getItem(position) as AlbumEntity
        mSelectedTextView?.text = albumEntity.bucketName
    }

    fun setSelected(position: Int) {
        mListPopupWindow.setSelection(position)
        onselected(position)
    }

    fun setAlbumAdapter(adapter: AlbumAdapter) {
        mAlbumAdapter = adapter
        mListPopupWindow.setAdapter(adapter)
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        mOnItemSelectedListener = listener
    }

    fun setAnchorView(view: View) {
        mListPopupWindow.anchorView = view
    }

    fun setSelectedTextView(textView: TextView) {
        mSelectedTextView = textView

        mSelectedTextView?.let {
            // tint dropdown arrow icon
            val right = it.compoundDrawablesRelative[2]
            val color = AttrUtil.getcolor(it.context, R.attr.toolbar_album_text_color)
            right.setColorFilter(color, PorterDuff.Mode.SRC_IN)

            it.setOnClickListener {
                mAlbumAdapter?.let { it1 ->
                    val itemHeight =
                        it.resources.getDimensionPixelSize(R.dimen.wudaozi_spinner_item_height)
                    mListPopupWindow.height = if (it1.count > MAX_SHOW_COUNT) {
                        itemHeight * MAX_SHOW_COUNT
                    } else {
                        itemHeight * it1.count
                    }
                }
                mListPopupWindow.show()
            }

            it.setOnTouchListener(mListPopupWindow.createDragToOpenListener(it))
        }
    }

    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

}