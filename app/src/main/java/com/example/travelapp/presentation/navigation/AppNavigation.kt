package com.example.travelapp.presentation.navigation

import com.example.travelapp.data.repository.impl.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.travelapp.presentation.profile.ProfileScreen
import com.example.travelapp.presentation.profile.ProfileViewModel
import com.example.travelapp.data.repository.impl.FirebaseNotificationRepository
import com.example.travelapp.presentation.notifications.NotificationsScreen
import com.example.travelapp.presentation.notifications.NotificationsViewModel
import com.example.travelapp.data.repository.impl.FirebaseParticipantRepository
import com.example.travelapp.presentation.participants.ParticipantsViewModel
import com.example.travelapp.data.repository.impl.FirebaseExpenseRepository
import com.example.travelapp.presentation.budget.BudgetViewModel
import com.example.travelapp.data.repository.impl.FirebaseRouteRepository
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
import com.example.travelapp.data.repository.impl.FirebaseTripRepository
import com.example.travelapp.presentation.auth.AuthViewModel
import com.example.travelapp.presentation.auth.LoginScreen
import com.example.travelapp.presentation.auth.RegisterScreen
import com.example.travelapp.presentation.trip.TripScreen
import com.example.travelapp.presentation.trips.CreateTripScreen
import com.example.travelapp.presentation.trips.CreateTripViewModel
import com.example.travelapp.presentation.trips.TripsScreen
import com.example.travelapp.presentation.trips.TripsViewModel
import com.example.travelapp.data.repository.impl.FirebaseInvitationRepository
import com.example.travelapp.presentation.invitations.InvitationsScreen
import com.example.travelapp.presentation.invitations.InvitationsViewModel
import com.example.travelapp.presentation.trip.TripViewModel
import com.example.travelapp.data.repository.impl.YandexPlaceSearchRepository
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

    val authRepository = remember {
        FirebaseAuthRepository(
            firebaseAuth = FirebaseAuth.getInstance(),
            firestore = FirebaseFirestore.getInstance()
        )
    }
    val tripRepository = remember {
        FirebaseTripRepository(
            firestore = FirebaseFirestore.getInstance()
        )
    }
    val routeRepository = remember {
        FirebaseRouteRepository(
            firestore = FirebaseFirestore.getInstance()
        )
    }
    val expenseRepository = remember {
        FirebaseExpenseRepository(
            firestore = FirebaseFirestore.getInstance()
        )
    }
    val participantRepository = remember {
        FirebaseParticipantRepository(
            firestore = FirebaseFirestore.getInstance()
        )
    }
    val notificationRepository = remember {
        FirebaseNotificationRepository(
            firestore = FirebaseFirestore.getInstance()
        )
    }
    val invitationRepository = remember {
        FirebaseInvitationRepository(
            firestore = FirebaseFirestore.getInstance()
        )
    }
    val placeSearchRepository = remember {
        YandexPlaceSearchRepository()
    }

        NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Invitations.route) {
            val invitationsViewModel: InvitationsViewModel = viewModel(
                factory = ViewModelFactory {
                    InvitationsViewModel(
                        authRepository = authRepository,
                        invitationRepository = invitationRepository
                    )
                }
            )

            val uiState by invitationsViewModel.uiState.collectAsState()

            /**
             * Загружаем входящие приглашения при открытии экрана.
             */
            LaunchedEffect(Unit) {
                invitationsViewModel.loadInvitations()
            }

            InvitationsScreen(
                uiState = uiState,
                onBackClick = {
                    navController.popBackStack()
                },
                onAcceptClick = invitationsViewModel::acceptInvitation,
                onDeclineClick = invitationsViewModel::declineInvitation
            )
        }
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
                onConfirmPasswordChange = authViewModel::updateConfirmPassword,
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
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onInvitationsClick = {
                    navController.navigate(Screen.Invitations.route)
                },
                onTripClick = { trip ->
                    navController.navigate(
                        Screen.TripDetails.createRoute(trip.id)
                    )
                }
            )
        }

        composable(Screen.Notifications.route) {
            val notificationsViewModel: NotificationsViewModel = viewModel(
                factory = ViewModelFactory {
                    NotificationsViewModel(
                        authRepository = authRepository,
                        notificationRepository = notificationRepository
                    )
                }
            )

            val uiState by notificationsViewModel.uiState.collectAsState()

            /**
             * Загружаем уведомления при открытии экрана.
             */
            LaunchedEffect(Unit) {
                notificationsViewModel.loadNotifications()
            }

            NotificationsScreen(
                uiState = uiState,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ViewModelFactory {
                    ProfileViewModel(authRepository)
                }
            )

            val uiState by profileViewModel.uiState.collectAsState()

            /**
             * Загружаем профиль при открытии экрана.
             */
            LaunchedEffect(Unit) {
                profileViewModel.loadProfile()
            }

            ProfileScreen(
                uiState = uiState,
                onSignOutClick = profileViewModel::signOut,
                onBackClick = {
                    navController.popBackStack()
                },
                onSignedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
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
                onBackClick = {
                    navController.popBackStack()
                },
                onTripCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TripDetails.route) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""

            /**
             * ViewModel маршрута.
             */
            val routeViewModel: RouteViewModel = viewModel(
                factory = ViewModelFactory {
                    RouteViewModel(
                        routeRepository = routeRepository,
                        authRepository = authRepository,
                        placeSearchRepository = placeSearchRepository,
                        notificationRepository = notificationRepository
                    )
                }
            )

            val routeUiState by routeViewModel.uiState.collectAsState()

            val tripViewModel: TripViewModel = viewModel(
                factory = ViewModelFactory {
                    TripViewModel(tripRepository)
                }
            )

            val tripUiState by tripViewModel.uiState.collectAsState()
            /**
             * ViewModel бюджета.
             */
            val budgetViewModel: BudgetViewModel = viewModel(
                factory = ViewModelFactory {
                    BudgetViewModel(
                        authRepository = authRepository,
                        expenseRepository = expenseRepository,
                        notificationRepository = notificationRepository
                    )
                }
            )

            val budgetUiState by budgetViewModel.uiState.collectAsState()

            /**
             * ViewModel участников.
             */
            val participantsViewModel: ParticipantsViewModel = viewModel(
                factory = ViewModelFactory {
                    ParticipantsViewModel(
                        participantRepository = participantRepository,
                        invitationRepository = invitationRepository,
                        authRepository = authRepository,
                        notificationRepository = notificationRepository
                    )
                }
            )

            val participantsUiState by participantsViewModel.uiState.collectAsState()

            /**
             * Загружаем данные для выбранной поездки.
             *
             * LaunchedEffect(tripId) выполнится при открытии экрана
             * и при изменении id поездки.
             */
            LaunchedEffect(tripId) {
                routeViewModel.loadRoutePoints(tripId)
                budgetViewModel.loadExpenses(tripId)
                participantsViewModel.loadParticipants(tripId)
                tripViewModel.loadTrip(tripId)
            }
            /**
             * Если поездка удалена, возвращаемся к списку поездок.
             */
            LaunchedEffect(tripUiState.isDeleted) {
                if (tripUiState.isDeleted) {
                    navController.popBackStack()
                }
            }
            TripScreen(
                tripId = tripId,
                onBackClick = {
                    navController.popBackStack()
                },

                tripTitle = tripUiState.trip?.title.orEmpty(),
                isDeletingTrip = tripUiState.isDeleting,
                tripErrorMessage = tripUiState.errorMessage,
                canDeleteTrip = participantsUiState.canInviteParticipants,
                onDeleteTripClick = {
                    tripViewModel.deleteTrip(tripId)
                },

                routeUiState = routeUiState,
                onRouteSearchQueryChange = routeViewModel::updateSearchQuery,
                onRouteSearchClick = routeViewModel::searchPlaces,
                onRoutePlaceClick = routeViewModel::selectPlace,
                onRouteDescriptionChange = routeViewModel::updateDescription,
                onAddSelectedPlaceClick = {
                    routeViewModel.addSelectedPlaceToRoute(tripId)
                },
                onMoveRoutePointUpClick = { pointId ->
                    routeViewModel.movePointUp(
                        tripId = tripId,
                        pointId = pointId
                    )
                },
                onMoveRoutePointDownClick = { pointId ->
                    routeViewModel.movePointDown(
                        tripId = tripId,
                        pointId = pointId
                    )
                },
                onDeleteRoutePointClick = { pointId ->
                    routeViewModel.deleteRoutePoint(
                        tripId = tripId,
                        pointId = pointId
                    )
                },

                budgetUiState = budgetUiState,
                onBudgetTitleChange = budgetViewModel::updateTitle,
                onBudgetCategoryChange = budgetViewModel::updateCategory,
                onBudgetAmountChange = budgetViewModel::updateAmount,
                onAddExpenseClick = {
                    budgetViewModel.addExpense(tripId)
                },
                onDeleteExpenseClick = { expenseId ->
                    budgetViewModel.deleteExpense(
                        tripId = tripId,
                        expenseId = expenseId
                    )
                },

                participantsUiState = participantsUiState,
                onParticipantEmailChange = participantsViewModel::updateEmail,
                onParticipantRoleChange = participantsViewModel::updateRole,
                onInviteParticipantClick = {
                    participantsViewModel.inviteParticipant(
                        tripId = tripId,
                        tripTitle = tripUiState.trip?.title.orEmpty()
                    )
                }
            )
        }
    }
}