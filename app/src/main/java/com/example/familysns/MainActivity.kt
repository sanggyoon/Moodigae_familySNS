package com.example.familysns

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hasFamily = intent.getBooleanExtra("hasFamily", false)

        // NavController 및 NavGraph 구성
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController
        val navInflater = navController.navInflater
        val navGraph: NavGraph = navInflater.inflate(R.navigation.nav_graph)

        // ✅ startDestination 분기
        navGraph.setStartDestination(
            if (hasFamily) R.id.familyHomeFragment
            else R.id.homeFragment
        )

        navController.graph = navGraph

        // 바텀 네비게이션 설정
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (!navController.popBackStack(R.id.homeFragment, false)) {
                        navController.navigate(R.id.homeFragment)
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