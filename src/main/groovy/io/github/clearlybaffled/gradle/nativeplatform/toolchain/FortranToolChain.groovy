package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import javax.annotation.Nullable

import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.toolchain.NativePlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.nativeplatform.toolchain.internal.tools.ToolRegistry
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath


class FortranPlatformToolChain implements NativePlatformToolChain, ToolRegistry {
	final NativePlatform platform
	List<String> compilerProbeArgs = []
	final Map<ToolType, GccCommandLineToolConfigurationInternal> tools = [:]
	
	
	public FortranPlatformToolChain(NativePlatform platform) {
		this.platform = platform;
		
	}
	
	/**
	 * Returns the settings to use for the Fortran compiler.
	 */
	FortranCommandLineToolConfiguration getCompiler() {
		 tools.get(ToolType.C_COMPILER)
	}

	
	/**
	 * Returns the settings to use for the linker.
	 */
	FortranCommandLineToolConfiguration getLinker() { 
		tools.get(ToolType.LINKER)
	}
	
	/**
	 * Returns the settings to use for the assembler.
	 */
	FortranCommandLineToolConfiguration getAssembler() {
		tools.get(ToolType.ASSEMBLER)
	}

	/**
	 * Returns the settings to use for the archiver.
	 */
	FortranCommandLineToolConfiguration getStaticLibArchiver() {
		tools.get(ToolType.SYMBOL_EXTRACTOR)
	}

	FortranCommandLineToolConfiguration getStripper() {
		tools.get(ToolType.STRIPPER)
	}
	
	def getTools() {
		tools.values()
	}
	
	@Nullable
	@Override
	GccCommandLineToolConfigurationInternal getTool(ToolType toolType) {
		tools.get(toolType);
	}

	public void add(FortranCommandLineToolConfiguration tool) {
		tools.put(tool.getToolType(), tool);
	}
	
}

class FortranCommandLineToolConfiguration extends DefaultCommandLineToolConfiguration implements GccCommandLineToolConfigurationInternal {
	String executable

	public FortranCommandLineToolConfiguration(ToolType toolType, String defaultExecutable) {
		super(toolType)
		this.executable = defaultExecutable
	}
	
}


