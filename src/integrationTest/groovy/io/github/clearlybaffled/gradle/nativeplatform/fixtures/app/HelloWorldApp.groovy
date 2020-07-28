package io.github.clearlybaffled.gradle.nativeplatform.fixtures.app

import java.nio.charset.StandardCharsets

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils


public abstract class HelloWorldApp {
	
	public static final String HELLO_WORLD = "Hello, World!"
	public static final String HELLO_WORLD_FRENCH = "Bonjour, Monde!"
	
	public abstract List<String> getPluginList()
	public abstract SourceFile getMainSource()
	public abstract SourceFile getLibraryHeader()
	public abstract List<SourceFile> getLibrarySources()
	public abstract String getSourceSetType()
	
	abstract List<SourceFile> getSourceFiles()

	abstract List<SourceFile> getHeaderFiles()

	def sourceSetName = "main"
	
	def englishOutput = ' ' + HELLO_WORLD + "\n" //12"
	def frenchOutput = ' ' + HELLO_WORLD_FRENCH + "\n" //12"
	def sourceType = mainSource.path
	def sourceExtension = FilenameUtils.getExtension(mainSource.name)
	
	
	public String getPluginScript() {
		"""
plugins {
			${pluginList.collect { "    id '$it'" }.join('\n')}
}
		"""

	}
	
	List<SourceFile> getFiles() {
		sourceFiles + headerFiles
	}

	void writeSources(File sourceDir) {
		for (SourceFile srcFile : files) {
			srcFile.writeToDir(sourceDir)
		}
	}
	
	public String compilerArgs(String arg) {
		compilerConfig("args", arg)
	}

	public String compilerDefine(String define) {
		compilerConfig("define", define)
	}

	public String compilerDefine(String define, String value) {
		compilerConfig("define", define, value)
	}

	private String compilerConfig(String action, String... args) {
		String quotedArgs =  args.collect { "'$it'" }.join(',')
		
		pluginList.inject("") { config, plugin ->
			String compilerPrefix = getCompilerPrefix(plugin)
			if (compilerPrefix) {
				config << "${compilerPrefix}Compiler.${action} ${quotedArgs}\n"
			}
		}
	}
	
	def getCompilerPrefix(plugin) {
		switch(plugin) {
			case 'c': return "c"
			case 'cpp': return 'cpp'
			case ~/.*fortran$/: return "c"
			default: null
		}
	}
}


public class SourceFile {
	private final String path
	private final String name
	private final String content

	public SourceFile(String path, String name, String content) {
		this.content = content
		this.path = path
		this.name = name
	}

	public String getPath() {
		path
	}

	public String getName() {
		name
	}

	public String getContent() {
		content
	}

	public File writeToDir(File base) {
		writeToDir(base, name)
	}

	public File writeToDir(File base, String name) {
		File file = new File([base.path, path, name].join('/'))
		writeToFile(file)
		file
	}

	public void writeToFile(File file) {
        try {
            FileUtils.writeStringToFile(file, content.toString(), StandardCharsets.UTF_8)
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not write to test file '%s'", file), e)
        }
	}

	public String withPath(String basePath) {
		 [basePath, path, name].findAll { it != null }.join('/')
	}
}
