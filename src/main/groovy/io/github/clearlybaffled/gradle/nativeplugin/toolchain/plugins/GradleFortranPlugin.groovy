package io.github.clearlybaffled.gradle.nativeplugin.toolchain.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin


public class GradleFortranPlugin implements Plugin<Project> {
    public void apply(Project project) {
        // Register a task
        project.tasks.register("greeting") {
            doLast {
                println("Hello from plugin 'io.github.clearlybaffled.gradle-fortran-plugin.greeting'")
            }
        }
    }
}
