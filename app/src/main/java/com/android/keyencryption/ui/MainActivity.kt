package com.android.keyencryption.ui

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.keyencryption.R
import com.android.keyencryption.database.DataBase
import com.android.keyencryption.database.DataTable
import com.android.keyencryption.databinding.ActivityMainBinding
import com.android.keyencryption.ui.viewall.ViewAllKeysActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    /*
     Here we will use dataBinding... why? because it is very useful to work with design and code at a time
     like we don't need to assign layout ids to variables instead we will just call them directly
     best advantage of binding is we can apply click listeners on textViews also so no need of buttons
     we will initialize it later
     */
    private lateinit var binding: ActivityMainBinding

    //This is our generatedKey which will be random everytime we will initialize it later
    private var generatedKey: String? = null


    //Companion object is used for saving constants so out salt and iv both are constants which can be used everywhere in project
    companion object {


        /*
         Salt (and an "iteration count") is used to derive a key from the password.
         The salt and iteration count used for key derivation do not have to be secret.
         The salt should be unpredictable, however, and is best chosen randomly.
         lets say our text which has to be encrypted is a password
         Passwords aren't selected randomly; some passwords are much more likely than others.
         Therefore, rather than generating all possible passwords of a given length (exhaustive brute-force search), attackers maintain a list of passwords, ordered by decreasing probability.
          Deriving an encryption key from a password is relatively slow (due to the iteration of the key derivation algorithm).
         Deriving keys for a few million passwords could take months.
         This would motivate an attacker to derive the keys from his most-likely-password list once, and store the results.
         With such a list, he can quickly try to decrypt with each key in his list, rather than spending months of compute time to derive keys again.
         However, each bit of salt doubles the space required to store the derived key, and the time it takes to derive keys for each of his likely passwords.
          A few bytes of salt, and it quickly becomes infeasible to create and store such a list.
         Salt is necessary to prevent pre-computation attacks.
         */
        const val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu

        /*
        An IV (or nonce with counter modes) makes the same plain text produce different cipher texts.
         The prevents an attacker from exploiting patterns in the plain text to garner information from a set of encrypted messages.
        An initialization vector is necessary to hide patterns in messages.
         */
        const val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX

        //One serves to enhance the security of the key, the other enhances the security of each message encrypted with that key. Both are necessary together.
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //here we are initializing binding with DataBindingUtil which is built in
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        //This is how we call layout IDs directly with the help of binding and this is textView which we are using as button textViews are easy to give shapes
        binding.generateKeyBtn.setOnClickListener {

            //UUID is built-in feature which provides random ids with the help of randomUIID so we will generate random unique keys with this
            //subString is used to limit length of string so we have set limit of 10 characters limit
            generatedKey = UUID.randomUUID().toString().substring(0, 10)

            //Here we are assigning generatedKey to our view where we are showing generated key
            //I've used edit text so that user can copy his generated key in text view user cannot copy
            binding.viewGeneratedKey.setText(generatedKey)

        }


        //Here we are setting click listener on encryption button
        binding.encryptTextBtn.setOnClickListener {
            //first we are checking if our text is empty ot not otherwise without this check application will crash on null text
            if (!binding.etTextForEncrypt.text.isNullOrEmpty()) {
                //we have created method encrypt and passing our text in it
                encrypt(binding.etTextForEncrypt.text.toString())

            } else {
                binding.etTextForEncrypt.error = "Required"
            }
        }

        //Here we are setting click listener on decrypt button
        binding.decryptTextBtn.setOnClickListener {
            if (!binding.viewEncryptedText.text.isNullOrEmpty()) {
                decrypt(binding.viewEncryptedText.text.toString())
            } else {
                binding.viewEncryptedText.error = "Required"
            }
        }

        //Here we are inserting our data (key and text) in database
        binding.saveBtn.setOnClickListener {
            val generatedKey = binding.viewGeneratedKey.text.toString()
            val textToEncrypt = binding.etTextForEncrypt.text.toString()

            if (generatedKey.isNotEmpty() && textToEncrypt.isNotEmpty()) {
                val cardDao = DataBase.getDatabase(this).dataDao()
                //We have used kotlin Coroutine because database call should be on differed thread
                //DAO is used to perform operations in our database on our tables it is DATA ACCESS OBJECT
                GlobalScope.launch {
                    cardDao.insertData(
                        DataTable(
                            generatedKey = generatedKey,
                            textToEncrypt = textToEncrypt
                        )
                    )
                }
            } else {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            }
        }

        //Going to listing screen with this click listener
        binding.viewList.setOnClickListener {
            val intent = Intent(this, ViewAllKeysActivity::class.java)
            startActivity(intent)
            binding.etTextForEncrypt.text = null
            binding.viewEncryptedText.text = null
            binding.viewGeneratedKey.text = null
            binding.viewDecryptedText.text = null
        }
    }

    private fun encrypt(strToEncrypt: String) {
        try {
            /*
             This class specifies an initialization vector (IV).
             Examples which use IVs are ciphers in feedback mode, e.g., DES in CBC mode and RSA ciphers with OAEP encoding operation.
             */
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            /*
            SecretKeyFactory is algorithm-independent and provider-based,
            so you must obtain a SecretKeyFactory object by calling one of the static getInstance( )
            factory methods and specifying the name of the desired secret-key algorithm and, optionally,
             the name of the provider whose implementation is desired.
             */
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

            /*
            public class PBEKeySpec extends Object implements KeySpec.
             A user-chosen password that can be used with password-based encryption (PBE).
             The password can be viewed as some kind of raw key material, from which the encryption mechanism that uses it derives a cryptographic key.
             We will pass our generatedKey here as a secret key
             */
            val spec =
                PBEKeySpec(
                    generatedKey?.toCharArray(),
                    Base64.decode(salt, Base64.DEFAULT),
                    10000,
                    256
                )
            val tmp = factory.generateSecret(spec)
            //So here our encoded secret key will be generated
            //The Advanced Encryption Standard (AES) is a symmetric block cipher chosen by the U.S. government to protect classified information.
            val secretKey = SecretKeySpec(tmp.encoded, "AES")


            /*Cipher is our main algorithm for encrypting decrypting data which need AES
            The padding is done before encryption. It ensures that what will be given to the encryption algorithm can be split into an integral number of blocks (16 bytes per block with AES).
            AES encrypts the whole, data and padding, and encrypted data "looks random".
            After decryption, the original contents of the padding are revealed,
            and will exhibit the proper structure (namely that the extra bytes all have value n if n extra bytes were added).
             */
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            //cipher needs to be initialized with a mode which is encryption here and secret key and ivParameterSpec
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

            //So final thing is cipher.dofinal() here we have to convert out text as byteArray and pass into cipher.dofinal's constructor
            binding.viewEncryptedText.setText(
                Base64.encodeToString(
                    cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)),
                    Base64.DEFAULT
                )
            )
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
    }


    /*
    All things are same as encryption method we just need to pass DECRYPT_MODE in cipher.init to decrypt our text
    also in cipher.doFinal() Base64.decode values should be pass
     */
    private fun decrypt(strToDecrypt: String) {
        try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =
                PBEKeySpec(
                    generatedKey?.toCharArray(),
                    Base64.decode(salt, Base64.DEFAULT),
                    10000,
                    256
                )
            val tmp = factory.generateSecret(spec);
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            binding.viewDecryptedText.setText(
                String(
                    cipher.doFinal(
                        Base64.decode(
                            strToDecrypt,
                            Base64.DEFAULT
                        )
                    )
                )
            )

        } catch (e: Exception) {
            println("Error while decrypting: $e");
        }

    }


}