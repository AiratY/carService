package ru.airatyunusov.carservice.model

import android.os.Parcel
import android.os.Parcelable

data class Employee(
    var id: String = "",
    val branchId: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var patronymic: String = "",
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun toString(): String {
        return "$lastName ${firstName[0]}.${patronymic[0]}."
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(branchId)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(patronymic)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Employee> {
        override fun createFromParcel(parcel: Parcel): Employee {
            return Employee(parcel)
        }

        override fun newArray(size: Int): Array<Employee?> {
            return arrayOfNulls(size)
        }
    }
}
