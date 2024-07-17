package L01_Basics

data class Book(val name: String, val publisher: String, var reviewScore: Int)

fun main() {

    val book = Book("Kotlin", "Tutorials Point", 10)

    println("Name = ${book.name}")
    println("Publisher = ${book.publisher}")
    println("Score = ${book.reviewScore}")

}
