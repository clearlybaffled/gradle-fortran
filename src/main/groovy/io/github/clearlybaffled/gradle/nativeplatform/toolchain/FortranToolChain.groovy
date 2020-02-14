package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import javax.annotation.Nullable

import org.gradle.language.base.internal.compile.CompileSpec
import org.gradle.language.base.internal.compile.Compiler
import org.gradle.language.base.internal.compile.CompilerUtil
import org.gradle.nativeplatform.internal.LinkerSpec
import org.gradle.nativeplatform.internal.StaticLibraryArchiverSpec
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.toolchain.NativePlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.compilespec.AssembleSpec
import org.gradle.nativeplatform.toolchain.internal.compilespec.CCompileSpec
import org.gradle.nativeplatform.toolchain.internal.compilespec.CppCompileSpec
import org.gradle.nativeplatform.toolchain.internal.gcc.GccPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetadata
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.nativeplatform.toolchain.internal.tools.ToolRegistry


interface FortranCompileSpec extends NativeCompileSpec {}

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


class FortranPlatformToolProvider extends GccPlatformToolProvider {

	@Override
	public CommandLineToolSearchResult locateTool(ToolType compilerType) {
		// TODO Auto-generated method stub
		return super.locateTool(compilerType);
	}

	
	
	@Override
	public <T extends CompileSpec> Compiler<T> newCompiler(Class<T> spec) {
		if (spec in FortranCompileSpec) {
			CompilerUtil.castCompiler(createCompiler())
		} else {
			super.newCompiler(spec)
		}
	}



	@Override
	protected Compiler<CppCompileSpec> createCppCompiler() {
		throw unavailableTool("C++ compiler is not available");
    }

	@Override
    protected Compiler<?> createCppPCHCompiler() {
        throw unavailableTool("C++ pre-compiled header compiler is not available");
    }

	@Override
    protected Compiler<?> createCPCHCompiler() {
        throw unavailableTool("C pre-compiled header compiler is not available");
    }

	@Override
    protected Compiler<?> createObjectiveCppCompiler() {
        throw unavailableTool("Objective-C++ compiler is not available");
    }

	@Override
    protected Compiler<?> createObjectiveCppPCHCompiler() {
        throw unavailableTool("Objective-C++ pre-compiled header compiler is not available");
    }

	@Override
    protected Compiler<?> createObjectiveCCompiler() {
        throw unavailableTool("Objective-C compiler is not available");
    }

	@Override
    protected Compiler<?> createObjectiveCPCHCompiler() {
        throw unavailableTool("Objective-C compiler is not available");
    }

	@Override
    protected Compiler<?> createWindowsResourceCompiler() {
        throw unavailableTool("Windows resource compiler is not available");
    }


	@Override
	protected Compiler<CCompileSpec> createCCompiler() {
        
    }
	
	protected Compiler<?> createCompiler() {
		createCCompiler()
	}
	
	@Override
	protected Compiler<AssembleSpec> createAssembler() {
		// TODO Auto-generated method stub
		return super.createAssembler();
	}

	@Override
	protected Compiler<LinkerSpec> createLinker() {
		// TODO Auto-generated method stub
		return super.createLinker();
	}

	@Override
	protected Compiler<StaticLibraryArchiverSpec> createStaticLibraryArchiver() {
		// TODO Auto-generated method stub
		return super.createStaticLibraryArchiver();
	}

	@Override
	protected Compiler<?> createSymbolExtractor() {
		// TODO Auto-generated method stub
		return super.createSymbolExtractor();
	}

	@Override
	protected Compiler<?> createStripper() {
		// TODO Auto-generated method stub
		return super.createStripper();
	}

	@Override
	public SystemLibraries getSystemLibraries(ToolType compilerType) {
		// TODO Auto-generated method stub
		return super.getSystemLibraries(compilerType);
	}

	@Override
	public CompilerMetadata getCompilerMetadata(ToolType toolType) {
		// TODO Auto-generated method stub
		return super.getCompilerMetadata(toolType);
	}
	
}