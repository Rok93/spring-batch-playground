package com.roki.springbatchplayground.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "person")
class Person(
    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "age", nullable = false)
    val age: String,

    @Column(name = "address", nullable = false)
    val address: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun isNotEmptyName(): Boolean {
        return name.isNotEmpty()
    }

    fun unknownName(): Person {
        return Person(
            name = "UNKNOWN", age = age, address = address
        )
    }
}
