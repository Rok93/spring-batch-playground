package com.roki.springbatchplayground.step3.config

import com.roki.springbatchplayground.domain.Person
import com.roki.springbatchplayground.exception.NotFoundNameException
import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.concurrent.ConcurrentHashMap
import javax.persistence.EntityManagerFactory

@Configuration
class SavePersonConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val entityManagerFactory: EntityManagerFactory,
) {
    private val logger = KotlinLogging.logger { }

    @Bean
    fun savePersonJob(): Job {
        return this.jobBuilderFactory["savePersonJob"]
            .incrementer(RunIdIncrementer())
            .start(savePersonStep(null))
            .listener(SaverPersonJobExecutionListener()) // listener를 여러개 등록하면 listener를 내부적으로 list에 담아서 실행함.(순서가 유지됌!)
            .listener(SavePersonAnnotationJobExecutionListener())
            .build()
    }

    @Bean
    @JobScope
    fun savePersonStep(@Value("#{jobParameters[allowDuplicate]}") allowDuplicate: String?): Step {
        return this.stepBuilderFactory["savePersonStep"]
            .chunk<Person, Person>(10)
            .reader(itemReader())
//            .processor(DuplicateValidationProcessor(Person::name, allowDuplicate.toBoolean()))
            .processor(itemProcessor(allowDuplicate))
            .writer(itemWriter())
            .listener(SavePersonStepExecutionListener())
            .faultTolerant()
            .skip(NotFoundNameException::class.java)
            .skipLimit(3) // 3번까지 허용하겠다.
            .build()
    }

    private fun itemReader(): ItemReader<out Person> {
        val lineMapper = DefaultLineMapper<Person>()
        val lineTokenizer = DelimitedLineTokenizer()

        lineTokenizer.setNames("name", "age", "address")
        lineMapper.setLineTokenizer(lineTokenizer)
        lineMapper.setFieldSetMapper {
            Person(
                name = it.readString(0),
                age = it.readString(1),
                address = it.readString(2),
            )
        }

        return FlatFileItemReaderBuilder<Person>()
            .name("savePersonItemReader")
            .encoding("UTF-8")
            .linesToSkip(1)
            .resource(ClassPathResource("person.csv"))
            .lineMapper(lineMapper)
            .build()
            .apply { afterPropertiesSet() }
    }

    private fun itemProcessor(allowDuplicate: String?): ItemProcessor<Person, Person> {
        val duplicateValidationProcessor = DuplicateValidationProcessor(Person::name, allowDuplicate.toBoolean())
        val validationProcessor = ItemProcessor<Person, Person> {
            if(it.isNotEmptyName()) {
                return@ItemProcessor it
            }

            throw NotFoundNameException()
        }

        return CompositeItemProcessorBuilder<Person, Person>()
            .delegates(validationProcessor, duplicateValidationProcessor)
            .build()
            .apply { afterPropertiesSet() }
    }

    /**
     * 두 개의 ItemWriter를 하나의 ItemWriter로 합쳐서 실행할 수 있도록 CompositeItemWriter를 만든다.
     */
    private fun itemWriter(): ItemWriter<Person> {
//        return { it.forEach { logger.info { "저는 ${it.name} 입니다." } } }
        val jpaItemWriter = JpaItemWriterBuilder<Person>()
            .entityManagerFactory(entityManagerFactory)
            .build()

        val logItemWriter = ItemWriter<Person> { items -> logger.info { "person.size : ${items.size}" } }

        return CompositeItemWriterBuilder<Person>()
            .delegates(jpaItemWriter, logItemWriter)
            .build()
            .apply { afterPropertiesSet() }
    }
}

/**
 * 확장성을 고려한 중복 검증 ItemProcessor 구현
 */
class DuplicateValidationProcessor<T>(
    private val keyExtractor: (T) -> String,
    private val allowDuplicate: Boolean,
) : ItemProcessor<T, T> {
    private val keyPool: MutableMap<String, Any> = ConcurrentHashMap()

    override fun process(item: T): T? {
        if (allowDuplicate) {
            return item
        }

        val key = keyExtractor(item)
        if (keyPool.containsKey(key)) {
            return null
        }

        keyPool[key] = key
        return item
    }
}
