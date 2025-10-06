package fr.marstech.mtlinkspray.conf

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor

@Configuration
class ValidationConfig {

    @Bean
    fun validator(): LocalValidatorFactoryBean = LocalValidatorFactoryBean()

    @Bean
    fun methodValidationPostProcessor(validator: LocalValidatorFactoryBean): MethodValidationPostProcessor {
        val processor = MethodValidationPostProcessor()
        processor.setValidator(validator)
        processor.setAdaptConstraintViolations(true)
        return processor
    }
}