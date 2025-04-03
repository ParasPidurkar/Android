/*You're building a game where a player can enter a dungeon. However, there are certain conditions for entering.
 The player must satisfy certain criteria like having enough health, having a weapon, and completing a previous quest.
  You will use logical operators (&&, ||, !) to determine if the player is eligible to enter the dungeon.*/
fun main() {
    var playerHealth =85;
    var playerHasWeapon ="Axe"
    val playerQuestCompleted = true

    if (playerHealth>50 && (playerHasWeapon.length >1)|| playerQuestCompleted == true )
        println("Bring it on ")

}