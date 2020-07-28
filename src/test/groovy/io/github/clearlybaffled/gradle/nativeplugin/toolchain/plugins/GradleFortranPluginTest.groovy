/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package io.github.clearlybaffled.gradle.nativeplugin.toolchain.plugins

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import spock.lang.Specification

/**
 * A simple unit test for the 'io.github.clearlybaffled.fortran' plugin.
*/ 
public class GradleFortranPluginTest extends Specification {
    def "plugin registers task"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply("io.github.clearlybaffled.fortran")

        then:
        project.tasks.findByName("greeting") == null
    }
}
