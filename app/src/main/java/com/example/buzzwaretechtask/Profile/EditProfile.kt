package com.example.buzzwaretechtask.Profile

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.buzzwaretechtask.databinding.ActivityEditprofileBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class DataDisplayActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityEditprofileBinding
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditprofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        firestore = FirebaseFirestore.getInstance()

        textView = binding.name
//        imageView = binding.imagepreview
        fetchDataFromFirestore()
    }
    private fun fetchDataFromFirestore() {
        val collectionReference = firestore.collection("your_collection")
        val documentReference = collectionReference.document("your_document_id")

        documentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    val fullName = data?.get("fullName") as String
                    val email = data["email"] as String
                    val imageURL = data["imageURL"] as String

                    // Display the data in your TextView or any other UI element
                    val displayText = "Full Name: $fullName\nEmail: $email"
                    textView.text = displayText
                    Glide.with(this)
                        .load(imageURL)
                        .into(imageView)
                } else {
                    textView.text = "Document does not exist."
                }
            }
            .addOnFailureListener { e ->
                textView.text = "Error: ${e.message}"
            }
    }
}
