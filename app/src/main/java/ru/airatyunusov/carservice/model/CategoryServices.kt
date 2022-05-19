package ru.airatyunusov.carservice.model

import android.os.Parcel
import android.os.Parcelable

data class CategoryServices(
    val id: String = "",
    val name: String = "",
    val adminId: String = "",
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun toString(): String {
        return name
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(adminId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CategoryServices> {
        override fun createFromParcel(parcel: Parcel): CategoryServices {
            return CategoryServices(parcel)
        }

        override fun newArray(size: Int): Array<CategoryServices?> {
            return arrayOfNulls(size)
        }
    }
}
