package com.alarysai.alarysai.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alarysai.alarysai.feature.auth.presentation.forgot.ForgotPasswordScreenRoute
import com.alarysai.alarysai.feature.auth.presentation.login.LoginScreenRoute
import com.alarysai.alarysai.feature.auth.presentation.signup.SignUpScreenRoute

/**
 * Everything shown while nobody is signed in: login, sign-up and password recovery. The app root
 * shows it instead of the main navigation and leaves it on its own once a session exists.
 */
@Composable
fun AuthFlow() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LOGIN) {
        composable(LOGIN) {
            LoginScreenRoute(
                onOpenSignUp = { navController.navigate(SIGN_UP) },
                onOpenForgotPassword = { navController.navigate(FORGOT_PASSWORD) },
            )
        }
        composable(SIGN_UP) { SignUpScreenRoute(onBackToLogin = { navController.popBackStack() }) }
        composable(FORGOT_PASSWORD) { ForgotPasswordScreenRoute(onBackToLogin = { navController.popBackStack() }) }
    }
}

private const val LOGIN = "auth/login"
private const val SIGN_UP = "auth/sign_up"
private const val FORGOT_PASSWORD = "auth/forgot_password"
