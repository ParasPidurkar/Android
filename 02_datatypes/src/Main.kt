//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    println("Hello, " + name + "!")
    // Declare my weapons (variables)
    var myRifle = 35  // My primary weapon ammo count

// Declare my ultimate ability (Long integer)
    var myUltimate = 23L  // Ultimate points

// Print my weapon stats
    println("My rifle ammo: ${myRifle}")
    println("My ultimate ability points: ${myUltimate}")

// Declare my utility items (Bytes)  8bits
    var smallGrenade: Byte = Byte.MIN_VALUE  // Smallest grenade value
    var largeGrenade: Byte = Byte.MAX_VALUE  // Largest grenade value

// Print my utility item stats
    println("Smallest grenade value: " + smallGrenade)
    println("Largest grenade value: " + largeGrenade)

// Declare my armor stats (Short values)  16 bits
    var lightArmor: Short = Short.MIN_VALUE  // Light armor value
    var heavyArmor: Short = Short.MAX_VALUE  // Heavy armor value

// Print armor stats
    println("Smallest light armor value: " + lightArmor)
    println("Largest heavy armor value: " + heavyArmor)

// Declare my health pool (Int values)  32 bits
    var lowHealth: Int = Int.MIN_VALUE  // Low health value
    var fullHealth: Int = Int.MAX_VALUE  // Full health value

// Print health stats
    println("Smallest health value: " + lowHealth)
    println("Largest health value: " + fullHealth)

// Declare my ultimate charge (Long values) 64 bits
    var lowUltimate: Long = Long.MIN_VALUE  // Minimum ultimate charge
    var fullUltimate: Long = Long.MAX_VALUE  // Full ultimate charge

// Print ultimate charge stats
    println("Smallest ultimate charge value: " + lowUltimate)
    println("Largest ultimate charge value: " + fullUltimate)





    //Floating

    // Declare my abilities (variables)
    var mySpike = 54F  // My spike (float) value to defuse
    println("My spike value ${mySpike}")  // Printing my spike value

// Declare my ultimate ability (Float)
    var F1: Float = Float.MIN_VALUE  // Smallest value for my ultimate float
    var F2: Float = Float.MAX_VALUE  // Largest value for my ultimate float
    println("Smallest ultimate float value: " + F1)
    println("Largest ultimate float value: " + F2)

// Declare my teammate's skills (Double values)
    var D1: Double = Double.MIN_VALUE  // Smallest value for double skill
    var D2: Double = Double.MAX_VALUE  // Largest value for double skill
    println("Smallest double skill value: " + D1)
    println("Largest double skill value: " + D2)



    //Boolean

    if (true is Boolean){
        print("Yes,valorant is the best game")
    }

    //Char

    var grade: Char = 'A'
    println("game grade is  : ${grade }")



    //String
    val BrimStone_ult_dia: String = "Open up the SKY !!!"
    println(BrimStone_ult_dia)


    val favAgent ="cypher"
    println("My favourite agent is $favAgent")
    println("Cypher has "+favAgent.length+" letters")
    println(favAgent.capitalize()+ " ult says \"No one can hide from me , Let's go  \" ")

}