@file:Suppress("DEPRECATION")

package com.karaketir.fiyatvestoktakip.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.fiyatvestoktakip.adapters.ProductAdapter
import com.karaketir.fiyatvestoktakip.databinding.ActivityMainBinding
import com.karaketir.fiyatvestoktakip.models.Product
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    private lateinit var binding: ActivityMainBinding
    private var productList = ArrayList<Product>()
    private var filteredList = ArrayList<Product>()
    private lateinit var layoutManager: LayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.registerListener(installStateUpdatedListener)


        checkForAppUpdate()

        auth = Firebase.auth
        db = Firebase.firestore
        layoutManager = LinearLayoutManager(this)
        recyclerView = binding.productRecyclerView
        recyclerView.layoutManager = layoutManager

        setupRecyclerView(productList)

        val searchEditText = binding.searchEditText
        val signOutButton = binding.signOutButton

        signOutButton.setOnClickListener {
            val signOutAlertDialog = AlertDialog.Builder(this)
            signOutAlertDialog.setTitle("Çıkış Yap")
            signOutAlertDialog.setMessage("Hesabınızdan Çıkış Yapmak İstediğinize Emin misiniz?")
            signOutAlertDialog.setPositiveButton("Çıkış") { _, _ ->
                auth.signOut()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                this.startActivity(intent)
                finish()
            }
            signOutAlertDialog.setNegativeButton("İptal") { _, _ ->

            }
            signOutAlertDialog.show()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredList = ArrayList()
                if (p0.toString() != "") {
                    for (item in productList) {
                        if (item.name.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.sortBy { it.name }
                            filteredList.add(item)
                        } else if (item.sellerName.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.sortBy { it.name }
                            filteredList.add(item)
                        } else if (item.sellerNumber.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.sortBy { it.name }
                            filteredList.add(item)
                        } else if (item.description.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.sortBy { it.name }
                            filteredList.add(item)
                        } else if (item.barcode.toString().lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.sortBy { it.name }
                            filteredList.add(item)
                        }
                    }
                    setupRecyclerView(filteredList)
                } else {
                    setupRecyclerView(productList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        binding.addProductButton.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            this.startActivity(intent)
        }

        if (auth.currentUser != null) {

            db.collection("users").document(auth.currentUser!!.uid).collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    productList.clear()

                    if (value != null) {
                        for (document in value) {
                            val name = document.get("productName").toString()
                            val buyPrice = document.get("productGetPrice").toString().toDouble()
                            val sellPrice = document.get("productSellPrice").toString().toDouble()
                            val stock = document.get("productStock").toString().toInt()
                            val description = document.get("productDescription").toString()
                            val imageLink = document.get("productImageLink").toString()
                            val timestamp =
                                document.get("timestamp") as com.google.firebase.Timestamp
                            val sellerName = document.get("productSellerName").toString()
                            val sellerNumber = document.get("productSellerNumber").toString()
                            val barcode: Long = try {
                                document.get("productBarcode").toString().toLong()

                            } catch (e: Exception) {
                                0
                            }
                            val product = Product(
                                name,
                                buyPrice,
                                sellPrice,
                                stock,
                                description,
                                imageLink,
                                timestamp,
                                document.id,
                                sellerName,
                                sellerNumber,
                                barcode
                            )
                            productList.add(product)
                        }
                        adapter.notifyDataSetChanged()
                    }
                    if (error != null) {
                        println("Error: ${error.message}")
                    }
                }

        }
    }

    private fun setupRecyclerView(list: List<Product>) {
        adapter = ProductAdapter(list)
        recyclerView.adapter = adapter
    }

    private fun checkForAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, updateType, this, 1
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode != RESULT_OK) {
                checkForAppUpdate()
            }
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            lifecycleScope.launch {
                appUpdateManager.completeUpdate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, updateType, this, 1
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }


}