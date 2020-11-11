package ru.spb.altercom.altercom2020rest.model


data class Employee(
    val employeeID: String,
    val name: String,
    val surname: String,
    val secondaryName: String,
    val email: String,
    val login: String
) {
    constructor(list: List<String>): this(list[0], list[1], list[2], list[3], list[4], list[5])
}
