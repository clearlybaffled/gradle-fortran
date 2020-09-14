package io.github.clearlybaffled.gradle.nativeplatform

import static org.junit.Assert.*

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.junit.Test

class BinaryConfigurationIntegrationTest extends AbstractIntegrationSpec {

    def "can configure the binaries of a Fortran application"() {
        given:
        buildFile << """
plugins {
    id "io.github.clearlybaffled.fortran"
}

model {
    toolChains {
        gfort(GnuFortran)
    }
    components {
        main(NativeExecutableSpec) {
            sources { 
                fortran {
                    srcDir "src/main/fortran/*.f"
                }
            }
            binaries.all {
                fortranCompiler.define 'ENABLE_GREETING'
            }
        }
    }
}
"""

        and:
        file("src/main/fortran/helloworld.f") << """
            program hello
              #ifdef ENABLE_GREETING
              print *, "Hello!"
              #endif
           end program hello
        """
        withDebugLogging()
        withStacktrace()

        when:
        run "mainExecutable"

        then:
        def executable = executable("build/exe/main/main")
        executable.exec().out == "Hello!"
    }

}
