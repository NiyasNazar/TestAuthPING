package com.pl.testauth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretPost
import net.openid.appauth.ResponseTypeValues

class MainActivity : AppCompatActivity() {
    private lateinit var authService: AuthorizationService
    private lateinit var authState: AuthState
    private val TAG = "OIDC_Login"

    private lateinit var authLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        authService = AuthorizationService(this)
        authState=AuthState()


        authLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the successful result here
                Log.e(TAG, "registerForActivityResult cancelled or failed. No data returned.")

            } else {
                Log.e(TAG, "registerForActivityResult cancelled or ss. No data returned."+authState.lastAuthorizationResponse)

                // Handle the case where the result was not successful
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun Login(view: View) {
        performLogin()




    }

    private fun performLogin() {
        // Ping Identity OIDC configuration
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://auth.pingone.com.au/a6cf06a1-52f6-4b48-9443-b59171c40480/as/authorize"), // Authorization endpoint
            Uri.parse("https://auth.pingone.com.au/a6cf06a1-52f6-4b48-9443-b59171c40480/as/token")           // Token endpoint
        )

        val clientId = "03c9ee4b-2623-4f01-b5d6-a30e65a01fb2"
        val redirectUri = Uri.parse("com.pl.testauth://oauth2redirect")
        val scope = "openid profile email"
        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        ).setScope(scope).build()
        Log.d("LoginActivity", "Auth Request URI: ${authRequest.toUri().toString()}")

        try {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            val intentBuilder: CustomTabsIntent.Builder = authService.createCustomTabsIntentBuilder(authRequest.toUri())
            val authIntent = authService.getAuthorizationRequestIntent(authRequest, intentBuilder.build())

            Log.d(TAG, "Authorization response received. Client ID: ${authRequest.clientId}")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, authRequest.toUri())
            startActivity(fallbackIntent)
           // authLauncher.launch(authIntent) // Use the ActivityResultLauncher to start the intent
        } catch (e: Exception) {
            Log.e(TAG, "Error launching authorization intent: ${e.message}", e)
        }

    }



    private fun exchangeAuthorizationCode(response: AuthorizationResponse) {
        val clientAuthentication: ClientAuthentication = ClientSecretPost("VAnF~Au5ofXAB_PeYVO_5IJEPxcNUG6Ik-xUao~288AvZUf9LHXdiwVmxOmW--Lm")

        authService.performTokenRequest(
            response.createTokenExchangeRequest(),
            clientAuthentication
        ) { tokenResponse, exception ->
            if (tokenResponse != null) {
                authState.update(tokenResponse, exception)
                Log.d(TAG, "Access Token: ${tokenResponse.accessToken}")
                Log.d(TAG, "ID Token: ${tokenResponse.idToken}")
                // You can now access the tokens and authenticated user data
            } else {
                Log.e(TAG, "Token Exchange failed: ${exception?.localizedMessage}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//authService.dispose()
    }

    companion object {
        private const val RC_AUTH = 100
    }

    fun Logout(view: View) {

    }


}