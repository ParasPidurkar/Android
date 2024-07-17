package L01_Basics

fun main()
{
    // declaring an array using arrayOf()
    val arrayname = arrayOf(1, 2, 3, 4, 5)
    for (i in arrayname.indices)
    {
        println(arrayname.get(i))
    }


    val arrayname2 = Array(5, { i -> i })
    for (i in 0..arrayname2.size-1)
    {
        print(" ${arrayname2[i]}")
    }

    println(arrayname.size)
    println(arrayname.get(0));
}