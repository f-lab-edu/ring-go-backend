package com.ringgo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RinggoServerApplication

fun main(args: Array<String>) {
	runApplication<RinggoServerApplication>(*args)
}
