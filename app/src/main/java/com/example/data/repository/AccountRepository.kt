package com.example.data.repository

import com.example.data.db.AccountDao
import com.example.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {

    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    suspend fun insert(account: AccountEntity): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun deleteById(id: Int) {
        accountDao.deleteAccountById(id)
    }

    suspend fun clearAll() {
        accountDao.clearAllAccounts()
    }
}
