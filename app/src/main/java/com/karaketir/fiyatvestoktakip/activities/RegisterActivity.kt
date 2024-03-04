package com.karaketir.fiyatvestoktakip.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.fiyatvestoktakip.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {


    private lateinit var documentID: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityRegisterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val signUpButton = binding.signUpButton
        val emailEditText = binding.emailRegisterEditText
        val passwordEditText = binding.passwordRegisterEditText
        val logInButton = binding.logInButton


        logInButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        signUpButton.setOnClickListener {
            if (emailEditText.text.toString().isNotEmpty()) {
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()) {

                    if (passwordEditText.text.toString().length >= 6) {
                        passwordEditText.error = null
                        signUp(
                            emailEditText.text.toString(),
                            passwordEditText.text.toString(),
                        )
                    }
                } else {
                    passwordEditText.error = "Şifre En Az 6 Karakter Uzunluğunda Olmalı"
                }


            } else {
                emailEditText.error = "Bu Alan Boş Bırakılamaz"
            }
        }


    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                documentID = auth.uid!!

                db.collection("users").document(documentID).set(
                    hashMapOf(
                        "email" to auth.currentUser!!.email, "uid" to documentID
                    )
                )
                val newIntent = Intent(this, MainActivity::class.java)
                this.startActivity(newIntent)
                finish()


            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(
                    baseContext, "Kayıt Başarısız!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}