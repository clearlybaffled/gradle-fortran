package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import javax.annotation.Nullable

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.language.base.internal.compile.CompileSpec
import org.gradle.language.base.internal.compile.Compiler
import org.gradle.language.base.internal.compile.CompilerUtil
import org.gradle.language.base.internal.compile.DefaultCompilerVersion
import org.gradle.language.base.internal.compile.VersionAwareCompiler
import org.gradle.language.nativeplatform.internal.AbstractNativeCompileSpec
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.internal.LinkerSpec
import org.gradle.nativeplatform.internal.StaticLibraryArchiverSpec
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal
import org.gradle.nativeplatform.toolchain.NativePlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.DefaultCommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.DefaultMutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.MutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage
import org.gradle.nativeplatform.toolchain.internal.OutputCleaningCompiler
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.UnavailablePlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.UnsupportedPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.compilespec.AssembleSpec
import org.gradle.nativeplatform.toolchain.internal.compilespec.CCompileSpec
import org.gradle.nativeplatform.toolchain.internal.compilespec.CppCompileSpec
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.GccPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.TargetPlatformConfiguration
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain.CompilerMetaDataProviderWithDefaultArgs
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.GccMetadata
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetadata
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.nativeplatform.toolchain.internal.tools.ToolRegistry
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.platform.base.internal.toolchain.ComponentNotFound
import org.gradle.platform.base.internal.toolchain.SearchResult
import org.gradle.platform.base.internal.toolchain.ToolChainAvailability
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran.FortranCompiler
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins.GFortranCompilePlugins
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins.IFortranCompilePlugins


class FortranToolChains implements Plugin<Project> {
	@Override
	public void apply (Project project) {
		project.getPluginManager().apply(GFortranCompilePlugins)
		//project.getPluginManager().apply(IFortranCompilePlugins)
	}
}

interface FortranCompileSpec extends CCompileSpec {} 
class DefaultFortranCompileSpec extends AbstractNativeCompileSpec implements FortranCompileSpec {}

interface FortranPlatformToolChain extends NativePlatformToolChain, ToolRegistry {}

class DefaultFortranPlatformToolChain implements FortranPlatformToolChain {
	final NativePlatform platform
	List<String> compilerProbeArgs = []
	final Map<ToolType, GccCommandLineToolConfigurationInternal> tools = [:]
	def canUseCommandFile = true
	
	public DefaultFortranPlatformToolChain(NativePlatform platform) {
		this.platform = platform	
	}
	
	public void compilerProbeArgs(String... args) {
		this.compilerProbeArgs << args
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
		tools.get(toolType)
	}

	public void add(FortranCommandLineToolConfiguration tool) {
		tools.put(tool.getToolType(), tool)
	}
	
