package ru.airatyunusov.carservice.model

import android.os.Parcel
import android.os.Parcelable

data class ServiceModel(
    val id: String = "",
    val adminId: String = "",
    val name: String = "",
    val hours: Int = 0,
    val price: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(adminId)
        parcel.writeString(name)
        parcel.writeInt(hours)
        parcel.writeInt(price)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServiceModel> {
        override fun createFromParcel(parcel: Parcel): ServiceModel {
            return ServiceModel(parcel)
        }

        override fun newArray(size: Int): Array<ServiceModel?> {
            return arrayOfNulls(size)
        }
    }
}
