package com.example.familysns

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController
import androidx.navigation.NavGraph

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val navInflater = navController.navInflater
        navGraph = navInflater.inflate(R.navigation.nav_graph)

        bottomNav = findViewById(R.id.bottom_navigation)

        // 🔥 Firestore에서 familyId 여부 판단
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.get().addOnSuccessListener { document ->
                val familyIdList = document.get("familyId") as? List<*>
                val hasFamily = !familyIdList.isNullOrEmpty()
                val familyId = familyIdList?.firstOrNull()?.toString()

                // ✅ SharedPreferences 저장
                com.example.familysns.util.PrefsHelper.saveUserId(this, userId)
                if (familyId != null) {
                    com.example.familysns.util.PrefsHelper.saveFamilyId(this, familyId)
                }

                navGraph.setStartDestination(
                    if (hasFamily) R.id.familyHomeFragment else R.id.homeFragment
                )
                navController.graph = navGraph
                setupBottomNav()
            }.addOnFailureListener {
                navGraph.setStartDestination(R.id.homeFragment)
                navController.graph = navGraph
                setupBottomNav()
            }
        }
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val userRef = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(currentUser.uid)

                        userRef.get().addOnSuccessListener { document ->
                            val familyIdList = document.get("familyId") as? List<*>
                            val destinationId = if (!familyIdList.isNullOrEmpty()) {
                                R.id.familyHomeFragment
                            } else {
                                R.id.homeFragment
                            }

                            if (!navController.popBackStack(destinationId, false)) {
                                navController.navigate(destinationId)
                            }
                        }
                    } else {
                        // 로그인 정보 없으면 기본 홈으로
                        if (!navController.popBackStack(R.id.homeFragment, false)) {
                            navController.navigate(R.id.homeFragment)
                        }
                    }
                    true
                }
                R.id.albumFragment -> {
                    if (!navController.popBackStack(R.id.albumFragment, false)) {
                        navController.navigate(R.id.albumFragment)
                    }
                    true
                }
                R.id.myPageFragment -> {
                    if (!navController.popBackStack(R.id.myPageFragment, false)) {
                        navController.navigate(R.id.myPageFragment)
                    }
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = when (destination.id) {
                R.id.createChannelFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }
}