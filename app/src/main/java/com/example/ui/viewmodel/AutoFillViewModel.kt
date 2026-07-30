package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.automation.FieldSelectors
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
    val savedUserPassword: MutableStateFlow<String> = MutableStateFlow("FbAbrar#888")
    val currentProfile: MutableStateFlow<GeneratedProfile> = MutableStateFlow(
        ProfileGenerator.generate(
            passwordMode = ProfileGenerator.PasswordMode.CUSTOM_FIXED,
            customPassword = "FbAbrar#888"
        )
    )
    val customSelectors: MutableStateFlow<FieldSelectors> = MutableStateFlow(FieldSelectors())
    val autoSubmitEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val filledCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val isLiveSequentialActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val liveStatusText: MutableStateFlow<String> = MutableStateFlow("Ready for Live AutoFill")

    fun updateSavedPassword(newPass: String) {
        if (newPass.isNotBlank()) {
            savedUserPassword.value = newPass.trim()
            // Update current profile password as well
            currentProfile.value = currentProfile.value.copy(password = newPass.trim())
        }
    }

    fun incrementFilledCount() {
        filledCount.value = filledCount.value + 1
    }

    fun setLiveSequentialActive(active: Boolean) {
        isLiveSequentialActive.value = active
        if (active) {
            liveStatusText.value = "Live AutoFill Sequence Running..."
        } else {
            liveStatusText.value = "Live AutoFill Sequence Paused (${filledCount.value} completed)"
        }
    }

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

    fun updateCustomSelectors(
        fnSel: String? = null,
        lnSel: String? = null,
        epSel: String? = null,
        pwSel: String? = null
    ) {
        val current = customSelectors.value
        val newFn = if (!fnSel.isNullOrBlank()) listOf(fnSel.trim()) + current.firstNameSelectors else current.firstNameSelectors
        val newLn = if (!lnSel.isNullOrBlank()) listOf(lnSel.trim()) + current.lastNameSelectors else current.lastNameSelectors
        val newEp = if (!epSel.isNullOrBlank()) listOf(epSel.trim()) + current.emailPhoneSelectors else current.emailPhoneSelectors
        val newPw = if (!pwSel.isNullOrBlank()) listOf(pwSel.trim()) + current.passwordSelectors else current.passwordSelectors

        customSelectors.value = current.copy(
            firstNameSelectors = newFn.distinct(),
            lastNameSelectors = newLn.distinct(),
            emailPhoneSelectors = newEp.distinct(),
            passwordSelectors = newPw.distinct()
        )
    }

    fun toggleAutoSubmit(enabled: Boolean) {
        autoSubmitEnabled.value = enabled
    }

    // Generate new profile
    fun generateNewProfile(
        preset: ProfileGenerator.PresetType = ProfileGenerator.PresetType.BANGLADESHI,
        forcedGender: String? = null,
        useEmail: Boolean = true,
        passwordMode: ProfileGenerator.PasswordMode = ProfileGenerator.PasswordMode.CUSTOM_FIXED,
        customPassword: String? = null,
        customPrefix: String = "FbUser"
    ) {
        val passToUse = customPassword ?: savedUserPassword.value
        currentProfile.value = ProfileGenerator.generate(
            preset = preset,
            forcedGender = forcedGender,
            useEmail = useEmail,
            passwordMode = passwordMode,
            customPassword = passToUse,
            customPrefix = customPrefix
        )
    }

    // Save account with UID, Password, and Cookie
    fun saveAccount(
        uid: String,
        password: String,
        cookie: String,
        firstName: String = "",
        lastName: String = ""
    ) {
        viewModelScope.launch {
            val prof = currentProfile.value
            val entity = AccountEntity(
                uid = uid,
                emailOrPhone = if (uid.isNotBlank()) uid else prof.emailOrPhone,
                password = if (password.isNotBlank()) password else savedUserPassword.value,
                cookie = cookie,
                firstName = if (firstName.isNotBlank()) firstName else prof.firstName,
                lastName = if (lastName.isNotBlank()) lastName else prof.lastName,
                birthDay = prof.birthDay,
                birthMonth = prof.birthMonth,
                birthYear = prof.birthYear,
                gender = prof.gender,
                status = "Saved"
            )
            repository.insert(entity)
        }
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
