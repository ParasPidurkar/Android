//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val valoDuelists = listOf("Pheonix","Jett","Reyna","Raze","Iso")
    println("My favorite is "+valoDuelists.get(0))
    //valoDuelists.add("waylay")

    val valoMutDuelists = mutableListOf("Pheonix","Jett","Reyna","Raze","Iso","Sage")

    valoMutDuelists.remove("Sage")
    valoMutDuelists.add("waylay")

    for (i in valoDuelists)
        print("${i}  ")
    println("")
    for (i in valoMutDuelists.indices){
        println("${i}:  "+valoMutDuelists[i])
    }

}