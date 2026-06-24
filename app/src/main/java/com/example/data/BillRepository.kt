package com.example.data

import kotlinx.coroutines.flow.Flow

class BillRepository(private val savedBillDao: SavedBillDao) {
    val allBills: Flow<List<SavedBill>> = savedBillDao.getAllBills()

    suspend fun insertBill(bill: SavedBill): Long {
        return savedBillDao.insertBill(bill)
    }

    suspend fun deleteBillById(id: Long) {
        savedBillDao.deleteBillById(id)
    }
}
