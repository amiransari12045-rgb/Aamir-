package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class SubmeterBill(
    val name: String,
    val previousReading: Double,
    val currentReading: Double,
    val rawUnits: Double,
    val finalUnits: Double,
    val calculatedAmount: Double
)

@Entity(tableName = "saved_bills")
data class SavedBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billMonth: String,
    val billYear: Int,
    val totalAmount: Double,
    val finalConsumption: Double,
    val previousMonth: String,
    val currentMonth: String,
    val perUnitRate: Double,
    val submeterBills: List<SubmeterBill>,
    val createdAt: Long = System.currentTimeMillis()
)

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val listType = Types.newParameterizedType(List::class.java, SubmeterBill::class.java)
    private val adapter = moshi.adapter<List<SubmeterBill>>(listType)

    @TypeConverter
    fun fromSubmeterList(list: List<SubmeterBill>?): String {
        return adapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toSubmeterList(json: String?): List<SubmeterBill> {
        if (json.isNullOrEmpty()) return emptyList()
        return adapter.fromJson(json) ?: emptyList()
    }
}
