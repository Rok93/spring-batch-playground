package com.roki.springbatchplayground.step3.config

import mu.KotlinLogging
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeJob
import org.springframework.batch.core.annotation.BeforeStep

/**
 * 1) Interface 방식
 */
class SaverPersonJobExecutionListener: JobExecutionListener {
    private val logger = KotlinLogging.logger { }

    override fun beforeJob(jobExecution: JobExecution) {
        logger.info { "beforeJob" }
    }

    override fun afterJob(jobExecution: JobExecution) {
        val sum = jobExecution.stepExecutions
            .map(StepExecution::getWriteCount)
            .sum()
        logger.info { "afterJob : $sum" }
    }
}

/**
 * 2) 애너테이션 기반 방식
 */
class SavePersonAnnotationJobExecutionListener {
    private val logger = KotlinLogging.logger { }

    /**
     * BeforeJob 애너테이션을 사용하는, 메서드 시그니처는 interface의 시그니처와 동일하게 해야한다! (메서드명은 상관없다!)
     * 사실 함수 시그니처가 달라도 실행은 되는듯 함. (jobExecution 없이도 로그 찍힘)
     */
    @BeforeJob
    fun beforeJob(jobExecution: JobExecution) {
        logger.info { "annotationBeforeJob" }
    }

    @AfterJob
    fun afterJob(jobExecution: JobExecution) {
        val sum = jobExecution.stepExecutions
            .map(StepExecution::getWriteCount)
            .sum()
        logger.info { "annotationAfterJob : $sum" }
    }
}

class SavePersonStepExecutionListener {
    private val logger = KotlinLogging.logger { }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        logger.info { "beforeStep" }
    }

    /**
     * return type이 ExitStatus 이다.
     */
    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        logger.info { "afterStep : ${stepExecution.writeCount}" }
        if (stepExecution.writeCount == 0) { // (2) 물론 조건에 따라 내가 직접 다른 ExitStatus를 지정할 수도 있음!
            return ExitStatus.FAILED
        }

        return stepExecution.exitStatus
    // (1) springBatch는 내부적으로 step의 성공/실패 여부를 stepExeuction에 저장하므로 stepExecution에 저장된 exitStatus를 썼음.
    }
}
