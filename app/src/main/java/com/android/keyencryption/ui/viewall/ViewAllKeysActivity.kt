package com.android.keyencryption.ui.viewall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.android.keyencryption.R
import com.android.keyencryption.database.DataBase
import com.android.keyencryption.database.DataDao
import com.android.keyencryption.database.DataTable
import com.android.keyencryption.databinding.ActivityViewAllKeysBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

//We have created a ViewAllKeysListener and implemented just to handle our click listeners
// like when from adapter we have to call some methods in our activity class so we have to override it like onDeleteClickListener
//this onDeleteClickListener is used to delete data from our database
class ViewAllKeysActivity : AppCompatActivity(), ViewAllKeysListener {

    private lateinit var binding: ActivityViewAllKeysBinding

    //Adapter is necessary for our recyclerView
    private lateinit var allKeysAdapter: AllKeysAdapter

    //List to get data from database
    private var list: MutableList<DataTable>? = null
    private var allListOfKeys: MutableList<DataTable>? = null
    private lateinit var dataDao: DataDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_all_keys)
        dataDao = DataBase.getDatabase(this@ViewAllKeysActivity).dataDao()

        allKeysAdapter = AllKeysAdapter(this)
        getKeys()


        binding.backBtn.setOnClickListener {
            super.onBackPressed()
        }

    }

    //Here we are getting data from database we are using UIthread because it will show our data on UI
    private fun getKeys() {
        runOnUiThread {
            GlobalScope.launch {
                allListOfKeys = dataDao.getAllData().toMutableList()
                allListOfKeys?.forEach {
                    list?.add(it)
                }
                allKeysAdapter.submitList(allListOfKeys)
                setupRecyclerView()
            }
        }
    }


    //Here we are setting our recyclerView
    private fun setupRecyclerView() {
        runOnUiThread {
            binding.rvKeys.apply {
                adapter = allKeysAdapter
            }
        }
    }

    //Here we have override our onDeleteClickListener which will be called from adapter through interface it will delete our specific data from database
    override fun onDeleteClickListener(position: Int, item: DataTable) {
        runOnUiThread {
            MainScope().launch {
                dataDao.deleteCard(item.id)
                removeView(position, item)
            }
        }
    }

    private fun removeView(position: Int, item: DataTable) {

        allListOfKeys?.remove(item)
        binding.rvKeys.removeViewAt(position)
        allKeysAdapter.notifyItemRemoved(position)
        allKeysAdapter.notifyItemRangeChanged(position, allListOfKeys?.size!!)
        allKeysAdapter.notifyDataSetChanged()

    }
}