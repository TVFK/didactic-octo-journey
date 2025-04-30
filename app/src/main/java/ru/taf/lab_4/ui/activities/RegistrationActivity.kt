package ru.taf.lab_4.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.taf.lab_4.data.AppDatabaseHelper
import ru.taf.lab_4.databinding.ActivityRegistrationBinding
import ru.taf.lab_4.model.User

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etFirstName.text.toString().trim()
            val surname = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()


            if(name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                val user = User(0, name, surname, email, password)

                val db = AppDatabaseHelper(this)
                db.saveUser(user)

                val intent = Intent(this, LoginActivity::class.java).apply {
                    putExtra("user", user)
                }
                startActivity(intent)
            }
        }
    }
}