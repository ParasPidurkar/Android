plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    // Turns @Serializable route classes into something Navigation can put in the back stack
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
}
