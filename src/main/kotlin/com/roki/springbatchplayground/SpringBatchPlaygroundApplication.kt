package com.roki.springbatchplayground

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableBatchProcessing
@SpringBootApplication
class SpringBatchPlaygroundApplication

fun main(args: Array<String>) {
    runApplication<SpringBatchPlaygroundApplication>(*args)
}
