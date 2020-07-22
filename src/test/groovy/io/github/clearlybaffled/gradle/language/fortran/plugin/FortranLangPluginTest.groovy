package io.github.clearlybaffled.gradle.language.fortran.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.language.base.ProjectSourceSet
import org.gradle.model.ModelMap
import org.gradle.model.internal.core.rule.describe.SimpleModelRuleDescriptor
import org.gradle.model.internal.registry.RuleContext
import org.gradle.model.internal.type.ModelType
import org.gradle.model.internal.type.ModelTypes
import org.gradle.nativeplatform.NativeBinary
import org.gradle.nativeplatform.NativeExecutableBinarySpec
import org.gradle.nativeplatform.NativeExecutableSpec
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.nativeplatform.plugins.NativeComponentModelPlugin
import org.gradle.platform.base.BinaryContainer
import org.gradle.platform.base.ComponentSpecContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import io.github.clearlybaffled.gradle.language.fortran.FortranLangPlugin
import io.github.clearlybaffled.gradle.language.fortran.FortranSourceSet
import io.github.clearlybaffled.gradle.language.fortran.tasks.FortranCompile
import spock.lang.Ignore
import spock.lang.Specification

class FortranLangPluginTest extends Specification {
	
	def pluginClass = FortranLangPlugin
	def sourceSetClass = FortranSourceSet
	def compileTaskClass = FortranCompile
    def pluginName = "fortran"
	
	@Rule
	TemporaryFolder testDir = new TemporaryFolder()

	ProjectInternal project
	@Rule SetRuleContext setContext = new SetRuleContext()


	
	def setup() {
		project  = ProjectBuilder.builder().withProjectDir(testDir.folder).build()
		dsl {
			pluginManager.apply(NativeComponentModelPlugin)
		}
	}
	
	def "creates source set with conventional locations for components"() {
		when:
		dsl {
			pluginManager.apply pluginClass

			model {
				components {
					exe(NativeExecutableSpec)
					lib(NativeLibrarySpec)
				}
			}
		}

		then:
		def components = realizeComponents()
		components.size() == 2
		components.values()*.name == ["exe", "lib"]

		and:
		def exe = components.exe
		exe.sources instanceof ModelMap
		sourceSetClass.isInstance(exe.sources."$pluginName")
		exe.sources."$pluginName".source.srcDirs == [project.file("src/exe/$pluginName")] as Set
		exe.sources."$pluginName".exportedHeaders.srcDirs == [project.file("src/exe/headers")] as Set

		and:
		def lib = components.lib
		lib.sources instanceof ModelMap
		sourceSetClass.isInstance(lib.sources."$pluginName")
		lib.sources."$pluginName".source.srcDirs == [project.file("src/lib/$pluginName")] as Set
		lib.sources."$pluginName".exportedHeaders.srcDirs == [project.file("src/lib/headers")] as Set

		and:
		def sources = realizeSourceSets()
		sources as Set == (lib.sources as Set) + (exe.sources as Set)
	}

	def "can configure source set locations"() {
		given:
		dsl {
			pluginManager.apply pluginClass

			model {
				components {
					lib(NativeLibrarySpec) {
						sources {
							"${this.pluginName}" {
								source {
									srcDirs "d3"
								}
								exportedHeaders {
									srcDirs "h3"
								}
							}
						}
					}
					exe(NativeExecutableSpec) {
						sources {
							"${this.pluginName}" {
								source {
									srcDirs "d1", "d2"
								}
								exportedHeaders {
									srcDirs "h1", "h2"
								}
							}
						}
					}
				}
			}
		}

		expect:
		def components = realizeComponents()
		def exe = components.exe
		with(exe.sources."$pluginName") {
			source.srcDirs*.name == ["d1", "d2"]
			exportedHeaders.srcDirs*.name == ["h1", "h2"]
		}

		def lib = components.lib
		with(lib.sources."$pluginName") {
			source.srcDirs*.name == ["d3"]
			exportedHeaders.srcDirs*.name == ["h3"]
		}
	}

	@Ignore
	def "creates compile tasks for each non-empty executable source set"() {
		when:
		testDir.newFolder("src","test", pluginName)
		testDir.newFolder("src","test", "anotherOne")
		testDir.newFile("src/test/$pluginName/file.o")
		testDir.newFile("src/test/anotherOne/file.o")
		dsl {
			pluginManager.apply pluginClass
			model {
				components {
					test(NativeExecutableSpec) {
						binaries.all { NativeBinary binary ->
							binary.cCompiler.define "NDEBUG"
							binary.cCompiler.define "LEVEL", "1"
							binary.cCompiler.args "ARG1", "ARG2"
						}
						sources {
							anotherOne(this.sourceSetClass) {}
							emptyOne(this.sourceSetClass) {}
						}
					}
				}
			}
		}

		then:
		NativeExecutableBinarySpec binary = realizeBinaries().testExecutable
		binary.tasks.withType(compileTaskClass)*.name as Set == ["compileTestExecutableTestAnotherOne", "compileTestExecutableTest${pluginName.capitalize()}"] as Set

		and:
		binary.tasks.withType(compileTaskClass).each { compile ->
			compile.toolChain == binary.toolChain
			compile.macros == [NDEBUG: null, LEVEL: "1"]
			compile.compilerArgs == ["ARG1", "ARG2"]
		}

		and:
		def linkTask = binary.tasks.link
		//linkTask TaskDependencyMatchers.dependsOn("compileTestExecutableTestAnotherOne", "compileTestExecutableTest${pluginName.capitalize()}")
		
		hasItem linkTask.taskDependencies.getDependencies(linkTask), "compileTestExecutableTestAnotherOne" 
		hasItem linkTask.taskDependencies.getDependencies(linkTask), "compileTestExecutableTest${pluginName.capitalize()}"
		
		
	}
	
	def realize(String name) {
		project.modelRegistry.find(name, ModelType.UNTYPED)
	}

	ModelMap<Task> realizeTasks() {
		project.modelRegistry.find("tasks", ModelTypes.modelMap(Task))
	}

	ComponentSpecContainer realizeComponents() {
		project.modelRegistry.find("components", ComponentSpecContainer)
	}

	ProjectSourceSet realizeSourceSets() {
		project.modelRegistry.find("sources", ProjectSourceSet)
	}

	BinaryContainer realizeBinaries() {
		def binaries = project.modelRegistry.find("binaries", BinaryContainer)
		// Currently some rules take the task container as subject but actually mutate the binaries
		realizeTasks()
		return binaries
	}

	def dsl(@DelegatesTo(Project) Closure closure) {
		closure.delegate = project
		closure()
		project.bindAllModelRules()
	}

	static class SetRuleContext implements TestRule {
		@Override
		Statement apply(Statement base, Description description) {
			return new Statement() {
				@Override
				void evaluate() throws Throwable {
					RuleContext.run(new SimpleModelRuleDescriptor(description.displayName)) {
						base.evaluate()
					}
				}
			}
		}
	}
}
