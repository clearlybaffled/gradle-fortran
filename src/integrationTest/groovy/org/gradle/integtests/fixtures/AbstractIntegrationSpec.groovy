package org.gradle.integtests.fixtures

import static org.junit.Assert.assertTrue

import org.gradle.internal.os.OperatingSystem
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

abstract class AbstractIntegrationSpec extends Specification {

	@Rule final TemporaryFolder testDir = new TemporaryFolder()
	
	
	GradleRunner gradleRunner
	BuildResult result
	
	protected String buildFileName = 'build.gradle'
	protected String settingsFileName = 'settings.gradle'
	
	def setup() {
		File buildFile = testDir.newFile(buildFileName)
		File settingsFile = testDir.newFile(settingsFileName)
		File propertiesFile = testDir.newFile('gradle.properties')
	}
	
	GradleRunner getRunner() {
		gradleRunner == null ? createRunner() : gradleRunner
	}
	
	GradleRunner createRunner() {
		gradleRunner = GradleRunner.create()		
				.forwardOutput()
				.withPluginClasspath()
				.withProjectDir(testDir.root)
	}
	
	protected File getTestDirectory() {
		testDir.folder
	}
	
	File file(Object... path) {
		if (path.length == 1) {
			if (path[0] instanceof File) {
				path[0] as File
			} else {
				new File([testDir.root, path[0]].join('/'))
			}
		} else {
			def paths = (path as String[])*.replaceAll('/$','')
			testDir.newFolder(paths[0..-2])
			testDir.newFile(paths.join('/'))
		}
	}
	
	File getBuildFile() {
		//testDir.newFile(buildFileName)
		new File([testDirectory, buildFileName].join('/'))
	}
	
	File buildScript(String script) {
		buildFile.text = script
		buildFile
	}
	
	protected GradleRunner inDirectory(String path) {
		inDirectory(file(path))
	}

	protected GradleRunner inDirectory(File directory) {
		projectDir(directory)
	}

	protected GradleRunner projectDir(path) {
		gradleRunner = runner.withProjectDir(file(path))
	}

	protected GradleRunner requireOwnGradleUserHomeDir() {
		gradleRunner = runner.withEnvironment(["GRADLE_HOME": testDir.newFolder("user-home").path])
	}

	/**
	 * Synonym for succeeds()
	 */
	protected BuildResult run(String... tasks) {
		succeeds(*tasks)
	}
	
	protected GradleRunner args(String... args) {
		gradleRunner = runner.withArguments(args)
	}
		
	protected GradleRunner withDebugLogging() {
		gradleRunner = runner.withDebug(true)
	}
	
	protected GradleRunner withStacktrace() {
		gradleRunner = runner.withArguments("--stacktrace")
	}
	
	protected BuildResult succeeds(String... tasks) {
		result = runner.withArguments(tasks).build()
	}
	
	protected BuildResult runAndFail(String... tasks) {
		fails(*tasks)
	}

	protected BuildResult fails(String... tasks) {
		result = runner.withArguments(tasks).buildAndFail()
	}

	protected void executedAndNotSkipped(String... tasks) {
		assertHasResult()
		tasks.each {
			assert !result.tasks(TaskOutcome.SKIPPED).contains(it)
		}
	}

	protected void noneSkipped() {
		assertHasResult()
		def outcomes = result.tasks*.outcome as Set
			
		
	}

	protected void allSkipped() {
		assertHasResult()
		result.tasks.each { 
			assert it.outcome == TaskOutcome.SKIPPED
		}
	}

	protected void skipped(String... tasks) {
		assertHasResult()
		tasks.each {
			assert  result.tasks(TaskOutcome.SKIPPED).contains(it) ||
					result.tasks(TaskOutcome.UP_TO_DATE).contains(it) ||
					result.tasks(TaskOutcome.NO_SOURCE).contains(it) ||
					result.tasks(TaskOutcome.FROM_CACHE).contains(it)
		}
	}

	protected void notExecuted(String... tasks) {
		assertHasResult()
		tasks.each {
			result.tasks.assertTaskNotExecuted(it)
		}
	}

	protected void executed(String... tasks) {
		assertHasResult()
		tasks.each {
			result.assertTaskExecuted(it)
		}
	}

	protected void failureHasCause(String cause) {
		failure.assertHasCause(cause)
	}

	protected void failureDescriptionStartsWith(String description) {
		failure.assertThatDescription(containsNormalizedString(description))
	}

	protected void failureDescriptionContains(String description) {
		failure.assertThatDescription(containsNormalizedString(description))
	}

	protected void failureCauseContains(String description) {
		failure.assertThatCause(containsNormalizedString(description))
	}


	private assertHasResult() {
		assert result != null: "result is null, you haven't run succeeds()"
	}

	String getOutput() {
		result.output
	}

	String getErrorOutput() {
		result.output
	}
	
	/**
	 * Replaces the given text in the build script with new value, asserting that the change was actually applied (ie the text was present).
	 */
	void editBuildFile(String oldText, String newText) {
		def newContent = buildFile.text.replace(oldText, newText)
		assert newContent != buildFile.text
		buildFile.text = newContent
	}
	
	void outputContains(String string) {
		assertHasResult()
		assert result.output.contains(string.trim())
	}

	void outputDoesNotContain(String string) {
		assertHasResult()
		assert !result.output.contains(string.trim())
	}
	
	public ExecutableFixture executable(path) {
		new ExecutableFixture(new File(testDirectory, OperatingSystem.current().getExecutableName(path.toString())))
	}

}

class ExecutableFixture {
	final File file
    
    public ExecutableFixture(File file) {
        this.file = file
    }
    
	public File assertExists() {
		assertTrue(String.format("%s is not a file", file), file.file)
		file
	}
    public ExecOutput exec(Object... args) {
        executeSuccess(Arrays.asList(args), null)
    }

	ExecOutput execute(List args, List env) {
		assertExists()
		def process = ([file.absolutePath] + args).execute(env, null)

		// Prevent process from hanging by consuming the output as we go.
		def output = new ByteArrayOutputStream()
		def error = new ByteArrayOutputStream()

		Thread outputThread = Thread.start { output << process.in }
		Thread errorThread = Thread.start { error << process.err }

		int exitCode = process.waitFor()
		outputThread.join()
		errorThread.join()

		return new ExecOutput(exitCode, output.toString(), error.toString())
	}
	
	ExecOutput executeSuccess(List args, List env) {
		def result = execute(args, env)
		if (result.exitCode != 0) {
			throw new RuntimeException("Could not execute $file. Error: ${result.error}, Output: ${result.out}")
		}
		return result
	}

	ExecOutput executeFailure(List args, List env) {
		def result = execute(args, env)
		if (result.exitCode == 0) {
			throw new RuntimeException("Unexpected success, executing $file. Error: ${result.error}, Output: ${result.out}")
		}
		return result
	}
}

class ExecOutput {
	ExecOutput(int exitCode, String rawOutput, String error) {
		this.exitCode = exitCode
		this.rawOutput = rawOutput
		this.out = rawOutput.replaceAll("\r\n|\r", "\n")
		this.error = error
	}

	int exitCode
	String rawOutput
	String out
	String error
}
