package com.josephuszhou.wudaozi.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.josephuszhou.wudaozi.R
import com.josephuszhou.wudaozi.WuDaozi
import com.josephuszhou.wudaozi.adapter.PhotoAdapter
import com.josephuszhou.wudaozi.config.Config
import com.josephuszhou.wudaozi.data.PhotoData
import com.josephuszhou.wudaozi.data.SelectedData
import com.josephuszhou.wudaozi.entity.PhotoEntity
import com.josephuszhou.wudaozi.widget.AlbumSpinner
import com.josephuszhou.wudaozi.widget.PhotoGridView
import kotlinx.android.synthetic.main.activity_wu_daozi.*

class WuDaoziActivity : AppCompatActivity(), View.OnClickListener,
    AlbumSpinner.OnItemSelectedListener, PhotoAdapter.OnCheckStateListener,
    PhotoAdapter.OnThumbnailClickListener, PhotoData.OnLoadListener {

    private lateinit var mAlbumSpinner: AlbumSpinner

    private lateinit var mPhotoGridView: PhotoGridView

    private lateinit var mPhotoData: PhotoData

    companion object {
        fun start(activity: Activity, requestCode: Int) {
            activity.startActivityForResult(
                Intent(activity, WuDaoziActivity::class.java),
                requestCode
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Config.getInstance().mThemeId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wu_daozi)

        // init toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.toolbar_back_icon, typedValue, true)
        if (typedValue.resourceId > 0) {
            toolbar.setNavigationIcon(typedValue.resourceId)
        }

        tv_sure.setOnClickListener(this)

        // init spinner
        mAlbumSpinner = AlbumSpinner(this).apply {
            setAnchorView(tv_album)
            setSelectedTextView(tv_album)
            setOnItemSelectedListener(this@WuDaoziActivity)
        }

        // init mPhotoGridView
        mPhotoGridView = PhotoGridView(this, recyclerview).apply {
            setOnCheckStateListener(this@WuDaoziActivity)
            setOnThumbnailClickListener(this@WuDaoziActivity)
        }

        mPhotoData = PhotoData(this).apply {
            setOnLoadListener(this@WuDaoziActivity)
            load()
        }
    }

    override fun onResume() {
        super.onResume()
        onCheckStateChanged()
    }

    override fun onDestroy() {
        mPhotoData.destory()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_sure -> {
                setResult()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onLoaded() {
        mAlbumSpinner.setData(mPhotoData.getAlbumList())
        val albumIndex = mPhotoData.getCurrentAlbumIndex()
        mAlbumSpinner.setSelected(albumIndex)
        onItemSelected(albumIndex)
    }

    override fun onItemSelected(position: Int) {
        mPhotoData.setCurrentAlbum(position)
        mPhotoGridView.setData(mPhotoData.getPhotoList(mPhotoData.getCurrentAlbum()))
    }

    override fun onCheckStateChanged() {
        val selectedCount = SelectedData.getInstance().selectedCount()
        tv_sure.isEnabled = selectedCount > 0
        tv_sure.text = if (selectedCount > 0) {
            getString(R.string.wudaozi_sure_with_num, selectedCount)
        } else {
            getString(R.string.wudaozi_sure)
        }
    }

    override fun onThumbnailClick(photoEntity: PhotoEntity) {
        PreviewActivity.start(
            this,
            PreviewActivity.REQUEST_CODE_PREVIEW,
            mPhotoData.getCurrentAlbum(),
            photoEntity
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PreviewActivity.REQUEST_CODE_PREVIEW && resultCode == Activity.RESULT_OK) {
            setResult()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setResult() {
        val uriList = SelectedData.getInstance().getUriList()
        val bundle = Bundle().apply {
            putParcelableArrayList(WuDaozi.BUNDLE_KEY, uriList)
        }
        val intent = Intent().apply {
            putExtras(bundle)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
