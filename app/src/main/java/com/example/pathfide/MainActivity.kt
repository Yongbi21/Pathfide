    package com.example.pathfide

    import android.annotation.SuppressLint
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.view.Menu
    import android.view.MenuItem
    import androidx.activity.OnBackPressedCallback
    import androidx.annotation.RequiresApi
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.widget.Toolbar
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.drawerlayout.widget.DrawerLayout
    import androidx.lifecycle.ViewModelProvider
    import androidx.navigation.NavController
    import androidx.navigation.NavGraph
    import androidx.navigation.NavOptions
    import androidx.navigation.fragment.NavHostFragment
    import androidx.navigation.ui.AppBarConfiguration
    import androidx.navigation.ui.NavigationUI
    import androidx.navigation.ui.navigateUp
    import androidx.navigation.ui.setupActionBarWithNavController
    import androidx.navigation.ui.setupWithNavController
    import com.example.pathfide.Activities.LanguageSettingsActivity
    import com.example.pathfide.Activities.SigninActivity
    import com.example.pathfide.Fragments.Chat.ChatFragment
    import com.example.pathfide.ViewModel.NotificationViewModel
    import com.google.android.material.navigation.NavigationView
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.messaging.FirebaseMessaging
    import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
    import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
    import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
    import java.text.SimpleDateFormat
    import java.util.Date
    import java.util.Locale

    class MainActivity : AppCompatActivity() {

        private lateinit var drawerLayout: DrawerLayout
        private lateinit var navigationView: NavigationView
        private lateinit var navController: NavController
        private lateinit var appBarConfiguration: AppBarConfiguration
        private lateinit var toolbar: Toolbar
        private lateinit var userType: String
        private lateinit var db: FirebaseFirestore
        private lateinit var notificationViewModel: NotificationViewModel
        private lateinit var auth: FirebaseAuth
        private val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"


        @SuppressLint("LogNotTimber")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            db = FirebaseFirestore.getInstance()

            // Get user type from intent
            userType = intent.getStringExtra("USER_TYPE") ?: "CLIENT"
            Log.d("MainActivity", "onCreate: UserType = $userType")
            setUserStatus(true)
            invalidateOptionsMenu()

            // Inside onCreate()
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                initializeZegoCallService(it.uid)
                registerFCMToken()
            }

            // Initialize Toolbar and DrawerLayout
            toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            drawerLayout = findViewById(R.id.drawerLayout)
            navigationView = findViewById(R.id.navBar)
            val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val lastCompletedDate = sharedPref.getString("lastMoodTrackerDate", null)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Initialize NavController
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController

            if (lastCompletedDate == today) {
                // If completed today, go to MoodSummaryFragment
                navController.navigate(R.id.moodSummaryFragment)
            } else {
                // If not completed today, go to MoodTrackerFragment
                navController.navigate(R.id.moodTrackerFragment)
            }

            // Set up AppBarConfiguration
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.homeFragment,
                    R.id.clientHistoryFragment,
                    R.id.clientProfileFragment,
                    R.id.TherapistHomeFragment,
                    R.id.therapistProfileFragment,
                    R.id.therapistHistoryFragment,
                    R.id.notificationFragment,
                    R.id.moodSummaryFragment,
                    R.id.favoriteFragment,
                    R.id.bookingOptionFragment,
                    R.id.bookingMethodFragment,
                    R.id.scheduledTimeFragment,
                    R.id.adminPanelFragment,
                    R.id.sessionStatusFragment,
                    R.id.bookingFormFragment
                    ),
                drawerLayout
            )

            // Request notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS_PERMISSION), 101)
                }
            }


            // Set up the toolbar
            setSupportActionBar(toolbar)

            // Set up ActionBar with NavController and AppBarConfiguration
            setupActionBarWithNavController(navController, appBarConfiguration)
            navigationView.setupWithNavController(navController)
            // Modify the NavigationView menu based on user type
            setupNavigationMenu()

            // Dynamically set the start destination based on user type
            setStartDestination(userType)

            // Handle navigation item clicks
            navigationView.setNavigationItemSelectedListener { menuItem ->
                Log.d("MainActivity", "NavigationItemSelected: ${menuItem.title} clicked")
                when (menuItem.itemId) {
                    R.id.LanguageSettings -> {
                        Log.d(
                            "MainActivity",
                            "NavigationItemSelected: Navigating to LanguageSettingsActivity"
                        )
                        val intent = Intent(this, LanguageSettingsActivity::class.java)
                        startActivity(intent)
                        drawerLayout.closeDrawers()
                        true
                    }

                    R.id.nav_logout -> {
                        Log.d("MainActivity", "NavigationItemSelected: Logging out")
                        logoutUser()
                        true
                    }

                    R.id.homeFragment, R.id.TherapistHomeFragment, R.id.clientProfileFragment, R.id.therapistProfileFragment,
                    R.id.notificationFragment, R.id.therapistHistoryFragment -> {
                        Log.d(
                            "MainActivity",
                            "NavigationItemSelected: Navigating to fragment ID ${menuItem.itemId}"
                        )
                        navigateToFragment(menuItem.itemId)
                        drawerLayout.closeDrawers()
                        true
                    }
                    R.id.clientHistoryFragment -> {
                        // Force navigation to the fragment regardless of current destination
                        navController.navigate(R.id.clientHistoryFragment, null,
                            NavOptions.Builder()
                                .setPopUpTo(navController.graph.startDestinationId, false)
                                .build())
                        drawerLayout.closeDrawers()
                        true
                    }

                    else -> {
                        val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                        if (handled) {
                            Log.d(
                                "MainActivity",
                                "NavigationItemSelected: Handled by NavController"
                            )
                            drawerLayout.closeDrawers()
                        }
                        handled
                    }
                }
            }

            // Check if the intent has extra to open the ChatFragment
            if (intent.getBooleanExtra("openChat", false)) {
                openChatFragment()
            }
