package com.example.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BillRepository
import com.example.data.SavedBill
import com.example.data.SubmeterBill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SubmeterInput(
    val id: Int,
    val name: String,
    val previousReading: String = "",
    val currentReading: String = ""
)

data class CalculationResult(
    val billMonth: String,
    val billYear: Int,
    val totalAmount: Double,
    val finalConsumption: Double,
    val previousMonth: String,
    val currentMonth: String,
    val perUnitRate: Double,
    val submeters: List<SubmeterBill>,
    val totalSubmeterUnits: Double,
    val totalSubmeterAmount: Double
)

class BillViewModel(private val repository: BillRepository) : ViewModel() {

    // Input States
    private val _totalAmount = MutableStateFlow("")
    val totalAmount: StateFlow<String> = _totalAmount.asStateFlow()

    private val _finalConsumption = MutableStateFlow("")
    val finalConsumption: StateFlow<String> = _finalConsumption.asStateFlow()

    private val _previousMonth = MutableStateFlow("May")
    val previousMonth: StateFlow<String> = _previousMonth.asStateFlow()

    private val _currentMonth = MutableStateFlow("June")
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    private val _billMonth = MutableStateFlow("June")
    val billMonth: StateFlow<String> = _billMonth.asStateFlow()

    private val _billYear = MutableStateFlow("2026")
    val billYear: StateFlow<String> = _billYear.asStateFlow()

    // Submeters list (dynamic)
    private val _submeters = mutableStateListOf<SubmeterInput>()
    val submeters: List<SubmeterInput> get() = _submeters

    // Calculation result state
    private val _calculationResult = MutableStateFlow<CalculationResult?>(null)
    val calculationResult: StateFlow<CalculationResult?> = _calculationResult.asStateFlow()

    // Errors
    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    // Saved bills stream from database
    val savedBills: StateFlow<List<SavedBill>> = repository.allBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var submeterIdCounter = 0

    init {
        // Set initial bill year and months dynamically
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR).toString()
        _billYear.value = currentYear

        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val currentMonthIdx = calendar.get(Calendar.MONTH)
        _billMonth.value = months[currentMonthIdx]
        
        val prevMonthIdx = if (currentMonthIdx == 0) 11 else currentMonthIdx - 1
        _previousMonth.value = months[prevMonthIdx]
        _currentMonth.value = months[currentMonthIdx]

