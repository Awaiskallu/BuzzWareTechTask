package com.example.buzzwaretechtask.Profile

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.buzzwaretechtask.R
import com.example.buzzwaretechtask.databinding.ActivityProfileBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hbb20.CountryCodePicker
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val CAMERA_REQUEST_CODE = 101
    private lateinit var capturedImageBitmap: Bitmap
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    private lateinit var currentPhotoPath: String
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        val maleButton = binding.male
        val femaleButton = binding.female
        var selectedGender: String? = null

        maleButton.setOnClickListener {
            selectedGender = "Male"
            maleButton.setBackgroundResource(R.drawable.button_blue)
            femaleButton.setBackgroundResource(R.drawable.button_white)
        }

        femaleButton.setOnClickListener {
            selectedGender = "Female"
            femaleButton.setBackgroundResource(R.drawable.button_blue)
            maleButton.setBackgroundResource(R.drawable.button_white)
        }

        val captureButton = binding.plusIcon
        captureButton.setOnClickListener {
            if (hasCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }

        val getStartedButton = binding.getstarted
        getStartedButton.setOnClickListener {
            val fullName = binding.editname.text.toString()
            val email = binding.editemail.text.toString()
            val countryCode = binding.countryCodePicker.selectedCountryCode
            val mobileNumber = binding.mobileNumberEditText.text.toString()
            val aboutMe = binding.editT4.text.toString()
            saveImageToFirestore(fullName, email, countryCode, mobileNumber, aboutMe, selectedGender)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Dispatch the image capture intent
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.e(TAG, "Error creating image file: ${ex.message}")
            null
        }
        photoFile?.also {
            val photoURI = FileProvider.getUriForFile(
                this,
                "com.example.buzzwaretechtask.fileprovider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        }
    }

    // Handle the result of the image capture
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The image has been captured and saved to 'currentPhotoPath'
            // Load the image from 'currentPhotoPath' into 'capturedImageBitmap' or display it in your ImageView
            capturedImageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
        }
    }

    // Save the image to Firestore along with user data
    private fun saveImageToFirestore(
        fullName: String,
        email: String,
        countryCode: String,
        mobileNumber: String,
        aboutMe: String,
        gender: String?
    ) {
        // Create a reference to the Firebase Storage location where you want to store the image
        val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")

        // Create a ByteArrayOutputStream to compress the image
        val baos = ByteArrayOutputStream()
        capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Upload the image to Firebase Storage
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully, get its download URL
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Create a data object to save to Firestore, including the download URL
                val userData = hashMapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "countryCode" to countryCode,
                    "mobileNumber" to mobileNumber,
                    "gender" to gender,
                    "aboutMe" to aboutMe,
                    "imageURL" to downloadUri.toString()
                )

                // Save data to Firestore
                firestore.collection("users").add(userData)
                    .addOnSuccessListener { documentReference ->
                        // Data saved successfully
                        val userId = documentReference.id
                        Toast.makeText(
                            baseContext,
                            "Data Successfully saved.",
                            LENGTH_SHORT
                        ).show()
                        // Handle success, maybe navigate to another activity
                    }
                    .addOnFailureListener { e ->
                        // Handle errors
                        Log.e(TAG, "Error adding document", e)
                    }
            }
        }
        uploadTask.addOnFailureListener { e ->
            // Handle unsuccessful uploads
            Log.e(TAG, "Image upload failed: ${e.message}")
        }
    }
}
