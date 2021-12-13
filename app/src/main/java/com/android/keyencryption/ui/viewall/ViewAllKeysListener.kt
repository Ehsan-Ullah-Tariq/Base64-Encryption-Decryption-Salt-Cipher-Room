package com.android.keyencryption.ui.viewall

import com.android.keyencryption.database.DataTable

interface ViewAllKeysListener {
    fun onDeleteClickListener(position: Int, item: DataTable)
}