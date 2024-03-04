package com.karaketir.fiyatvestoktakip.activities

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Transition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.karaketir.fiyatvestoktakip.databinding.ActivityPhotoViewerBinding


class PhotoViewerActivity : AppCompatActivity() {
    private lateinit var photoLink: String
    private lateinit var binding: ActivityPhotoViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoLink = intent.getStringExtra("photoLink").toString()

        Glide.with(this).load(photoLink).into(object : CustomTarget<Drawable?>() {
            @SuppressLint("SetTextI18n")
            override fun onResourceReady(
                resource: Drawable,
                transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?
            ) {
                binding.imageGlide.setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit

        })

    }
}