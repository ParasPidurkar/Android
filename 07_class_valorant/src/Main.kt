class Agent{
    var name : String =""
    var health : Short =0
    var native : String = ""
    var precision : Double = 0.toDouble()

    fun setname (name:String):Unit{
        this.name =name
        println("You have selected "+this.name)
    }
}

fun main() {
    var Jett = Agent()
    Jett.setname("Jett")

    var Pheonix = Agent()
    Pheonix.setname("Pheonix")
}