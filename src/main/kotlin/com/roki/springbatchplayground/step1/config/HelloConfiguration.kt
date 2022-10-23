package com.roki.springbatchplayground.step1.config

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HelloConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun helloJob(): Job {
        // 아래의 job은 실행단위를 구분할 수 있는 incrementer와 job의 이름, 그리고 step을 설정함.
        return jobBuilderFactory["helloJob"] // job 이름: helloJob /  'name'은 Spring Batch를 실행할 수 있는 Key이기도 하다.
            .incrementer(RunIdIncrementer()) // 항상 job이 실행될 때마다 parameter id를 자동으로 생성해주는 클래
            .start(this.helloStep()) // Job 실행 시, 최초로 실행될 Step을 설정하는 메서드
            .build()
    }

//    @StepScope
    @Bean
    fun helloStep(): Step { // Step: Job의 실행단위 하나의 Job은 1개 이상의 Step을 가질 수 있다.
        return stepBuilderFactory["helloStep"] // step도 name 설정이 필요하다.
            .tasklet { contribution, chunkContext -> // tasklet이라는 step의 실행 단위를 지정해야한다.
                logger.info("hello spring batch")
                RepeatStatus.FINISHED
            }
            .build()
        // step의 실행 단위는 1. tasklet, 2. chunk 두 가지가 있다.
    }
}
