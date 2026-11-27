package com.biblelib.navigation.graphs

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.PrefsRepo
import com.biblelib.feature.donation.view.screens.DonationScreen
import com.biblelib.feature.donation.view.screens.PaymentWebViewScreen
import com.biblelib.feature.donation.viewmodel.DonationViewModel
import com.biblelib.feature.help.view.HelpScreen
import com.biblelib.feature.how_it_works.view.HowItWorksScreen
import kotlinx.coroutines.launch

fun NavGraphBuilder.miscGraph(
    navController: NavHostController,
    prefsRepo: PrefsRepo,
) {
    composable(Routes.HOW_IT_WORKS) {
        HowItWorksScreen(navController = navController)
    }

    composable(Routes.HELP) {
        HelpScreen(navController = navController)
    }
    composable(Routes.DONATION) {
        val donationVm: DonationViewModel = hiltViewModel()
        DonationScreen(navController = navController, viewModel = donationVm)
    }

    composable(
        route = Routes.PAYMENT_WEBVIEW,
        arguments = listOf(
            navArgument("redirectUrl") { type = NavType.StringType }
        ),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("redirectUrl") ?: ""
        val redirectUrl = Routes.decodeRedirectUrl(encoded)

        val donationEntry = remember(backStackEntry) {
            navController.previousBackStackEntry ?: navController.getBackStackEntry(Routes.DONATION)
        }
        val viewModel: DonationViewModel = hiltViewModel(donationEntry)
        val scope = rememberCoroutineScope()

        PaymentWebViewScreen(
            navController = navController,
            viewModel = viewModel,
            redirectUrl = redirectUrl,
            onPaymentComplete = { isSuccess ->
                if (isSuccess) {
                    scope.launch { prefsRepo.recordDonation() }
                    navController.navigate(Routes.READER) {
                        popUpTo(Routes.READER) { inclusive = false }
                    }
                } else {
                    viewModel.resetState()
                    navController.popBackStack()
                }
            },
        )
    }
}
