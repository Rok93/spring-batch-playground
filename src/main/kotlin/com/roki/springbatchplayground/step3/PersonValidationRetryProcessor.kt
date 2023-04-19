package com.roki.springbatchplayground.step3

import com.roki.springbatchplayground.domain.Person
import com.roki.springbatchplayground.exception.NotFoundNameException
import mu.KotlinLogging
import org.springframework.batch.item.ItemProcessor
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.support.RetryTemplate
import org.springframework.retry.support.RetryTemplateBuilder

class PersonValidationRetryProcessor : ItemProcessor<Person, Person> {
    private val retryTemplate: RetryTemplate = RetryTemplateBuilder()
        .maxAttempts(3) // retryLimit 메서드와 유사
        .retryOn(NotFoundNameException::class.java) // retry 메서드와 유사
        .withListener(SavePersonRetryListener())
        .build()

    override fun process(item: Person): Person? {
        return this.retryTemplate.execute<Person, NotFoundNameException>(
            {
                // RetryCallback
                // (1) 3번까지는 재실행
                if (item.isNotEmptyName()) {
                    return@execute item
                }
                throw NotFoundNameException()
            },
            {
                // RecoveryCallback
                // (2) 3번 재실행했음에도 실패하면 RecoveryCallback 실행!
                return@execute item.unknownName()
            }
        )
    }
}

class SavePersonRetryListener: RetryListener {
    private val logger = KotlinLogging.logger { }
    /**
     * retry를 시작하는 설정
     * true여야 retry가 적용된다.
     */
    override fun <T : Any?, E : Throwable?> open(context: RetryContext?, callback: RetryCallback<T, E>?): Boolean {
        return true
    }

    /**
     * retry 종료후 실행
     */
    override fun <T : Any?, E : Throwable?> close(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?,
    ) {
        logger.info { "close" }
    }

    /**
     * retry 템플릿에 정의한 Exception 발생 시, 호출된다.
     */
    override fun <T : Any?, E : Throwable?> onError(
        context: RetryContext?,
        callback: RetryCallback<T, E>?,
        throwable: Throwable?,
    ) {
        logger.info { "onError" }
    }
}
