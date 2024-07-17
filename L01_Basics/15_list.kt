package L01_Basics

fun main() {
    val theList = listOf( 0 , 10 , 20, 30)
    println(theList)

    val theMutableList = mutableListOf( 0 , 10 , 20, 30)
    println(theMutableList)

    for (i in theList.indices) {
        println(theList[i])
    }

    println("Size of the list = " + theList.size)

    println("Element at 3rd position =  " + theList.get(2))

    theMutableList.add(40)
    theMutableList.add(50)
    println(theMutableList)

    theMutableList.remove(40)
    theMutableList.remove(50)
    println(theMutableList)
}