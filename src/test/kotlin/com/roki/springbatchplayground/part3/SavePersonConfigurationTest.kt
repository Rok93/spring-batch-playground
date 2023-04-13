package com.roki.springbatchplayground.part3

import com.roki.springbatchplayground.TestConfiguration
import com.roki.springbatchplayground.domain.PersonRepository
import com.roki.springbatchplayground.step3.config.SavePersonConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.StepExecution
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

@SpringBatchTest
@ContextConfiguration(classes = [SavePersonConfiguration::class, TestConfiguration::class])
class SavePersonConfigurationTest {

    @Autowired
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var personRepository: PersonRepository

    @AfterEach
    internal fun tearDown() {
        personRepository.deleteAllInBatch()
    }


    /**
     * Job Test
     */
    @Test
    internal fun testAllowDuplicate() {
        // given
        val jobParameters = JobParametersBuilder()
            .addString("allowDuplicate", "false")
            .toJobParameters()

        // when
        // SavePersonConfiguration에서 실행된 Job이 launchJob 메서드로인해 실행이 되고, 그 실행 결과를 JobExecution으로 받을 수 있다.
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        val totalWriteCount = jobExecution.stepExecutions
            .map(StepExecution::getWriteCount)
            .sum()

        assertThat(totalWriteCount)
            .isEqualTo(personRepository.count())
            .isEqualTo(3)
    }

    @Test
    internal fun testNotAllowDuplicate() {
        // given
        val jobParameters = JobParametersBuilder()
            .addString("allowDuplicate", "true")
            .toJobParameters()

        // when
        // SavePersonConfiguration에서 실행된 Job이 launchJob 메서드로인해 실행이 되고, 그 실행 결과를 JobExecution으로 받을 수 있다.
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // then
        val totalWriteCount = jobExecution.stepExecutions
            .map(StepExecution::getWriteCount)
            .sum()

        assertThat(totalWriteCount)
            .isEqualTo(personRepository.count())
            .isEqualTo(100)
    }

    /**
     * Step Test
     *
     * ScopeNotActiveException 발생!
     * Spring에 Scope가 제대로 동작하지 않는다는 뜻 (@SpringBatchTest 애너테이션이 있어야 한다!)
     *
     * 추가적으로 @SpringBootTest 대신에 @SpringBatchTest를 써야하는 것으로 파악된다. (둘 다, 같이 선언하면 에러남)
     */
    @Test
    internal fun testStep() {
        // 동일하게 jobParameters를 받을 수 있음!
        val jobExecution = jobLauncherTestUtils.launchStep("savePersonStep")

        assertThat(jobExecution.stepExecutions
            .map(StepExecution::getWriteCount)
            .sum()
        )
            .isEqualTo(personRepository.count())
            .isEqualTo(3)
    }
}
