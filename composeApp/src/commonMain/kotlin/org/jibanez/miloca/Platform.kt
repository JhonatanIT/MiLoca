package org.jibanez.miloca

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform