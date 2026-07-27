package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.automation.GeneratedProfile
import com.example.automation.ProfileGenerator
import com.example.data.db.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AutoFillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AccountRepository
    val currentProfile: MutableStateFlow<GeneratedProfile> = MutableStateFlow(ProfileGenerator.generate())

    val accounts: StateFlow<List<AccountEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).accountDao()
        repository = AccountRepository(dao)
        accounts = repository.allAccounts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    // Generate new profile
    fun generateNewProfile(
        preset: ProfileGenerator.PresetType = ProfileGenerator.PresetType.BANGLADESHI,
        forcedGender: String? = null,
        useEmail: Boolean = true,
        passwordMode: ProfileGenerator.PasswordMode = ProfileGenerator.PasswordMode.RANDOM_STRONG,
        customPassword: String = "Pass#2026",
        customPrefix: String = "FbUser"
    ) {
        currentProfile.value = ProfileGenerator.generate(
            preset = preset,
            forcedGender = forcedGender,
            useEmail = useEmail,
            passwordMode = passwordMode,
            customPassword = customPassword,
            customPrefix = customPrefix
        )
    }

    // Save current profile to Room DB
    fun saveCurrentProfileToDb(status: String = "Registered") {
        viewModelScope.launch {
            val prof = currentProfile.value
            val entity = AccountEntity(
                firstName = prof.firstName,
                lastName = prof.lastName,
                emailOrPhone = prof.emailOrPhone,
                password = prof.password,
                birthDay = prof.birthDay,
                birthMonth = prof.birthMonth,
                birthYear = prof.birthYear,
                gender = prof.gender,
                status = status
            )
            repository.insert(entity)
        }
    }

    fun deleteAccount(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllAccounts() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
