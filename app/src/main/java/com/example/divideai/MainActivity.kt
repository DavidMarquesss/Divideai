package com.example.divideai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.divideai.data.image.Base64Image
import com.example.divideai.databinding.ActivityMainBinding
import com.example.divideai.notifications.DivideAiMessagingService
import com.example.divideai.ui.dashboard.DashboardActivity
import com.example.divideai.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * Pede POST_NOTIFICATIONS no Android 13+. Em versões anteriores a permissão
     * é concedida automaticamente na instalação, então o launcher nunca é chamado.
     */
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* result ignored — token registration happens regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        val profileImage = binding.topAppBar.findViewById<ImageView>(R.id.img_profile_action)
        val dashboardImage = binding.topAppBar.findViewById<ImageView>(R.id.img_dashboard_action)

        profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        dashboardImage.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        DivideAiMessagingService.ensureChannel(this)
        requestNotificationPermissionIfNeeded()
        registerFcmTokenForCurrentUser()
    }

    override fun onResume() {
        super.onResume()
        loadProfileAvatar()
    }

    private fun loadProfileAvatar() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val profileImage = binding.topAppBar.findViewById<ImageView>(R.id.img_profile_action)
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val bmp = Base64Image.decode(doc.getString("profilePhoto"))
                if (bmp != null) {
                    profileImage.setImageBitmap(bmp)
                } else {
                    profileImage.setImageResource(R.drawable.ic_generic_avatar_gray)
                }
            }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Recupera o token FCM atual e persiste no documento do usuário logado.
     * Um servidor (ex.: o script `notifier/send.js`) lê esse campo para enviar
     * pushes direcionados a este usuário.
     */
    private fun registerFcmTokenForCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token)
            }
            .addOnFailureListener { e ->
                Log.w("MainActivity", "Failed to retrieve FCM token", e)
            }
    }
}