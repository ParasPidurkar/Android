package L01_Basics

// base class
open class Phone() {
    var color = "Rose Gold"
    fun displayCompany(name:String) {
        println("Company is: $name")
    }
}
// derived class
class iphone: Phone() {
    fun displayColor(){

        // calling the base class property color
        println("Color is: "+super.color)

        // calling the base class member function
        super.displayCompany("Apple")
    }
}
fun main(args: Array<String>) {
    val p = iphone()
    p.displayColor()
}