	public Collection<FortranCommandLineToolConfiguration> getCompilers() {
		[tools.get(ToolType.C_COMPILER)]
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

	private final ToolSearchPath toolSearchPath
	private final ToolRegistry toolRegistry
	private final ExecActionFactory execActionFactory
	private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
	private final boolean useCommandFile
	private final WorkerLeaseService workerLeaseService
	private final CompilerMetaDataProvider<GccMetadata> metadataProvider

	FortranPlatformToolProvider(BuildOperationExecutor buildOperationExecutor, OperatingSystemInternal targetOperatingSystem, ToolSearchPath toolSearchPath, ToolRegistry toolRegistry, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, boolean useCommandFile, WorkerLeaseService workerLeaseService, CompilerMetaDataProvider<GccMetadata> metadataProvider) {
		super(buildOperationExecutor, targetOperatingSystem, toolSearchPath, toolRegistry, execActionFactory, compilerOutputFileNamingSchemeFactory, useCommandFile, workerLeaseService, metadataProvider)
		this.toolRegistry = toolRegistry
		this.toolSearchPath = toolSearchPath
		this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory
		this.useCommandFile = useCommandFile
		this.execActionFactory = execActionFactory
		this.workerLeaseService = workerLeaseService
		this.metadataProvider = metadataProvider
	}

	@Override
	public CommandLineToolSearchResult locateTool(ToolType compilerType) {
		// TODO Auto-generated method stub
		return super.locateTool(compilerType)
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
		throw unavailableTool("C++ compiler is not available")
    }

	@Override
    protected Compiler<?> createCppPCHCompiler() {
        throw unavailableTool("C++ pre-compiled header compiler is not available")
    }

	@Override
    protected Compiler<?> createCPCHCompiler() {
        throw unavailableTool("C pre-compiled header compiler is not available")
    }

	@Override
    protected Compiler<?> createObjectiveCppCompiler() {
        throw unavailableTool("Objective-C++ compiler is not available")
    }

	@Override
    protected Compiler<?> createObjectiveCppPCHCompiler() {
        throw unavailableTool("Objective-C++ pre-compiled header compiler is not available")
    }

	@Override
    protected Compiler<?> createObjectiveCCompiler() {
        throw unavailableTool("Objective-C compiler is not available")
    }

	@Override
    protected Compiler<?> createObjectiveCPCHCompiler() {
        throw unavailableTool("Objective-C compiler is not available")
    }

	@Override
    protected Compiler<?> createWindowsResourceCompiler() {
        throw unavailableTool("Windows resource compiler is not available")
    }


	@Override
	protected Compiler<CCompileSpec> createCCompiler() {
        return createCompiler()
    }
	
	protected Compiler<FortranCompileSpec> createCompiler() {
		GccCommandLineToolConfigurationInternal compilerTool = toolRegistry.getTool(ToolType.C_COMPILER)
		def compiler = new FortranCompiler(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineTool(compilerTool), context(compilerTool), getObjectFileExtension(), useCommandFile, workerLeaseService)
		def outputCleaningCompiler = new OutputCleaningCompiler<FortranCompileSpec>(compiler, compilerOutputFileNamingSchemeFactory, getObjectFileExtension())
		def gccMetadata = getGccMetadata(compilerTool)
		return new VersionAwareCompiler<FortranCompileSpec>(compiler, new DefaultCompilerVersion(
			metadataProvider.compilerType.identifier,
			gccMetadata.component.vendor,
			gccMetadata.component.version)
		)
	}

	private CommandLineToolInvocationWorker commandLineTool(GccCommandLineToolConfigurationInternal tool) {
		ToolType key = tool.toolType
		String exeName = tool.executable
		return new DefaultCommandLineToolInvocationWorker(key.toolName, toolSearchPath.locate(key, exeName).tool, execActionFactory)
	}

	private CommandLineToolContext context(GccCommandLineToolConfigurationInternal toolConfiguration) {
		MutableCommandLineToolContext baseInvocation = new DefaultMutableCommandLineToolContext()
		// MinGW requires the path to be set
		baseInvocation.addPath(toolSearchPath.getPath())
		baseInvocation.addEnvironmentVar("CYGWIN", "nodosfilewarning")
		baseInvocation.argAction = toolConfiguration.argAction

		String developerDir = System.getenv("DEVELOPER_DIR")
		if (developerDir != null) {
			baseInvocation.addEnvironmentVar("DEVELOPER_DIR", developerDir)
		}
		return baseInvocation
	}
	
	private SearchResult<GccMetadata> getGccMetadata(ToolType compilerType) {
		GccCommandLineToolConfigurationInternal compiler = toolRegistry.getTool(compilerType)
		if (compiler == null) {
			return new ComponentNotFound<GccMetadata>("Tool " + compilerType.toolName + " is not available")
		}
		CommandLineToolSearchResult searchResult = toolSearchPath.locate(compiler.toolType, compiler.executable)
		String language = "f95"  // TODO: BADD HARCODING
		List<String> languageArgs = language == null ? Collections.<String>emptyList() : ImmutableList.of("-x", language)

		return metadataProvider.getCompilerMetaData(toolSearchPath.getPath()) { spec ->
			spec.executable(searchResult.tool).args(languageArgs)
		}
	}
	
	@Override
	protected Compiler<AssembleSpec> createAssembler() {
		// TODO Auto-generated method stub
		return super.createAssembler()
	}

	@Override
	protected Compiler<LinkerSpec> createLinker() {
		// TODO Auto-generated method stub
		return super.createLinker()
	}

	@Override
	protected Compiler<StaticLibraryArchiverSpec> createStaticLibraryArchiver() {
		// TODO Auto-generated method stub
		return super.createStaticLibraryArchiver()
	}

	@Override
	protected Compiler<?> createSymbolExtractor() {
		// TODO Auto-generated method stub
		return super.createSymbolExtractor()
	}

	@Override
	protected Compiler<?> createStripper() {
		// TODO Auto-generated method stub
		return super.createStripper()
	}

	@Override
	public SystemLibraries getSystemLibraries(ToolType compilerType) {
		// TODO Auto-generated method stub
		return super.getSystemLibraries(compilerType)
	}

	@Override
	public CompilerMetadata getCompilerMetadata(ToolType toolType) {
		// TODO Auto-generated method stub
		return super.getCompilerMetadata(toolType)
	}
	
}

abstract class AbstractFortranCompatibleToolChain extends AbstractGccCompatibleToolChain {

