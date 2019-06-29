/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.keep2iron.pejoy.internal.model

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import io.github.keep2iron.pejoy.internal.loader.AlbumLoader
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference

class AlbumCollection : LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var mContext: WeakReference<Context>
    private lateinit var mLoaderManager: LoaderManager
    private var mCallbacks: AlbumCallbacks? = null
    var currentSelection: Int = 0
        private set
    private var mLoadFinished: Boolean = false

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val context = mContext.get() ?: throw  IllegalArgumentException("context is null")
        mLoadFinished = false
        return AlbumLoader.newInstance(context)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mContext.get() ?: return

        if (!mLoadFinished) {
            mLoadFinished = true
            mCallbacks!!.onAlbumLoad(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mContext.get() ?: return

        mCallbacks!!.onAlbumReset()
    }

    fun onCreate(activity: FragmentActivity, callbacks: AlbumCallbacks) {
        mContext = WeakReference(activity)
        mLoaderManager = activity.supportLoaderManager
        mCallbacks = callbacks
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            return
        }

        currentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_CURRENT_SELECTION, currentSelection)
    }

    fun onDestroy() {
        mLoaderManager.destroyLoader(LOADER_ID)
        mCallbacks = null
    }

    fun loadAlbums() {
        mLoaderManager.initLoader(LOADER_ID, null, this)
    }

    fun setStateCurrentSelection(currentSelection: Int) {
        this.currentSelection = currentSelection
    }

    interface AlbumCallbacks {
        fun onAlbumLoad(cursor: Cursor)

        fun onAlbumReset()
    }

    companion object {
        private const val LOADER_ID = 1
        private const val STATE_CURRENT_SELECTION = "state_current_selection"
    }
}