//            startCallService()

            notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
            setSupportActionBar(toolbar)

            // Observe the LiveData and trigger a menu refresh when notifications change
            notificationViewModel.hasNotifications.observe(this) { hasNotifications ->
                Log.d("MainActivity", "Notification state changed: $hasNotifications")
                invalidateOptionsMenu()  // Force menu refresh on state change
            }
            notificationViewModel.observeNotifications(userType)
            notificationViewModel.observePaymentNotifications()
            notificationViewModel.fetchScheduledSessions()

            // Custom back navigation handling
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackNavigation()
                }
            })

        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 100 && grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Notification permission granted")
                } else {
                    Log.d("MainActivity", "Notification permission denied")
                }
            }
        }


        private fun handleBackNavigation() {
            when (navController.currentDestination?.id) {
                R.id.selfCareTipsFragment,
                R.id.clientHistoryFragment,
                R.id.favoriteFragment -> {
                    // Always navigate back to HomeFragment
                    navController.navigate(R.id.homeFragment)
                }
                else -> {
                    if (!navController.popBackStack()) {
                        super.onBackPressed()
                    }
                }
            }
        }


            override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
            val notificationMenuItem = menu?.findItem(R.id.notification)
            Log.d("MainActivity", "Preparing options menu with hasNotifications=${notificationViewModel.hasNotifications.value}")
            // Set the icon based on the latest notification state
            notificationMenuItem?.icon = if (notificationViewModel.hasNotifications.value == true) {
                ContextCompat.getDrawable(this, R.drawable.redbell)
            } else {
                ContextCompat.getDrawable(this, R.drawable.bell)
            }
            return super.onPrepareOptionsMenu(menu)
        }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.top_app_bar, menu)

            // Check user type and hide/show items
            if (userType == "ADMIN") {
                // Only show logout for the admin user
                menu?.findItem(R.id.notification)?.isVisible = false
            } else {
                // For non-admin users, show notification icon
                menu?.findItem(R.id.notification)?.isVisible = true
            }

            return true
        }

        // Dynamically sets the start destination in the navigation graph based on user type
        private fun setStartDestination(userType: String) {
            val navGraph: NavGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            when (userType) {
                "THERAPIST" -> {
                    navGraph.setStartDestination(R.id.TherapistHomeFragment)
                }
                "ADMIN" -> {
                    navGraph.setStartDestination(R.id.adminPanelFragment)
                }
                else -> {
                    navGraph.setStartDestination(R.id.moodTrackerFragment)
                }
            }
            navController.graph = navGraph
        }




        private fun setupNavigationMenu() {
            val menu = navigationView.menu
            when (userType) {
                "CLIENT" -> {
                    Log.d("MainActivity", "setupNavigationMenu: Client-specific menu setup")
                    menu.findItem(R.id.homeFragment)?.isVisible = true
                    menu.findItem(R.id.TherapistHomeFragment)?.isVisible = false
                    menu.findItem(R.id.clientProfileFragment)?.isVisible = true
                    menu.findItem(R.id.therapistProfileFragment)?.isVisible = false
                    menu.findItem(R.id.clientHistoryFragment)?.isVisible = true
                    menu.findItem(R.id.therapistHistoryFragment)?.isVisible = false
                    menu.findItem(R.id.sessionStatusFragment)?.isVisible = false
                    menu.findItem(R.id.adminPanelFragment)?.isVisible = false

                }
                "THERAPIST" -> {
                    Log.d("MainActivity", "setupNavigationMenu: Therapist-specific menu setup")
                    menu.findItem(R.id.homeFragment)?.isVisible = false
                    menu.findItem(R.id.TherapistHomeFragment)?.isVisible = true
                    menu.findItem(R.id.clientProfileFragment)?.isVisible = false
                    menu.findItem(R.id.therapistProfileFragment)?.isVisible = true
                    menu.findItem(R.id.clientHistoryFragment)?.isVisible = false
                    menu.findItem(R.id.therapistHistoryFragment)?.isVisible = true
                    menu.findItem(R.id.favoriteFragment)?.isVisible = false
                    menu.findItem(R.id.LanguageSettings)?.isVisible = false
                    menu.findItem(R.id.sessionStatusFragment)?.isVisible = false
                    menu.findItem(R.id.adminPanelFragment)?.isVisible = false



                }
                "ADMIN" -> {
                    // Show admin-specific items or features
                    Log.d("MainActivity", "setupNavigationMenu: Admin-specific menu setup")
                    menu.findItem(R.id.homeFragment)?.isVisible = false
                    menu.findItem(R.id.TherapistHomeFragment)?.isVisible = false
                    menu.findItem(R.id.adminPanelFragment)?.isVisible = true // Admin panel item visible
                    menu.findItem(R.id.clientProfileFragment)?.isVisible = false
                    menu.findItem(R.id.therapistProfileFragment)?.isVisible = false
                    menu.findItem(R.id.clientHistoryFragment)?.isVisible = false
                    menu.findItem(R.id.therapistHistoryFragment)?.isVisible = false
                    menu.findItem(R.id.favoriteFragment)?.isVisible = false
                    menu.findItem(R.id.LanguageSettings)?.isVisible = false
                    menu.findItem(R.id.sessionStatusFragment)?.isVisible = true

                }
            }

        }

        private fun navigateToFragment(fragmentId: Int) {
            Log.d("MainActivity", "navigateToFragment: Navigating to fragment ID $fragmentId with UserType = $userType")
            val bundle = Bundle().apply {
                putString("USER_TYPE", userType)
            }
            navController.navigate(fragmentId, bundle)
        }


        // Open ChatFragment directly
        private fun openChatFragment() {
            val fragment = ChatFragment()
            val bundle = Bundle().apply {
                putString("USER_TYPE", "CLIENT") // Assuming client as default, modify as needed
            }
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit()
        }


        private fun registerFCMToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Save the token in Firestore
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener

                db.collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("MainActivity", "FCM token updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Error updating FCM token", e)
                    }
            }
        }


        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.notification -> {
                    Log.d("MainActivity", "onOptionsItemSelected: Notification clicked")

                    // Mark notifications as read
                    notificationViewModel.clearNotifications()

                    // Refresh the menu to update the icon
                    invalidateOptionsMenu()

                    // Navigate to notification screen
                    navigateToFragment(R.id.notificationFragment)
                    true
                }
                R.id.nav_logout -> {
                    Log.d("MainActivity", "onOptionsItemSelected: Logout clicked")
                    logoutUser()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        override fun onSupportNavigateUp(): Boolean {
            Log.d("MainActivity", "onSupportNavigateUp: Navigation up clicked")
            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }

        // Update the user's online status when logging in or out
        private fun setUserStatus(isOnline: Boolean) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            val updates = hashMapOf<String, Any>(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()  // Add this to track last activity
            )

            db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    Log.d("MainActivity", "User status updated successfully: isOnline = $isOnline")
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error updating status", e)
                }
        }


        override fun onPause() {
            super.onPause()
            setUserStatus(false)
        }


        private fun initializeZegoCallService(userId: String) {
            val appIDString: String = getString(R.string.app_id)
            val appID: Long = appIDString.toLong()
            val appSign: String = getString(R.string.app_sign)

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val userName = "$firstName $lastName".trim()

                        Log.d("MainActivity", "User Full Name: $userName")  // Ensure full name is correct

                        // Check if the full name is correct here:
                        if (userName.isEmpty()) {
                            Log.e("MainActivity", "Error: Username is empty.")
                        } else {
                            Log.d("MainActivity", "Initializing Zego call service for: $userName")

                            val config = ZegoUIKitPrebuiltCallInvitationConfig()
                            config.translationText.apply {
                                incomingCallPageDeclineButton = "Decline"
                                incomingCallPageAcceptButton = "Accept"
                            }

                            // Initialize the service
                            ZegoUIKitPrebuiltCallService.init(
                                application,
                                appID,
                                appSign,
                                userId,
                                userName,
                                config
                            )

                            Log.d("MainActivity", "Zego initialized for: $userName ($userId)")
                        }
                    } else {
                        Log.e("MainActivity", "No user data found for ID: $userId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error fetching user details: ", e)
                }
        }



        private fun logoutUser() {
            Log.d("MainActivity", "logoutUser: Starting logout process")

            // First uninitialize ZEGO service
            try {
                ZegoUIKitPrebuiltCallService.unInit()
                Log.d("MainActivity", "ZEGO service uninitialized")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error uninitializing ZEGO service", e)
            }

            // Ensure safe access to MyApplication instance
            (application as? MyApplication)?.forceOfflineStatus()

            // Sign out from Firebase and transition to SigninActivity
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SigninActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }



        override fun onDestroy() {
            super.onDestroy()
            if (!isChangingConfigurations) {
                (application as MyApplication).forceOfflineStatus()
            }
            ZegoUIKitPrebuiltCallInvitationService.unInit()
        }

        override fun onResume() {
            super.onResume()
            setUserStatus(true)
        }


    }