        // Add 2 submeters by default as in HTML code
        addSubmeter()
        addSubmeter()
    }

    fun updateTotalAmount(value: String) {
        _totalAmount.value = value
    }

    fun updateFinalConsumption(value: String) {
        _finalConsumption.value = value
    }

    fun updatePreviousMonth(value: String) {
        _previousMonth.value = value
    }

    fun updateCurrentMonth(value: String) {
        _currentMonth.value = value
    }

    fun updateBillMonth(value: String) {
        _billMonth.value = value
    }

    fun updateBillYear(value: String) {
        _billYear.value = value
    }

    fun addSubmeter() {
        submeterIdCounter++
        _submeters.add(
            SubmeterInput(
                id = submeterIdCounter,
                name = "Room $submeterIdCounter",
                previousReading = "",
                currentReading = ""
            )
        )
    }

    fun removeSubmeter(id: Int) {
        if (_submeters.size <= 2) {
            _validationError.value = "Minimum 2 submeters required"
            return
        }
        _submeters.removeAll { it.id == id }
    }

    fun updateSubmeterName(id: Int, name: String) {
        val index = _submeters.indexOfFirst { it.id == id }
        if (index != -1) {
            _submeters[index] = _submeters[index].copy(name = name)
        }
    }

    fun updateSubmeterPrevious(id: Int, value: String) {
        val index = _submeters.indexOfFirst { it.id == id }
        if (index != -1) {
            _submeters[index] = _submeters[index].copy(previousReading = value)
        }
    }

    fun updateSubmeterCurrent(id: Int, value: String) {
        val index = _submeters.indexOfFirst { it.id == id }
        if (index != -1) {
            _submeters[index] = _submeters[index].copy(currentReading = value)
        }
    }

    fun clearError() {
        _validationError.value = null
    }

    fun calculateBill() {
        _validationError.value = null

        val totalAmt = _totalAmount.value.toDoubleOrNull()
        if (totalAmt == null || totalAmt < 0) {
            _validationError.value = "Please enter a valid Total Amount Payable"
            return
        }

        val finalCons = _finalConsumption.value.toDoubleOrNull()
        if (finalCons == null || finalCons <= 0) {
            _validationError.value = "Please enter valid Final Consumption Units (greater than 0)"
            return
        }

        val parsedSubmeters = mutableListOf<SubmeterBill>()
        var totalUnits = 0.0

        for (input in _submeters) {
            val name = input.name.ifBlank { "Room ${input.id}" }
            val prev = input.previousReading.toDoubleOrNull()
            if (prev == null || prev < 0) {
                _validationError.value = "Invalid previous reading for $name"
                return
            }
            val curr = input.currentReading.toDoubleOrNull()
            if (curr == null || curr < prev) {
                _validationError.value = "Current reading must be greater than or equal to previous reading for $name"
                return
            }

            val units = curr - prev
            totalUnits += units

            parsedSubmeters.add(
                SubmeterBill(
                    name = name,
                    previousReading = prev,
                    currentReading = curr,
                    rawUnits = units,
                    finalUnits = 0.0, // adjusted later
                    calculatedAmount = 0.0 // calculated later
                )
            )
        }

        val perUnitRate = totalAmt / finalCons
        val difference = totalUnits - finalCons
        val adjustment = difference / parsedSubmeters.size

        var finalUnitTotal = 0.0
        var finalAmountTotal = 0.0

        val finalSubmeterBills = parsedSubmeters.map { item ->
            val finalUnit = item.rawUnits - adjustment
            val amount = finalUnit * perUnitRate
            finalUnitTotal += finalUnit
            finalAmountTotal += amount

            item.copy(
                finalUnits = finalUnit,
                calculatedAmount = amount
            )
        }

        val yearInt = _billYear.value.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)

        _calculationResult.value = CalculationResult(
            billMonth = _billMonth.value,
            billYear = yearInt,
            totalAmount = totalAmt,
            finalConsumption = finalCons,
            previousMonth = _previousMonth.value,
            currentMonth = _currentMonth.value,
            perUnitRate = perUnitRate,
            submeters = finalSubmeterBills,
            totalSubmeterUnits = finalUnitTotal,
            totalSubmeterAmount = finalAmountTotal
        )
    }

    fun saveBill() {
        val currentResult = _calculationResult.value
        if (currentResult == null) {
            _validationError.value = "Please calculate a bill first before saving"
            return
        }

        viewModelScope.launch {
            try {
                val billToSave = SavedBill(
                    billMonth = currentResult.billMonth,
                    billYear = currentResult.billYear,
                    totalAmount = currentResult.totalAmount,
                    finalConsumption = currentResult.finalConsumption,
                    previousMonth = currentResult.previousMonth,
                    currentMonth = currentResult.currentMonth,
                    perUnitRate = currentResult.perUnitRate,
                    submeterBills = currentResult.submeters
                )
                repository.insertBill(billToSave)
            } catch (e: Exception) {
                _validationError.value = "Failed to save bill: ${e.localizedMessage}"
            }
        }
    }

    fun deleteBill(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteBillById(id)
            } catch (e: Exception) {
                _validationError.value = "Failed to delete bill: ${e.localizedMessage}"
            }
        }
    }

    fun openBill(bill: SavedBill) {
        // Load the saved bill data into the UI
        _totalAmount.value = bill.totalAmount.toString()
        _finalConsumption.value = bill.finalConsumption.toString()
        _previousMonth.value = bill.previousMonth
        _currentMonth.value = bill.currentMonth
        _billMonth.value = bill.billMonth
        _billYear.value = bill.billYear.toString()

        // Populate submeters
        _submeters.clear()
        bill.submeterBills.forEachIndexed { index, submeter ->
            _submeters.add(
                SubmeterInput(
                    id = index + 1,
                    name = submeter.name,
                    previousReading = submeter.previousReading.toString(),
                    currentReading = submeter.currentReading.toString()
                )
            )
        }
        submeterIdCounter = bill.submeterBills.size

        // Set the calculation result directly
        _calculationResult.value = CalculationResult(
            billMonth = bill.billMonth,
            billYear = bill.billYear,
            totalAmount = bill.totalAmount,
            finalConsumption = bill.finalConsumption,
            previousMonth = bill.previousMonth,
            currentMonth = bill.currentMonth,
            perUnitRate = bill.perUnitRate,
            submeters = bill.submeterBills,
            totalSubmeterUnits = bill.submeterBills.sumOf { it.finalUnits },
            totalSubmeterAmount = bill.submeterBills.sumOf { it.calculatedAmount }
        )
    }
}

class BillViewModelFactory(private val repository: BillRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
