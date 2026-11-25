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
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Service

/** ADR 0007 rules, checked unfrozen since MLS-276 emptied the violation store. */
internal class ArchitectureTest {

    private val production: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("fr.marstech.mtlinkspray")

    @Test
    fun domainShouldNotDependOnAdaptersOrInfrastructure() {
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat(
                resideInAnyPackage("..adapter..", "com.mongodb..", "jakarta.servlet..", "java.net.http..", "javax.naming.."),
            )
            .check(production)
    }

    @Test
    fun domainShouldOnlyUseSpringServiceAnnotation() {
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat(resideInAPackage("org.springframework..").and(equivalentTo(Service::class.java).negate()))
            .check(production)
    }

    @Test
    fun inboundAdaptersShouldNotDependOnOutboundAdapters() {
        noClasses().that().resideInAPackage("..adapter.inbound..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.outbound..")
            .check(production)
    }

    @Test
    fun domainsShouldOnlyShareDomainShared() {
        slices().matching("..domain.(*)..")
            .should().notDependOnEachOther()
            .ignoreDependency(alwaysTrue(), resideInAPackage("..domain.shared.."))
            .check(production)
    }

    @Test
    fun domainSharedShouldOnlyHoldContractsAndValues() {
        classes().that().resideInAPackage("..domain.shared..")
            .and().areTopLevelClasses()
            .should(beContractOrValue)
            .check(production)
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
