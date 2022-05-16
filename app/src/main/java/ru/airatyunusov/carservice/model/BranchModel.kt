package ru.airatyunusov.carservice.model

import android.os.Parcel
import android.os.Parcelable

data class BranchModel(
    val id: String = "",
    val adminId: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val startTime: String = "",
    val endTime: String = "",
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun toString(): String {
        return "$name \n$address \nТел: $phone"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(adminId)
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(phone)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BranchModel> {
        override fun createFromParcel(parcel: Parcel): BranchModel {
            return BranchModel(parcel)
        }

        override fun newArray(size: Int): Array<BranchModel?> {
            return arrayOfNulls(size)
        }
    }
}
