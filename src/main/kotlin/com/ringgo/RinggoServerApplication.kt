package com.ringgo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class RinggoServerApplication

fun main(args: Array<String>) {
	runApplication<RinggoServerApplication>(*args)
}
