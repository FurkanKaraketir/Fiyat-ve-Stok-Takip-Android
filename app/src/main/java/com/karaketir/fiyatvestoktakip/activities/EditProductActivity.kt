@file:Suppress("DEPRECATION")

package com.karaketir.fiyatvestoktakip.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private val storage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

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
    private lateinit var addProductImageButton: Button
    private lateinit var productImageView: ImageView
    private lateinit var deleteProductButton: Button
    private lateinit var productSellerNameEditText: EditText
    private lateinit var productSellerNumberEditText: EditText
    private lateinit var productBarcodeEditText: EditText
    private lateinit var uploadProgressBar: ProgressBar

    private val requestImageCapture = 1
    private val requestImagePick = 2
    private var imageBitmap: Bitmap? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImageSourceDialog()
        } else {
            Toast.makeText(this, "Kamera izni verilmedi", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImageSourceDialog()
        } else {
            Toast.makeText(this, "Depolama izni verilmedi", Toast.LENGTH_SHORT).show()
        }
    }

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
        uploadProgressBar = binding.uploadProgressBar

        productNameEditText.setText(productName)
        productSellPriceEditText.setText(productSellPrice)
        productGetPriceEditText.setText(productGetPrice)
        productStockEditText.setText(productStock)
        productDescriptionEditText.setText(productDescription)
        productSellerNameEditText.setText(productSellerName)
        productSellerNumberEditText.setText(productSellerNumber)
        productBarcodeEditText.setText(productBarcode)

        if (productImageLink.isNotEmpty()) {
            productImageView.visibility = View.VISIBLE
            Glide.with(this).load(productImageLink).into(productImageView)
        } else {
            productImageView.setImageResource(0)
        }

        addProductImageButton.setOnClickListener {
            checkAndRequestPermissions()
        }

        addProductButton.setOnClickListener {
            addProductButton.isClickable = false
            handleUpdateProduct()
        }

        deleteProductButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun checkAndRequestPermissions() {
        when {
            !hasCameraPermission() -> requestCameraPermission()
            !hasStoragePermission() -> requestStoragePermission()
            else -> openImageSourceDialog()
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasStoragePermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            showPermissionExplanationDialog(
                "Kamera izni gerekli",
                "Kamera izni, ürün fotoğrafı çekmek için gereklidir. Lütfen izni verin."
            ) {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestStoragePermission() {
        val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, storagePermission)) {
            showPermissionExplanationDialog(
                "Depolama izni gerekli",
                "Depolama izni, galeriden görsel seçmek için gereklidir. Lütfen izni verin."
            ) {
                requestStoragePermissionLauncher.launch(storagePermission)
            }
        } else {
            requestStoragePermissionLauncher.launch(storagePermission)
        }
    }

    private fun showPermissionExplanationDialog(
        title: String, message: String, onPositiveButtonClick: () -> Unit
    ) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton("Tamam") { _, _ -> onPositiveButtonClick() }
            .setNegativeButton("İptal", null).show()
    }

    private fun openImageSourceDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Görsel Kaynağını Seçin")
            .setMessage("Görselin nereden alınacağını seçin:")
            .setPositiveButton("Kamera") { _, _ -> dispatchTakePictureIntent() }
            .setNegativeButton("Galeri") { _, _ -> openGallery() }.setCancelable(true).show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, requestImageCapture)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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

    private fun handleUpdateProduct() {
        productName = productNameEditText.text.toString()
        productGetPrice = productGetPriceEditText.text.toString().ifEmpty { "0" }
        productSellPrice = productSellPriceEditText.text.toString().ifEmpty { "0" }
        productStock = productStockEditText.text.toString().ifEmpty { "0" }
        productDescription = productDescriptionEditText.text.toString().ifEmpty { "" }
        productSellerName = productSellerNameEditText.text.toString().ifEmpty { "" }
        productSellerNumber = productSellerNumberEditText.text.toString().ifEmpty { "" }
        productBarcode = productBarcodeEditText.text.toString().ifEmpty { "0" }

        if (productName.isNotEmpty()) {
            if (imageBitmap == null) {
                saveProduct()
            } else {
                uploadProductImage()
            }
        } else {
            productNameEditText.error = "Bu Alan Boş Bırakılamaz"
            addProductButton.isClickable = true
        }
    }

    private fun uploadProductImage() {
        val fileName = "$documentID.jpg"
        val imageRef = storageRef.child(fileName)
        val baos = ByteArrayOutputStream()
        imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Show the ProgressBar
        uploadProgressBar.visibility = View.VISIBLE
        productImageView.visibility = View.GONE

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                productImageLink = uri.toString()
                saveProduct()
                // Hide the ProgressBar
                uploadProgressBar.visibility = View.GONE
            }.addOnFailureListener {
                showToast("Görsel yükleme başarısız")
                addProductButton.isClickable = true
                // Hide the ProgressBar
                uploadProgressBar.visibility = View.GONE
            }
        }.addOnFailureListener {
            showToast("Görsel yükleme başarısız")
            addProductButton.isClickable = true
            // Hide the ProgressBar
            uploadProgressBar.visibility = View.GONE
        }

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            uploadProgressBar.progress = progress.toInt()
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
                showToast("Başarılı!")
                finish()
            }.addOnFailureListener {
                showToast("Ürün kaydedilemedi")
                addProductButton.isClickable = true
            }
    }

    private fun showDeleteConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Ürünü Sil").setMessage("Ürünü silmek istediğinize emin misiniz?")
            .setPositiveButton("Evet") { _, _ -> deleteProduct() }.setNegativeButton("İptal", null)
            .setCancelable(true).show()
    }

    private fun deleteProduct() {
        db.collection("users").document(auth.uid!!).collection("products").document(documentID)
            .delete().addOnSuccessListener {
                showToast("Ürün Silindi")
                finish()
            }.addOnFailureListener {
                showToast("Ürün Silinemedi")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
