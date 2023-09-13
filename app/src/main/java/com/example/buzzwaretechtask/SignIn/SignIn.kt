package com.example.buzzwaretechtask.SignIn

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buzzwaretechtask.MainActivity
import com.example.buzzwaretechtask.Profile.ProfileActivity
import com.example.buzzwaretechtask.R
import com.example.buzzwaretechtask.databinding.SigninBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var binding: SigninBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContentView(R.layout.signin)
        auth = FirebaseAuth.getInstance()

      val emailEditText = binding.editemaill
        val passwordEditText = binding.editpassword
       val  signInButton = binding.permissionbutn
        val registration = binding.registration

        registration.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            Toast.makeText(
                                baseContext,
                                "Authentication succeeded.",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    baseContext,
                    "Please enter both email and password.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
