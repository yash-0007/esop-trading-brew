package com.esops

import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
    println("starting server")
    build(*args).eagerInitConfiguration(true).start()
}
