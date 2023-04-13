package com.roki.springbatchplayground

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableBatchProcessing
@SpringBootApplication
class SpringBatchPlaygroundApplication

fun main(args: Array<String>) {
    runApplication<SpringBatchPlaygroundApplication>(*args)
}
