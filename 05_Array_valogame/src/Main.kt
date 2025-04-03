//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val duelists = arrayOf<String>("Jett","Yoru","reyna","Iso","Waylay","Pheonix")

    for (i in duelists.indices) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("${i} :  = "+duelists[i])


    }

    println("My favourite is "+duelists.get(5))
}