package com.karaketir.fiyatvestoktakip.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.fiyatvestoktakip.R
import com.karaketir.fiyatvestoktakip.activities.EditProductActivity
import com.karaketir.fiyatvestoktakip.activities.PhotoViewerActivity
import com.karaketir.fiyatvestoktakip.databinding.ProductRowBinding
import com.karaketir.fiyatvestoktakip.models.Product

class ProductAdapter(private var productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductHolder>() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    class ProductHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProductRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.product_row, parent, false)
        return ProductHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ProductHolder, position: Int) {

        auth = Firebase.auth
        db = Firebase.firestore

        with(holder) {

            val myBinding = binding

            myBinding.productNameTextView.text = productList[position].name
            myBinding.productSellPriceTextView.text =
                "Satış Fiyatı: ${productList[position].sellPrice} TL"

            if (productList[position].imageLink != "") {
                Glide.with(myBinding.root.context).load(productList[position].imageLink)
                    .into(myBinding.productImageView)
            } else {
                myBinding.productImageView.setImageResource(0)
            }

            myBinding.productDescriptionTextView.text =
                "Açıklama: ${productList[position].description}"

            myBinding.productSellerNameTextView.text = "Satıcı: ${productList[position].sellerName}"
            myBinding.productSellerNumberTextView.text =
                "Telefon: ${productList[position].sellerNumber}"
            myBinding.productStockTextView.text = "Stok: ${productList[position].stock} Adet"

            myBinding.productBuyPriceTextView.setOnClickListener {

                myBinding.productBuyPriceTextView.text =
                    "Alış Fiyatı: ${productList[position].buyPrice} TL"
            }

            myBinding.productCardView.setOnClickListener {
                val newIntent = Intent(holder.itemView.context, EditProductActivity::class.java)
                newIntent.putExtra("documentID", productList[position].documentID)
                newIntent.putExtra("name", productList[position].name)
                newIntent.putExtra("buyPrice", productList[position].buyPrice.toString())
                newIntent.putExtra("sellPrice", productList[position].sellPrice.toString())
                newIntent.putExtra("stock", productList[position].stock.toString())
                newIntent.putExtra("description", productList[position].description)
                newIntent.putExtra("imageLink", productList[position].imageLink)
                newIntent.putExtra("sellerName", productList[position].sellerName)
                newIntent.putExtra("sellerNumber", productList[position].sellerNumber)
                newIntent.putExtra("barcode", productList[position].barcode.toString())
                holder.itemView.context.startActivity(newIntent)
            }

            myBinding.addProductStockButton.setOnClickListener {
                val stock = productList[position].stock
                val newStock = stock + 1
                db.collection("users").document(auth.uid!!).collection("products")
                    .document(productList[position].documentID).update("productStock", newStock)
            }

            myBinding.removeProductStockButton.setOnClickListener {
                val stock = productList[position].stock
                var newStock = stock - 1
                if (newStock < 0) {
                    newStock = 0
                }
                db.collection("users").document(auth.uid!!).collection("products")
                    .document(productList[position].documentID).update("productStock", newStock)
            }

            myBinding.productBarcodeTextView.text = "Barkod: ${productList[position].barcode}"

            myBinding.productImageView.setOnClickListener {
                val newIntent = Intent(holder.itemView.context, PhotoViewerActivity::class.java)
                newIntent.putExtra("photoLink", productList[position].imageLink)
                holder.itemView.context.startActivity(newIntent)
            }
        }

    }
}
