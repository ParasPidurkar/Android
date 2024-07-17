package L01_Basics

//// base class
//open class Animal {
//    open fun run() {
//        println("Animals can run")
//    }
//}
//// derived class
//class Tiger: Animal() {
//    override fun run() {       // overrides the run method of base class
//        println("Tiger can run very fast")
//    }
//}
//fun main(args: Array<String>) {
//    val t = Tiger()
//    t.run()
//}


// base class
open class Animal {
    open var name: String = "Dog"
    open var speed = "40 km/hr"

}
// derived class
class Tiger: Animal() {
    override var name = "Tiger"
    override var speed = "100 km/hr"
}
fun main(args: Array<String>) {
    val t = Tiger()
    println(t.name+" can run at speed "+t.speed)
}
