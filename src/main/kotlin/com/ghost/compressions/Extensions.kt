package com.ghost.compressions

fun timer(r: () -> Unit) {
    val start = System.currentTimeMillis()
    r()
    val stop = System.currentTimeMillis()

    println("Time: " + (stop - start) / 1000.0)
}
