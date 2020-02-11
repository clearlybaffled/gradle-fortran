package io.github.clearlybaffled.gradle.nativeplugin.toolchain

import org.gradle.nativeplatform.toolchain.NativePlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.nativeplatform.toolchain.internal.tools.ToolRegistry


class FortranPlatformToolChain implements NativePlatformToolChain, ToolRegistry {
	/**
	 * Returns the settings to use for the Fortran compiler.
	 */
	FortranCommandLineToolConfiguration getCompiler() {
		
	}

	
	/**
	 * Returns the settings to use for the linker.
	 */
	FortranCommandLineToolConfiguration getLinker() { 
		
	}
	
	/**
	 * Returns the settings to use for the assembler.
	 */
	FortranCommandLineToolConfiguration getAssembler() {
		
	}

	/**
	 * Returns the settings to use for the archiver.
	 */
	FortranCommandLineToolConfiguration getStaticLibArchiver() {
		
	}
}

class FortranCommandLineToolConfiguration extends DefaultCommandLineToolConfiguration implements GccCommandLineToolConfigurationInternal {
	String executable

	public FortranCommandLineToolConfiguration(ToolType toolType, String defaultExecutable) {
		super(toolType)
		this.executable = defaultExecutable
	}
	
}


