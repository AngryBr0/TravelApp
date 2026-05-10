package com.example.travelapp.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ViewModelFactory нужен для создания ViewModel с параметрами.
 *
 * По умолчанию Android умеет создавать только ViewModel без аргументов.
 * Но наши ViewModel принимают репозитории:
 *
 * AuthViewModel(authRepository)
 * TripsViewModel(authRepository, tripRepository)
 *
 * Поэтому мы создаем универсальную фабрику.
 */
class ViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return creator() as VM
    }
}