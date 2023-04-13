package com.roki.springbatchplayground.part3

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [SavePerson])
class SavePersonConfigurationTest {
}
