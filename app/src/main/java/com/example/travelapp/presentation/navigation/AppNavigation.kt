package com.example.travelapp.presentation.navigation


import com.example.travelapp.data.repository.impl.FakeRouteRepository
import com.example.travelapp.presentation.route.RouteViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.core.ViewModelFactory
import com.example.travelapp.data.repository.impl.FakeAuthRepository
import com.example.travelapp.data.repository.impl.FakeTripRepository
import com.example.travelapp.presentation.auth.AuthViewModel
import com.example.travelapp.presentation.auth.LoginScreen
import com.example.travelapp.presentation.auth.RegisterScreen
import com.example.travelapp.presentation.trip.TripScreen
import com.example.travelapp.presentation.trips.CreateTripScreen
import com.example.travelapp.presentation.trips.CreateTripViewModel
import com.example.travelapp.presentation.trips.TripsScreen
import com.example.travelapp.presentation.trips.TripsViewModel

/**
 * AppNavigation описывает навигацию между экранами приложения.
 *
 * Здесь создаются:
 * - NavController;
 * - временные fake-репозитории;
 * - ViewModel;
 * - маршруты экранов.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    /**
     * remember нужен, чтобы репозитории не создавались заново
     * при каждой перерисовке интерфейса.
     */

    val authRepository = remember { FakeAuthRepository() }
    val tripRepository = remember { FakeTripRepository() }
    val routeRepository = remember { FakeRouteRepository() }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = ViewModelFactory {
                    AuthViewModel(authRepository)
                }
            )

            val uiState by authViewModel.uiState.collectAsState()

            LoginScreen(
                uiState = uiState,
                onEmailChange = authViewModel::updateEmail,
                onPasswordChange = authViewModel::updatePassword,
                onLoginClick = authViewModel::signIn,
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Trips.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = ViewModelFactory {
                    AuthViewModel(authRepository)
                }
            )

            val uiState by authViewModel.uiState.collectAsState()

            RegisterScreen(
                uiState = uiState,
                onNameChange = authViewModel::updateName,
                onEmailChange = authViewModel::updateEmail,
                onPasswordChange = authViewModel::updatePassword,
                onRegisterClick = authViewModel::signUp,
                onBackToLoginClick = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Trips.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Trips.route) {
            val tripsViewModel: TripsViewModel = viewModel(
                factory = ViewModelFactory {
                    TripsViewModel(
                        authRepository = authRepository,
                        tripRepository = tripRepository
                    )
                }
            )

            val uiState by tripsViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                tripsViewModel.loadTrips()
            }

            TripsScreen(
                uiState = uiState,
                onCreateTripClick = {
                    navController.navigate(Screen.CreateTrip.route)
                },
                onTripClick = { trip ->
                    navController.navigate(
                        Screen.TripDetails.createRoute(trip.id)
                    )
                }
            )
        }

        composable(Screen.CreateTrip.route) {
            val createTripViewModel: CreateTripViewModel = viewModel(
                factory = ViewModelFactory {
                    CreateTripViewModel(
                        authRepository = authRepository,
                        tripRepository = tripRepository
                    )
                }
            )

            val uiState by createTripViewModel.uiState.collectAsState()

            CreateTripScreen(
                uiState = uiState,
                onTitleChange = createTripViewModel::updateTitle,
                onDescriptionChange = createTripViewModel::updateDescription,
                onStartDateChange = createTripViewModel::updateStartDate,
                onEndDateChange = createTripViewModel::updateEndDate,
                onCreateClick = createTripViewModel::createTrip,
                onTripCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TripDetails.route) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""

            /**
             * RouteViewModel создается для экрана поездки.
             *
             * Она отвечает за вкладку маршрута:
             * добавление, отображение и удаление точек маршрута.
             */
            val routeViewModel: RouteViewModel = viewModel(
                factory = ViewModelFactory {
                    RouteViewModel(routeRepository)
                }
            )

            val routeUiState by routeViewModel.uiState.collectAsState()

            /**
             * Загружаем точки маршрута выбранной поездки.
             *
             * LaunchedEffect(tripId) выполнится при открытии экрана
             * и при изменении id поездки.
             */
            LaunchedEffect(tripId) {
                routeViewModel.loadRoutePoints(tripId)
            }

            TripScreen(
                tripId = tripId,

                routeUiState = routeUiState,
                onRouteTitleChange = routeViewModel::updateTitle,
                onRouteAddressChange = routeViewModel::updateAddress,
                onRouteDescriptionChange = routeViewModel::updateDescription,
                onRouteLatitudeChange = routeViewModel::updateLatitude,
                onRouteLongitudeChange = routeViewModel::updateLongitude,
                onAddRoutePointClick = {
                    routeViewModel.addRoutePoint(tripId)
                },
                onDeleteRoutePointClick = { pointId ->
                    routeViewModel.deleteRoutePoint(
                        tripId = tripId,
                        pointId = pointId
                    )
                }
            )
        }
    }
}