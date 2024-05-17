package dev.kikugie.stitchertest.res

fun f1() {}
fun f2() {}
fun f3() {}
fun f4() {}

fun main() {
    /*? if false {*//*
    f1()
    *///?} else {
    f2()
        /*? if false >>*/
        /*f3();*/ f4()
    f4()
    //?}
}