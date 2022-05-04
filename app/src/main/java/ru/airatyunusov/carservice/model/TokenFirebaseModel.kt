package ru.airatyunusov.carservice.model

import android.os.Parcel
import android.os.Parcelable
import ru.airatyunusov.carservice.DateTimeHelper

data class TokenFirebaseModel(
    var id: String = "",
    var userId: String = "",
    var branchId: String = "",
    var carId: String = "",
    var startRecordDateTime: String = "",
    var endRecordDateTime: String = "",
    val idEmployee: String = "",
    var listServices: List<ServiceModel> = emptyList()
) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        TODO("listServices")
    ) {
    }

    override fun toString(): String {
        return "$startRecordDateTime - $endRecordDateTime"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(branchId)
        parcel.writeString(carId)
        parcel.writeString(startRecordDateTime)
        parcel.writeString(endRecordDateTime)
        parcel.writeString(idEmployee)
    }

    override fun describeContents(): Int {
        return 0
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
