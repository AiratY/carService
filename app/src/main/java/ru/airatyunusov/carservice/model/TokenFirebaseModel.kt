package ru.airatyunusov.carservice.model

import android.os.Parcel
import android.os.Parcelable
import ru.airatyunusov.carservice.utils.DateTimeHelper

data class TokenFirebaseModel(
    var id: String = "",
    var userId: String = "",
    var branchId: String = "",
    var carId: String = "",
    var startRecordDateTime: String = "",
    var endRecordDateTime: String = "",
    val idEmployee: String = "",
    val hoursComplete: Long = 0L,
    var price: Long = 0,
    var listServices: List<ServiceModel> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readLong(),
        listOf<ServiceModel>().apply {
            parcel.readArrayList(ServiceModel::class.java.classLoader)
        }
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(branchId)
        parcel.writeString(carId)
        parcel.writeString(startRecordDateTime)
        parcel.writeString(endRecordDateTime)
        parcel.writeString(idEmployee)
        parcel.writeLong(hoursComplete)
        parcel.writeLong(price)
        parcel.writeTypedList(listServices)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        val start = DateTimeHelper.convertToLocalDateTime(startRecordDateTime)
        val end = DateTimeHelper.convertToLocalDateTime(endRecordDateTime)

        return "${DateTimeHelper.convertToStringMyPattern(start)}\n${DateTimeHelper.convertToStringMyPattern(end)}"
    }

    companion object CREATOR : Parcelable.Creator<TokenFirebaseModel> {
        override fun createFromParcel(parcel: Parcel): TokenFirebaseModel {
            return TokenFirebaseModel(parcel)
        }

        override fun newArray(size: Int): Array<TokenFirebaseModel?> {
            return arrayOfNulls(size)
        }
    }
}