	private final List<TargetPlatformConfiguration> platformConfigs = []
	private final Map<NativePlatform, PlatformToolProvider> toolProviders = [:]
	private final Instantiator instantiator
	private final SystemLibraryDiscovery standardLibraryDiscovery
	private final ToolSearchPath toolSearchPath
	private final ExecActionFactory execActionFactory
	private final WorkerLeaseService workerLeaseService
	private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
	
	
	public AbstractFortranCompatibleToolChain(String name, BuildOperationExecutor buildOperationExecutor,
			OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory,
			CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory,
			CompilerMetaDataProvider<GccMetadata> metaDataProvider, SystemLibraryDiscovery standardLibraryDiscovery,
			Instantiator instantiator, WorkerLeaseService workerLeaseService) {
		super(name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
				compilerOutputFileNamingSchemeFactory, metaDataProvider, standardLibraryDiscovery, instantiator,
				workerLeaseService)
		this.platformConfigs = platformConfigs
		this.toolProviders = toolProviders
		this.instantiator = instantiator
		this.standardLibraryDiscovery = standardLibraryDiscovery
		this.toolSearchPath = new ToolSearchPath(operatingSystem)
		this.execActionFactory = execActionFactory
		this.workerLeaseService = workerLeaseService
		this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory
	}

	@Override
	public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
		if (sourceLanguage == NativeLanguage.ANY) {
			select(targetMachine) ?: new UnavailablePlatformToolProvider(targetMachine.operatingSystem, ToolType.C_COMPILER)
		} else {
			new UnsupportedPlatformToolProvider(targetMachine.operatingSystem, String.format("Don't know how to compile language %s.", sourceLanguage))
		}
	}
	
	public PlatformToolProvider select(NativePlatformInternal targetPlatform) {
		def toolProvider = toolProviders.get(targetPlatform) ?: toolProviders.put(targetPlatform, createPlatformToolProvider(targetPlatform))
		toolProvider.locateTool(ToolType.C_COMPILER)
	}

	
	private PlatformToolProvider createPlatformToolProvider(NativePlatformInternal targetPlatform) {
		TargetPlatformConfiguration targetPlatformConfigurationConfiguration = getPlatformConfiguration(targetPlatform)
		if (targetPlatformConfigurationConfiguration) {
			DefaultFortranPlatformToolChain configurableToolChain = instantiator.newInstance(DefaultFortranPlatformToolChain, targetPlatform)
			configureDefaultTools(configurableToolChain)
			targetPlatformConfigurationConfiguration.apply(configurableToolChain)
			configureActions.execute(configurableToolChain)
			configurableToolChain.compilerProbeArgs(standardLibraryDiscovery.compilerProbeArgs(targetPlatform))
	
			ToolChainAvailability result = new ToolChainAvailability()
			initTools(configurableToolChain, result)
			if (!result.isAvailable()) {
				return new UnavailablePlatformToolProvider(targetPlatform.operatingSystem, result)
			}
	
			return new FortranPlatformToolProvider(buildOperationExecutor, targetPlatform.operatingSystem, toolSearchPath, configurableToolChain, execActionFactory, compilerOutputFileNamingSchemeFactory, configurableToolChain.canUseCommandFile, workerLeaseService, new CompilerMetaDataProviderWithDefaultArgs(configurableToolChain.getCompilerProbeArgs(), metaDataProvider))
		} else {
			new UnsupportedPlatformToolProvider(targetPlatform.operatingSystem, String.format("Don't know how to build for %s.", targetPlatform.displayName))
		}
	}


	protected void initForImplementation(DefaultFortranPlatformToolChain platformToolChain, GccMetadata versionResult) {
	}


	abstract protected void configureDefaultTools(DefaultFortranPlatformToolChain toolChain)
	
}