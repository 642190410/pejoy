/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.keep2iron.pejoy.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import io.github.keep2iron.pejoy.R
import io.github.keep2iron.pejoy.internal.entity.IncapableCause
import io.github.keep2iron.pejoy.internal.entity.Item
import io.github.keep2iron.pejoy.internal.entity.SelectionSpec
import io.github.keep2iron.pejoy.internal.model.SelectedItemCollection
import io.github.keep2iron.pejoy.ui.AlbumModel
import io.github.keep2iron.pejoy.ui.AbstractPreviewActivity
import io.github.keep2iron.pejoy.ui.AlbumPreviewActivity
import io.github.keep2iron.pejoy.ui.view.CheckView
import io.github.keep2iron.pejoy.ui.view.MediaGrid
import io.github.keep2iron.pejoy.utilities.getThemeDrawable

class AlbumMediaAdapter(
    val activity: Activity,
    private val mSelectedCollection: SelectedItemCollection,
    private val mRecyclerView: RecyclerView,
    private val model: AlbumModel
) : RecyclerViewCursorAdapter(activity, null), MediaGrid.OnMediaGridClickListener {

    private val placeholder by lazy {
        getThemeDrawable(context, R.attr.pejoy_item_placeholder)
    }

    private var onCheckedViewStateChangeListener: (() -> Unit)? = null

    fun setOnCheckedViewStateChangeListener(onCheckedViewStateChangeListener: () -> Unit) {
        this.onCheckedViewStateChangeListener = onCheckedViewStateChangeListener
    }

    override fun getLayoutId(): Int = R.layout.pejoy_item_grid_album

    override fun render(holder: RecyclerView.ViewHolder, cursor: Cursor?, position: Int) {
        Log.d("keep2iron", "position : ${position}")

        val mediaGrid = holder.itemView as MediaGrid
        val context = holder.itemView.context.applicationContext

        val item = Item.valueOf(cursor!!)
        mediaGrid.preBindMedia(
            MediaGrid.PreBindInfo(
                getImageResize(context),
                placeholder,
                mSelectionSpec.countable,
                holder,
                position
            )
        )
        mediaGrid.bindMedia(item)
        setCheckStatus(item, mediaGrid)

        mediaGrid.setOnMediaGridClickListener(this)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            if (!isDataValid(cursor)) {
                throw IllegalStateException("Cannot bind view holder when cursor is in invalid state.")
            }
            if (!cursor!!.moveToPosition(position)) {
                throw IllegalStateException(
                    "Could not move cursor to position " + position
                            + " when trying to bind view holder"
                )
            }
            val gridView = holder.itemView as MediaGrid
            gridView.setOnMediaGridClickListener(this)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemViewType(position: Int, cursor: Cursor?): Int {
        return 0
    }

    private val mSelectionSpec: SelectionSpec = SelectionSpec.instance
    private var mImageResize: Int = 0

    private fun setCheckStatus(item: Item, mediaGrid: MediaGrid) {
        if (mSelectionSpec.countable) {
            val checkedNum = mSelectedCollection.checkedNumOf(item)
            if (checkedNum > 0) {
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setCheckedNum(checkedNum)
            } else {
                if (mSelectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false)
                    mediaGrid.setCheckedNum(CheckView.UNCHECKED)
                } else {
                    mediaGrid.setCheckEnabled(true)
                    mediaGrid.setCheckedNum(checkedNum)
                }
            }
        } else {
            val selected = mSelectedCollection.isSelected(item)
            if (selected) {
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setChecked(true)
            } else {
                if (mSelectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false)
                    mediaGrid.setChecked(false)
                } else {
                    mediaGrid.setCheckEnabled(true)
                    mediaGrid.setChecked(false)
                }
            }
        }
    }

    override fun onCheckViewClicked(checkView: CheckView, item: Item, holder: RecyclerView.ViewHolder, position: Int) {
        val gridView = holder.itemView as MediaGrid

        val selected = model.selectedItemCollection.isSelected(item)
        if (selected) {
            model.selectedItemCollection.remove(item)
            onCheckedViewStateChangeListener?.invoke()
            refreshSelection()
        } else {
            if (!model.selectedItemCollection.maxSelectableReached() && assertAddSelection(context, item)) {
                model.selectedItemCollection.add(item)
                onCheckedViewStateChangeListener?.invoke()
                refreshSelection()
            }
        }

        setCheckStatus(item, gridView)
    }

    override fun onThumbnailClicked(thumbnail: View, item: Item, holder: RecyclerView.ViewHolder, position: Int) {
        val intent = Intent(context, AlbumPreviewActivity::class.java)
        intent.putExtra(AbstractPreviewActivity.EXTRA_BUNDLE_ITEMS, model.selectedItemCollection.dataWithBundle)
        intent.putExtra(AbstractPreviewActivity.EXTRA_BOOLEAN_ORIGIN_ENABLE, model.originEnabled)
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item)
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, model.currentShowAlbum())
        activity.startActivityForResult(intent, AbstractPreviewActivity.REQUEST_CODE)
    }

    private fun assertAddSelection(context: Context, item: Item): Boolean {
        val cause = mSelectedCollection.isAcceptable(item)
        IncapableCause.handleCause(context, cause)
        return cause == null
    }

    private fun refreshSelection() {
        notifyDataSetChanged()
    }

    private fun getImageResize(context: Context): Int {
        if (mImageResize == 0) {
            val gridLayoutManager = mRecyclerView.layoutManager as GridLayoutManager
            val spanCount = gridLayoutManager.spanCount
            val screenWidth = context.resources.displayMetrics.widthPixels
            val availableWidth = screenWidth - context.resources.getDimensionPixelSize(
                R.dimen.pejoy_media_grid_spacing
            ) * (spanCount - 1)
            mImageResize = availableWidth / spanCount
            mImageResize = (mImageResize * mSelectionSpec.thumbnailScale).toInt()
        }
        return mImageResize
    }


}