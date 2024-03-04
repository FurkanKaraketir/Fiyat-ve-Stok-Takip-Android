@file:Suppress("DEPRECATION")

package com.karaketir.fiyatvestoktakip.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karaketir.fiyatvestoktakip.databinding.ActivityEditProductBinding
import java.io.ByteArrayOutputStream
import java.util.Calendar

class EditProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProductBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val cameraPermissionCode = 100
    private val storagePermissionCode = 101
    private lateinit var productName: String
    private lateinit var productGetPrice: String
    private lateinit var productSellPrice: String
    private lateinit var productStock: String
    private lateinit var productDescription: String
    private lateinit var documentID: String
    private lateinit var productImageLink: String
    private lateinit var productSellerName: String
    private lateinit var productSellerNumber: String
    private lateinit var productBarcode: String

    private lateinit var addProductButton: Button
    private lateinit var productNameEditText: EditText
    private lateinit var productSellPriceEditText: EditText
    private lateinit var productGetPriceEditText: EditText
    private lateinit var productStockEditText: EditText
    private lateinit var productDescriptionEditText: EditText
    private lateinit var productSellerNameEditText: EditText
    private lateinit var productSellerNumberEditText: EditText
    private lateinit var productBarcodeEditText: EditText

    private lateinit var addProductImageButton: Button
    private lateinit var productImageView: ImageView
    private lateinit var deleteProductButton: Button
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    private val requestImageCapture = 1
    private val requestImagePick = 2
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        documentID = intent.getStringExtra("documentID").toString()
        productName = intent.getStringExtra("name").toString()
        productGetPrice = intent.getStringExtra("buyPrice").toString()
        productSellPrice = intent.getStringExtra("sellPrice").toString()
        productStock = intent.getStringExtra("stock").toString()
        productDescription = intent.getStringExtra("description").toString()
        productImageLink = intent.getStringExtra("imageLink").toString()
        productSellerName = intent.getStringExtra("sellerName").toString()
        productSellerNumber = intent.getStringExtra("sellerNumber").toString()
        productBarcode = intent.getStringExtra("barcode").toString()


        productNameEditText = binding.productNameEditText
        productSellPriceEditText = binding.productSellPriceEditText
        productGetPriceEditText = binding.productGetPriceEditText
        productStockEditText = binding.productStockEditText
        productDescriptionEditText = binding.productDescriptionEditText
        addProductButton = binding.addProductButton
        productImageView = binding.productImageView
        addProductImageButton = binding.addProductImageButton
        deleteProductButton = binding.deleteProductButton
        productSellerNameEditText = binding.productSellerNameEditText
        productSellerNumberEditText = binding.productSellerNumberEditText
        productBarcodeEditText = binding.productBarcodeEditText

        productNameEditText.setText(productName)
        productSellPriceEditText.setText(productSellPrice)
        productGetPriceEditText.setText(productGetPrice)
        productStockEditText.setText(productStock)
        productDescriptionEditText.setText(productDescription)
        productSellerNameEditText.setText(productSellerName)
        productSellerNumberEditText.setText(productSellerNumber)
        productBarcodeEditText.setText(productBarcode)

        deleteProductButton.setOnClickListener {
            //Delete after a confirmation dialog
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Ürünü Sil")
                .setMessage("Ürünü silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    db.collection("users").document(auth.uid!!).collection("products")
                        .document(documentID).delete().addOnSuccessListener {
                            Toast.makeText(this, "Ürün Silindi", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Ürün Silinemedi", Toast.LENGTH_SHORT).show()
                        }
                }.setNegativeButton("İptal") { _, _ -> }.setCancelable(true).show()
        }

        if (productImageLink != "") {
            Glide.with(this).load(productImageLink).into(productImageView)
        } else {
            productImageView.setImageResource(0)
        }

        addProductImageButton.setOnClickListener {
            checkAndRequestPermissions()
            openImageSourceDialog()
        }

        addProductButton.setOnClickListener {
            addProductButton.isClickable = false


            productName = productNameEditText.text.toString()
            productGetPrice = productGetPriceEditText.text.toString()
            productSellPrice = productSellPriceEditText.text.toString()
            productStock = productStockEditText.text.toString()
            productDescription = productDescriptionEditText.text.toString()
            productSellerName = productSellerNameEditText.text.toString()
            productSellerNumber = productSellerNumberEditText.text.toString()
            productBarcode = productBarcodeEditText.text.toString()

            if (productNameEditText.text.toString().isNotEmpty()) {
                if (productGetPrice.isEmpty()) {
                    productGetPrice = "0"
                }
                if (productSellPrice.isEmpty()) {
                    productSellPrice = "0"
                }
                if (productStock.isEmpty()) {
                    productStock = "0"
                }
                if (productDescription.isEmpty()) {
                    productDescription = ""
                }
                if (productSellerName.isEmpty()) {
                    productSellerName = ""
                }
                if (productSellerNumber.isEmpty()) {
                    productSellerNumber = ""
                }
                if (productBarcode.isEmpty()) {
                    productBarcode = "0"
                }


                if (productName.isNotEmpty()) {

                    if (imageBitmap == null) {

                        saveProduct()

                    } else {
                        val fileName = "${documentID}.jpg"

                        val imageRef = storageRef.child(fileName)
                        val baos = ByteArrayOutputStream()
                        imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        // Upload the image to Firebase Storage
                        val uploadTask = imageRef.putBytes(data)

                        uploadTask.addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                productImageLink = uri.toString()
                                saveProduct()


                            }.addOnFailureListener {

                                Toast.makeText(this, "Başarısız", Toast.LENGTH_SHORT).show()

                            }
                        }.addOnFailureListener {
                            // Handle unsuccessful uploads
                            Toast.makeText(this, "Başarısız", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    productNameEditText.error = "Bu Alan Boş Bırakılamaz"
                    addProductButton.isClickable = true
                }
            }
        }


    }


    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), cameraPermissionCode
            )
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), storagePermissionCode
            )
        }
    }


    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            cameraPermissionCode, storagePermissionCode -> {

            }
        }
    }

    private fun openImageSourceDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Görsel Kaynağını Seçin")
            .setMessage("Görselin nereden alınacağını seçin:").setPositiveButton("Kamera") { _, _ ->
                dispatchTakePictureIntent()
            }.setNegativeButton("Galeri") { _, _ ->
                openGallery()
            }.setCancelable(true).show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, requestImageCapture)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, requestImagePick)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                requestImageCapture -> {
                    imageBitmap = data?.extras?.get("data") as Bitmap
                    productImageView.setImageBitmap(imageBitmap)

                }

                requestImagePick -> {
                    val selectedImageUri = data?.data
                    imageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                    productImageView.setImageBitmap(imageBitmap)

                }
            }
        }
    }

    private fun saveProduct() {
        val cal = Calendar.getInstance()
        val product = hashMapOf(
            "productName" to productName,
            "productGetPrice" to productGetPrice.toDouble(),
            "productSellPrice" to productSellPrice.toDouble(),
            "productStock" to productStock.toInt(),
            "productDescription" to productDescription,
            "productImageLink" to productImageLink,
            "timestamp" to cal.time,
            "productSellerName" to productSellerName,
            "productSellerNumber" to productSellerNumber,
            "productBarcode" to productBarcode.toLong()
        )

        db.collection("users").document(auth.uid!!).collection("products").document(documentID)
            .set(product).addOnSuccessListener {

                Toast.makeText(this, "Başarılı!", Toast.LENGTH_SHORT).show()
                finish()

            }.addOnFailureListener {
                Toast.makeText(this, "Başarısız", Toast.LENGTH_SHORT).show()
                addProductButton.isClickable = true

            }

    }
}