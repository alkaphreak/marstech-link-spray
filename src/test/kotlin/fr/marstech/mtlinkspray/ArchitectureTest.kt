package fr.marstech.mtlinkspray

import com.tngtech.archunit.base.DescribedPredicate.alwaysTrue
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import com.tngtech.archunit.library.freeze.FreezingArchRule.freeze
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Service

/**
 * ADR 0007 rules. Frozen while MLS-276 moves domain by domain: the violations present when a rule
 * landed are recorded in `src/test/resources/archunit_store` and tolerated, any new one fails. The
 * store is read-only (`archunit.properties`), so a fixed violation also fails until the shrunk store
 * is recorded and committed. The freeze goes once the store is empty.
 */
internal class ArchitectureTest {

    private val production: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("fr.marstech.mtlinkspray")

    private fun ArchRule.checkFrozen() = freeze(this).check(production)

    @Test
    fun domainShouldNotDependOnAdaptersMongoOrServlet() {
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat(resideInAnyPackage("..adapter..", "com.mongodb..", "jakarta.servlet.."))
            .checkFrozen()
    }

    @Test
    fun domainShouldOnlyUseSpringServiceAnnotation() {
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat(resideInAPackage("org.springframework..").and(equivalentTo(Service::class.java).negate()))
            .checkFrozen()
    }

    @Test
    fun inboundAdaptersShouldNotDependOnOutboundAdapters() {
        noClasses().that().resideInAPackage("..adapter.inbound..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.outbound..")
            .checkFrozen()
    }

    @Test
    fun domainsShouldOnlyShareDomainShared() {
        slices().matching("..domain.(*)..")
            .should().notDependOnEachOther()
            .ignoreDependency(alwaysTrue(), resideInAPackage("..domain.shared.."))
            .checkFrozen()
    }

    @Test
    fun domainSharedShouldOnlyHoldContractsAndValues() {
        classes().that().resideInAPackage("..domain.shared..")
            .and().areTopLevelClasses()
            .should(beContractOrValue)
            .checkFrozen()
    }

    private val beContractOrValue = object : ArchCondition<JavaClass>("be an interface, a value type, an enum or an exception") {
        override fun check(item: JavaClass, events: ConditionEvents) {
            // Kotlin data classes generate copy(), value classes box-impl()
            val ok = item.isInterface || item.isEnum || item.isAssignableTo(Throwable::class.java) ||
                item.methods.any { it.name == "copy" || it.name == "box-impl" }
            events.add(SimpleConditionEvent(item, ok, "${item.name} is not an interface, a value type, an enum or an exception"))
        }
    }
}
