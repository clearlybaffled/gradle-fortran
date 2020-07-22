package io.github.clearlybaffled.gradle.language

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

import io.github.clearlybaffled.gradle.nativeplatform.fixtures.app.HelloWorldApp


abstract class AbstractNativeLanguageIntegrationTest extends AbstractIntegrationSpec {

    abstract HelloWorldApp getApp()

    def setup() {
        buildFile << app.pluginScript
    }

    def "compile and link executable"() {
        given:
        buildFile << """
model {
	toolChains {
		gfort(GFortran)
	}
    components {
        main(NativeExecutableSpec)
    }
}"""

        and:
        app.writeSources(file("src/main"))

        when:
        run "mainExecutable"

        then:
        def mainExecutable = executable("build/exe/main/main")
        mainExecutable.assertExists()
        mainExecutable.exec().out == app.englishOutput
    }

    def "build executable with custom compiler arg"() {
        given:
        buildFile << """
model {
	toolChains {
		gfort(GFortran)
	}
    components {
        main(NativeExecutableSpec) {
            binaries.all {
                ${app.compilerArgs("-DFRENCH")}
            }
        }
    }
}"""

        and:
        app.writeSources(file("src/main"))

        when:
        run "mainExecutable"

        then:
        def mainExecutable = executable("build/exe/main/main")
        mainExecutable.assertExists()
        mainExecutable.exec().out == app.frenchOutput
    }

    def "build executable with macro defined"() {
        given:
        buildFile << """
model {
	toolChains {
		gfort(GFortran)
	}
    components {
        main(NativeExecutableSpec) {
            binaries.all {
                ${app.compilerDefine("FRENCH")}
            }
        }
    }
}"""

        and:
        app.writeSources(file("src/main"))

        when:
        run "mainExecutable"

        then:
        def mainExecutable = executable("build/exe/main/main")
        mainExecutable.assertExists()
        mainExecutable.exec().out == app.frenchOutput
    }
}
