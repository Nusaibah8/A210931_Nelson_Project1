package com.example.a210931_nelson_project1

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReportUiState(
    val category: String = "",
    val location: String = "",
    val details: String = ""
)

data class ReportItem(
    val category: String,
    val location: String,
    val details: String,
    val status: String = "Pending"
)

class ReportViewModel : ViewModel() {

    // 1. Form State (Untuk proses membuat report baru)
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    // 2. History List State (Untuk simpan laporan yang dah disubmit)
    private val _reportHistory = MutableStateFlow<List<ReportItem>>(emptyList())
    val reportHistory: StateFlow<List<ReportItem>> = _reportHistory.asStateFlow()

    // 3. Profile Data State (Supaya tak hilang bila skrin rotate)
    private val _userName = MutableStateFlow("Nusaibah (A210931)")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPhone = MutableStateFlow("+60 12-345 6789")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userEmail = MutableStateFlow("nusaibahabkr@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // -- FUNCTIONS --
    fun updateProfile(name: String, phone: String, email: String) {
        _userName.value = name
        _userPhone.value = phone
        _userEmail.value = email
    }

    fun updateCategory(category: String) { _uiState.value = _uiState.value.copy(category = category) }
    fun updateLocation(location: String) { _uiState.value = _uiState.value.copy(location = location) }
    fun updateDetails(details: String) { _uiState.value = _uiState.value.copy(details = details) }

    fun submitReport() {
        val current = _uiState.value
        val newItem = ReportItem(current.category, current.location, current.details)
        _reportHistory.value = _reportHistory.value + newItem
        resetReport()
    }

    fun resetReport() { _uiState.value = ReportUiState() }
}