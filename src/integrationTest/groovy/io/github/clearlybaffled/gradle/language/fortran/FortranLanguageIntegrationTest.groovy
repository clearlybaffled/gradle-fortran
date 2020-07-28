package io.github.clearlybaffled.gradle.language.fortran

import io.github.clearlybaffled.gradle.language.AbstractNativeLanguageIntegrationTest
import io.github.clearlybaffled.gradle.nativeplatform.fixtures.app.FortranHelloWorldApp
import io.github.clearlybaffled.gradle.nativeplatform.fixtures.app.HelloWorldApp
import spock.lang.Ignore


@Ignore
class FortranLanguageIntegrationTest extends AbstractNativeLanguageIntegrationTest {
	
	HelloWorldApp app = new FortranHelloWorldApp()
	@Ignore
	def "sources are compiled with Fortran compiler"() {
		given:
		app.writeSources(file('src/main'))
        withStacktrace()
        withDebugLogging()
		

		and:
		buildFile << """
            model {
                components {
                    main(NativeExecutableSpec)
                }
            }
         """

		expect:
		succeeds "mainExecutable"
		executable("build/exe/main/main").exec().out == app.englishOutput
	}
}
