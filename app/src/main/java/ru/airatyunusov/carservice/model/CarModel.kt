package ru.airatyunusov.carservice.model

import android.os.Parcel
import android.os.Parcelable

data class CarModel(
    var id: String = "",
    val userId: String = "",
    val make: String = "",//марка
    val model: String = "",
    val numberCar: String = "",
    val year: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(make)
        parcel.writeString(model)
        parcel.writeString(numberCar)
        parcel.writeInt(year)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CarModel> {
        override fun createFromParcel(parcel: Parcel): CarModel {
            return CarModel(parcel)
        }

        override fun newArray(size: Int): Array<CarModel?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "$make $model $numberCar"
    }
}
