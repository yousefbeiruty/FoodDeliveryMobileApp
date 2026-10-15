package com.example.fooddelivery.ui.screens.profile

import com.example.fooddelivery.data.model.UserProfile

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(
        name = "Alex Green",
        email = "alex.green@eco.org",
        phone = "+1 (555) 234-5678",
        defaultAddress = "742 Evergreen Terrace, Green City",
        plasticSavedKg = 4.2,
        co2OffsetKg = 18.5,
        treesPlanted = 12,
        savedAddresses = listOf(
            "742 Evergreen Terrace, Green City",
            "100 Eco Tech Park, Suite 400",
            "15 Pine Crest Way, San Francisco"
        ),
        paymentMethods = listOf(
            "Visa ending in 4242 (Eco Card)",
            "Apple Pay",
            "FreshBite Eco Wallet ($45.00)"
        ),
        isDarkMode = false
    )
)
