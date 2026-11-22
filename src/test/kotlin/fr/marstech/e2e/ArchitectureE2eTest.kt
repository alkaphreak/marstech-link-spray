package fr.marstech.e2e

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

/** The suite must survive the hexagonal migration unchanged: no production class but the entry point. */
internal class ArchitectureE2eTest {

    @Test
    fun shouldOnlyDependOnTheApplicationEntryPoint() {
        val e2e = ClassFileImporter()
            .withImportOption(ImportOption.OnlyIncludeTests())
            .importPackages("fr.marstech.e2e")

        noClasses().that().resideInAPackage("fr.marstech.e2e..")
            .should().dependOnClassesThat(
                JavaClass.Predicates.resideInAPackage("fr.marstech.mtlinkspray..")
                    .and(JavaClass.Predicates.simpleName("MtLinkSprayApplication").negate())
            )
            .check(e2e)
    }
}
