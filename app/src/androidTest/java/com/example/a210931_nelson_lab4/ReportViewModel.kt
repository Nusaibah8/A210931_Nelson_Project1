package com.example.a210931_nelson_project1

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// DATA CLASS: Menyimpan 3 maklumat mengikut permintaan anda
data class ReportUiState(
    val category: String = "",
    val location: String = "",
    val details: String = ""
)

// VIEWMODEL
class ReportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun updateCategory(newCategory: String) {
        _uiState.update { currentState -> currentState.copy(category = newCategory) }
    }

    fun updateLocation(newLocation: String) {
        _uiState.update { currentState -> currentState.copy(location = newLocation) }
    }

    fun updateDetails(newDetails: String) {
        _uiState.update { currentState -> currentState.copy(details = newDetails) }
    }

    fun resetReport() {
        _uiState.value = ReportUiState()
    }
}