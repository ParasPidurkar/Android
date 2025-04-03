//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    println("Enter your valorant character")
    val agentName = readLine()?.trim()?.capitalize()
    when (agentName) {
        "Jett" -> println("$agentName is a Duelist.")
        "Phoenix" -> println("$agentName is a Duelist.")
        "Raze" -> println("$agentName is a Duelist.")
        "Breach" -> println("$agentName is an Initiator.")
        "Sage" -> println("$agentName is a Sentinel.")
        "Omen" -> println("$agentName is a Controller.")
        "Viper" -> println("$agentName is a Controller.")
        "Killjoy" -> println("$agentName is a Sentinel.")
        "Brimstone" -> println("$agentName is a Controller.")
        "Cypher" -> println("$agentName is a Sentinel.")
        "Astra" -> println("$agentName is a Controller.")
        "Sova" -> println("$agentName is an Initiator.")
        "Yoru" -> println("$agentName is a Duelist.")
        "Astra" -> println("$agentName is a Controller.")
        else -> println("Unknown agent.")
    }

}