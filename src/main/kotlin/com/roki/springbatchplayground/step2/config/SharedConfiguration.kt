package com.roki.springbatchplayground.step2.config

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
class SharedConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    private val logger = KotlinLogging.logger { }

    @Bean
    fun sharedJob(): Job {
        return jobBuilderFactory["shareJob"]
            .incrementer(RunIdIncrementer())
            .start(this.shareStep())
            .next(this.shareStep2())
            .build()
    }

    @Bean
    fun shareStep(): Step {
        return stepBuilderFactory["sharedStep"]
            .tasklet { contribution, chunkContext ->
                val stepExecution = contribution.stepExecution
                val stepExecutionContext = stepExecution.executionContext
                stepExecutionContext.putString("stepKey", "step execution context")

                val jobExecution = stepExecution.jobExecution
                val jobInstance = jobExecution.jobInstance
                val jobExecutionContext = jobExecution.executionContext
                jobExecutionContext.putString("jobKey", "job execution context")
                val jobParameters = jobExecution.jobParameters

                logger.info {
                    "jobName : ${jobInstance.jobName}, stepName : ${stepExecution.stepName}, parameter : ${
                    jobParameters.getLong("run.id")
                    }"
                }

                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun shareStep2(): Step {
        return stepBuilderFactory["sharedStep2"]
            .tasklet { contribution, chunkContext ->
                val stepExecution = contribution.stepExecution
                val stepExecutionContext = stepExecution.executionContext

                val jobExecution = stepExecution.jobExecution
                val jobExecutionContext = jobExecution.executionContext

                logger.info {
                    "jobKey : ${jobExecutionContext["jobKey"] ?: "emptyJobKey"}" +
                        "stepKey : ${stepExecutionContext["stepKey"] ?: "emptyStepKey"}"
                }

                RepeatStatus.FINISHED
            }
            .build()
    }
}
