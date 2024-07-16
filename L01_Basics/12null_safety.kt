package L01_Basics

fun main(){
    var s1: String = "Geeks"
    // s1 = null  // will lead to compilation error

    var s2: String? = "GeeksforGeeks"
    s2 = null // ok
    println(s2)

   // val l = s2.length  //error



    var s: String? = "GeeksforGeeks"
    println(s)
    if (s != null) {
        println("String of length ${s.length}")
    }
    else {
        println("Null string")
    }
    // assign null
    s = null
    println(s)
    if (s != null) {
        println("String of length ${s.length}")
    } else {
        println("Null String")
    }


    var firstName: String? = "Paras"
    var lastName: String? = null

    println(firstName?.toUpperCase())
    println(firstName?.length)
    println(lastName?.toUpperCase())
}