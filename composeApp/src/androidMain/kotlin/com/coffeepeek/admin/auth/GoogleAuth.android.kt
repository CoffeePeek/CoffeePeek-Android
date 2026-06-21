package com.coffeepeek.admin.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

actual object GoogleAuth {
    actual fun isSupported(): Boolean = true

    actual suspend fun signIn(): Result<String> =
        Result.failure(UnsupportedOperationException("Используйте rememberGoogleSignInLauncher()"))

    @Composable
    fun rememberGoogleSignInLauncher(
        webClientId: String,
        onResult: (Result<String>) -> Unit,
    ): () -> Unit {
        if (webClientId.isBlank()) {
            return { onResult(Result.failure(Exception("GOOGLE_WEB_CLIENT_ID не настроен"))) }
        }

        val context = LocalContext.current
        val activity = context as? Activity

        val gso = remember(webClientId) {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        }
        val client = remember(gso) { GoogleSignIn.getClient(context, gso) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                onResult(Result.failure(Exception("Вход через Google отменён")))
                return@rememberLauncherForActivityResult
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val token = account.idToken
                if (token.isNullOrBlank()) {
                    onResult(Result.failure(Exception("Не удалось получить Google token")))
                } else {
                    onResult(Result.success(token))
                }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }

        return {
            if (activity == null) {
                onResult(Result.failure(Exception("Activity недоступна")))
            } else {
                launcher.launch(client.signInIntent)
            }
        }
    }
}
