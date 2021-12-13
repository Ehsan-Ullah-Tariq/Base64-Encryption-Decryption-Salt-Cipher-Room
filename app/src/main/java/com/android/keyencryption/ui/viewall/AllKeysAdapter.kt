package com.android.keyencryption.ui.viewall

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.keyencryption.R
import com.android.keyencryption.database.DataTable
import com.android.keyencryption.databinding.ViewAllKeysLayoutBinding
import com.android.keyencryption.ui.MainActivity
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class AllKeysAdapter(private val listener: ViewAllKeysListener) :
    ListAdapter<DataTable, AllKeysAdapter.ViewAllKeysViewHolder>(Companion) {

    class ViewAllKeysViewHolder(val itemBinding: ViewAllKeysLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    companion object : DiffUtil.ItemCallback<DataTable>() {
        override fun areItemsTheSame(oldItem: DataTable, newItem: DataTable): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DataTable, newItem: DataTable): Boolean =
            oldItem == newItem
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewAllKeysViewHolder {
        return ViewAllKeysViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.view_all_keys_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewAllKeysViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.item = item
        holder.itemBinding.executePendingBindings()


        holder.itemBinding.removeBtn.setOnClickListener {
            listener.onDeleteClickListener(position, item!!)
        }

        holder.itemBinding.encryptTextBtn.setOnClickListener {
            try {
                val ivParameterSpec = IvParameterSpec(Base64.decode(MainActivity.iv, Base64.DEFAULT))
                val generatedKey = holder.itemBinding.tvGeneratedKey.text.toString()
                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val spec =
                    PBEKeySpec(generatedKey.toCharArray(), Base64.decode(MainActivity.salt, Base64.DEFAULT), 10000, 256)
                val tmp = factory.generateSecret(spec)
                val secretKey = SecretKeySpec(tmp.encoded, "AES")

                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
                holder.itemBinding.tvEncryptedText.text = Base64.encodeToString(
                    cipher.doFinal(holder.itemBinding.tvTextToEncrypt.text.toString().toByteArray(Charsets.UTF_8)),
                    Base64.DEFAULT
                )

                if (!holder.itemBinding.tvEncryptedText.text.isNullOrEmpty()){
                    holder.itemBinding.decryptTextBtn.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                println("Error while encrypting: $e")
            }
        }

        holder.itemBinding.decryptTextBtn.setOnClickListener {
            try {
                val generatedKey = holder.itemBinding.tvGeneratedKey.text.toString()
                val ivParameterSpec = IvParameterSpec(Base64.decode(MainActivity.iv, Base64.DEFAULT))

                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val spec =
                    PBEKeySpec(generatedKey.toCharArray(), Base64.decode(MainActivity.salt, Base64.DEFAULT), 10000, 256)
                val tmp = factory.generateSecret(spec);
                val secretKey = SecretKeySpec(tmp.encoded, "AES")

                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
                holder.itemBinding.tvDecryptedText.text = String(
                    cipher.doFinal(
                        Base64.decode(
                            holder.itemBinding.tvEncryptedText.text.toString(),
                            Base64.DEFAULT
                        )
                    )
                )
            } catch (e: Exception) {
                println("Error while decrypting: $e");
            }
        }

    }





}