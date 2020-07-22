package io.github.clearlybaffled.gradle.language.fortran.tasks

import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.project.taskfactory.TaskInstantiator
import org.gradle.api.internal.tasks.TaskExecuter
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.internal.tasks.execution.DefaultTaskExecutionContext
import org.gradle.api.tasks.WorkResult
import org.gradle.execution.ProjectExecutionServices
import org.gradle.language.base.internal.compile.Compiler
import org.gradle.language.c.tasks.CCompile
import org.gradle.nativeplatform.platform.internal.ArchitectureInternal
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.compilespec.FortranCompileSpec
import spock.lang.Specification

class FortranCompileTest extends Specification {

	FortranCompile fortranCompile
	NativeToolChainInternal toolChain = Mock(NativeToolChainInternal)
	NativePlatformInternal platform = Mock(NativePlatformInternal)
	PlatformToolProvider platformToolChain = Mock(PlatformToolProvider)
	Compiler<FortranCompileSpec> compiler = Mock(Compiler)
	
	ProjectInternal project
	ProjectExecutionServices executionServices

	@Rule TemporaryFolder testDir = new TemporaryFolder()
	
	def setup() {
		project = ProjectBuilder.builder().withProjectDir(testDir.folder).build()
		executionServices = new ProjectExecutionServices(project)
		System.setProperty("user.dir", testDir.folder.absolutePath)
		fortranCompile = project.services.get(TaskInstantiator).create("name", FortranCompile)
	}
	
	
	def "executes using the Fortran Compiler"() {
		def sourceFile = testDir.newFile("sourceFile")
		def result = Mock(WorkResult)

		when:
		fortranCompile.toolChain = toolChain
		fortranCompile.targetPlatform = platform
		fortranCompile.compilerArgs = ["arg"]
		fortranCompile.macros = [def: "value"]
		fortranCompile.objectFileDir = testDir.newFolder("outputFile")
		fortranCompile.source sourceFile
		execute(fortranCompile)

		then:
		_ * toolChain.outputType >> "c"
		platform.getName() >> "testPlatform"
		platform.getArchitecture() >> Mock(ArchitectureInternal) { getName() >> "arch" }
		platform.getOperatingSystem() >> Mock(OperatingSystemInternal) { getName() >> "os" }
		2 * toolChain.select(platform) >> platformToolChain
		2 * platformToolChain.newCompiler({FortranCompileSpec.class.isAssignableFrom(it)}) >> compiler

		1 * compiler.execute({ FortranCompileSpec spec ->
			assert spec.sourceFiles*.name== ["sourceFile"]
			assert spec.args == ['arg']
			assert spec.allArgs == ['arg']
			assert spec.macros == [def: 'value']
			assert spec.objectFileDir.name == "outputFile"
			true
		}) >> result
		1 * result.didWork >> true
		0 * _._

		and:
		fortranCompile.didWork
	}
	
	
	

	void execute(Task task) {
		executionServices.get(TaskExecuter).execute((TaskInternal) task, (TaskStateInternal) task.state, new DefaultTaskExecutionContext(null))
		task.state.rethrowFailure()
	}
}
