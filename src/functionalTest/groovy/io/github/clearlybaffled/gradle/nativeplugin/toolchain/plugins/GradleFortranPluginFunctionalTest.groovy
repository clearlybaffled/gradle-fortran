package io.github.clearlybaffled.gradle.nativeplugin.toolchain.plugins


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification


class GradleFortranPluginFunctionalTest extends Specification {

	@Rule TemporaryFolder testProjectDir = new TemporaryFolder()
	File buildFile
	File srcDir
    
    def helloworld = """
            program hello
                  print *, "Hello, World!"
            end program hello
        """.stripIndent()

	def setup() {
		buildFile = testProjectDir.newFile('build.gradle')

        testProjectDir.newFile("settings.gradle") << 'rootProject.name = "helloworld"'
 
		srcDir = testProjectDir.newFolder("src","main","f95")
	}
	
	
/*    def "builds program"() {
        given:
		buildFile << """
            plugins {
                id 'io.github.clearlybaffled.fortran'
            }
            model {
				components {
					hello(NativeExecutableSpec)
				}
			}
        """.stripIndent()

        testProjectDir.newFile("src/main/f95/hello.f95") << helloworld
        
        when:
	    
		def result = getRunner().build()
		
		
        then:
        assertThat(result.task(":assemble").outcome, org.hamcrest.Matchers.in([TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE]))
		assertThat(result.task(":build").outcome, org.hamcrest.Matchers.in([TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE]))
		
	
		
		//assertTrue(new File("${testProjectDir.root.absolutePath}/build").exists())
    }*/
    
    /*def "multi language project"() {
        given:
        buildFile << """
            plugins {
                id 'io.github.clearlybaffled.fortran'
                id 'c'
            }

            model {
                components {
                    hello(NativeExecutableSpec)
                }
            }
        """.stripIndent()
        testProjectDir.newFile("src/main/f95/hello.f95") << helloworld
        testProjectDir.newFolder('src','hello','c')
        testProjectDir.newFile("src/hello/c/helloworld.c") << '''
            #include <stdio.h>
            
            int main(int argv, char* argc) {
                printf("Hello World!\\n\");
                return 0;
            }

        '''.stripIndent()
        
        when:
        def result = getRunner().build()
        
        then:
        assertThat(result.task(":build").outcome, org.hamcrest.Matchers.in([TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE]))
        
    }
*/	
	def "builds from real filesystem"() {
		given:
		def runner = GradleRunner.create()
			.forwardOutput()
			.withPluginClasspath()
			.withArguments("--stacktrace","--debug","clean","build")
			.withDebug(true)
			.withProjectDir(new File("src/functionalTest/resources/helloworld"))
		when:
		def result
		try {
			result = runner.build()
		} catch (ex) {
			println ex.message
		}
		then:

		assertThat(result.tasks(TaskOutcome.FAILED), empty())
	}
    
    private GradleRunner getRunner() {
        return GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("--stacktrace", "--debug", "build")
                .withDebug(true)
                .withProjectDir(testProjectDir.root)
    }
	
}
