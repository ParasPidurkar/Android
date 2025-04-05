// Base class
open class Player {
    var name: String
    var age: Int
    var health: Int

    // Primary constructor
    constructor(name: String, age: Int, health: Int) {
        this.name = name
        this.age = age
        this.health = health
        println("My name is $name, $age years old and earning $health per month. ")
    }

    // Secondary constructor for Player with only name and age (health default to 100)
    constructor(name: String, age: Int) : this(name, age, 100) {
        println("Using secondary constructor with default health value: 100")
    }
}

// Derived class
class Duelist : Player {
    constructor(name: String, age: Int, health: Int) : super(name, age, health)  // Calls primary constructor
    constructor(name: String, age: Int) : super(name, age)  // Calls secondary constructor

    fun duel() {
        println("I am duelist")
        println()
    }
}

// Derived class
class Sentinal(name: String, age: Int, health: Int) : Player(name, age, health) {
    fun setup() {
        println("I am sentinal let me setup traps")
        println()
    }
}

// Derived class
class Controller(name: String, age: Int, health: Int) : Player(name, age, health) {
    fun smoke() {
        println("I am controller let me smoke")
        println()
    }
}

// Main method
fun main() {
    val Jett = Duelist("Jett", 25, 10000)
    Jett.duel()

    val Cypher = Sentinal("Cypher", 24, 12000)
    Cypher.setup()

    val Brim = Controller("Brimstone", 30, 15000)
    Brim.smoke()

    // Using secondary constructor with default health value
    val reyna = Duelist("reyna", 28)  // Default health will be 100
    reyna.duel()
}
