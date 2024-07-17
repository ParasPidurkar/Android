package L01_Basics

fun main() {

    for (i in 1..10) {     // Using Default step = 1
        print(" " +i)
    }
    println()

    for (i in 1..10 step 2) {   // Using step = 2
        print(" " +i)
    }
    println()

    for (i in 1..10 step 3) {   // Using step = 3
        print(" " +i)
    }
    println()


    // declaring an array using arrayOf()
    val arrayname = arrayOf(10, 20, 30, 40, 50)
    for (i in arrayname)
    {
        print(" " +i)
    }


    for (i in arrayname.indices)
    {
        print(" " +arrayname[i])
    }


    // declaring an array using arrayOf()
    val listname = listOf(10, 20, 30, 40, 50)
    for (i in listname)
    {
        print(" " +i)
    }


    for (i in listname.indices)
    {
        print(" " +listname[i])
    }

    val stringval = "Kotlin"
    for (i in stringval)
    {
        print(i)
    }

    for (i in stringval.indices)
    {
        print(stringval[i])
    }


}