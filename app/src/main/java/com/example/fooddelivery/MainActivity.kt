package com.example.fooddelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.fooddelivery.data.auth.TokenManager
import com.example.fooddelivery.data.repository.FoodRepository
import com.example.fooddelivery.ui.navigation.FreshBiteMainApp
import com.example.fooddelivery.ui.theme.FreshBiteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var foodRepository: FoodRepository

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userProfile by foodRepository.userProfileFlow.collectAsState()

            FreshBiteTheme(darkTheme = userProfile.isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FreshBiteMainApp(
                        foodRepository = foodRepository,
                        tokenManager = tokenManager
                    )
                }
            }
        }
    }
}
