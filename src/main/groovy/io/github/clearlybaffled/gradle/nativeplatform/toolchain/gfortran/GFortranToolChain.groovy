package io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.language.base.internal.compile.Compiler
import org.gradle.language.base.internal.compile.DefaultCompilerVersion
import org.gradle.language.base.internal.compile.VersionAwareCompiler
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.GccCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.DefaultCommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.DefaultMutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.MutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage
import org.gradle.nativeplatform.toolchain.internal.OutputCleaningCompiler
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SymbolExtractorOsConfig
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.UnavailablePlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.UnsupportedPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.compilespec.CppCompileSpec
import org.gradle.nativeplatform.toolchain.internal.gcc.CppCompiler
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.GccPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.GccToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.TargetPlatformConfiguration
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain.CompilerMetaDataProviderWithDefaultArgs
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.GccMetadata
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.nativeplatform.toolchain.internal.tools.ToolRegistry
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.platform.base.internal.toolchain.ComponentNotFound
import org.gradle.platform.base.internal.toolchain.ToolChainAvailability
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.FortranCompileSpec

/*
 * Needs to extend {@link Gcc} because {@link AbstractNativeCompileTask} is looking for an instanceof a {@link Gcc} 
 */
interface GFortran extends Gcc {}

/*
 * Adds Fortran as a language option to Gcc toolchain
 */
public class GFortranToolChain extends GccToolChain implements GFortran {

	public static final String DEFAULT_NAME = "gfortran"
	private final List<TargetPlatformConfiguration> platformConfigs = []
	private final Map<NativePlatform, PlatformToolProvider> toolProviders = [:]
	private final Instantiator instantiator
	private final SystemLibraryDiscovery standardLibraryDiscovery
	private final ToolSearchPath toolSearchPath
	private final ExecActionFactory execActionFactory
	private final WorkerLeaseService workerLeaseService
	private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory

		
	public GFortranToolChain(Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, WorkerLeaseService workerLeaseService) {
		super(instantiator, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
				compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
		this.platformConfigs = platformConfigs
		this.toolProviders = toolProviders
		this.instantiator = instantiator
		this.standardLibraryDiscovery = standardLibraryDiscovery
		this.toolSearchPath = new ToolSearchPath(operatingSystem)
		this.execActionFactory = execActionFactory
		this.workerLeaseService = workerLeaseService
		this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory

		target("linux-x86_64")
	}

	
	@Override
	protected void configureDefaultTools(DefaultGccPlatformToolChain toolChain) {
		toolChain.with {
			add(instantiator.newInstance(GccCommandLineToolConfiguration, ToolType.CPP_COMPILER, "gcc"))
			add(instantiator.newInstance(GccCommandLineToolConfiguration, ToolType.LINKER, "gcc")) 
			add(instantiator.newInstance(GccCommandLineToolConfiguration, ToolType.STATIC_LIB_ARCHIVER, "ar"))
			add(instantiator.newInstance(GccCommandLineToolConfiguration, ToolType.ASSEMBLER, "gcc"))
			add(instantiator.newInstance(GccCommandLineToolConfiguration, ToolType.SYMBOL_EXTRACTOR, SymbolExtractorOsConfig.current().getExecutableName()))
			add(instantiator.newInstance(GccCommandLineToolConfiguration, ToolType.STRIPPER, "strip"))
		}
	}
		
	@Override
	public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
		if (sourceLanguage == NativeLanguage.ANY) {
			select(targetMachine) ?: new UnavailablePlatformToolProvider(targetMachine.operatingSystem, ToolType.CPP_COMPILER)
		} else {
			new UnsupportedPlatformToolProvider(targetMachine.operatingSystem, String.format("Don't know how to compile language %s.", sourceLanguage))
		}
	}
	
	public PlatformToolProvider select(NativePlatformInternal targetPlatform) {
		def toolProvider = toolProviders.get(targetPlatform) ?: toolProviders.put(targetPlatform, createPlatformToolProvider(targetPlatform))
		toolProvider.locateTool(ToolType.CPP_COMPILER)
	}

	
	private PlatformToolProvider createPlatformToolProvider(NativePlatformInternal targetPlatform) {
		TargetPlatformConfiguration targetPlatformConfigurationConfiguration = getPlatformConfiguration(targetPlatform)
		if (targetPlatformConfigurationConfiguration) {
			DefaultGccPlatformToolChain configurableToolChain = instantiator.newInstance(DefaultGccPlatformToolChain, targetPlatform)
			configureDefaultTools(configurableToolChain)
			targetPlatformConfigurationConfiguration.apply(configurableToolChain)
			configureActions.execute(configurableToolChain)
			configurableToolChain.compilerProbeArgs(standardLibraryDiscovery.compilerProbeArgs(targetPlatform))
	
			ToolChainAvailability result = new ToolChainAvailability()
			initTools(configurableToolChain, result)
			if (!result.isAvailable()) {
				return new UnavailablePlatformToolProvider(targetPlatform.operatingSystem, result)
			}
	
			return new GFortranPlatformToolProvider(buildOperationExecutor, targetPlatform.operatingSystem, toolSearchPath, configurableToolChain, execActionFactory, compilerOutputFileNamingSchemeFactory, configurableToolChain.canUseCommandFile, workerLeaseService, new CompilerMetaDataProviderWithDefaultArgs(configurableToolChain.getCompilerProbeArgs(), metaDataProvider))
		} else {
			new UnsupportedPlatformToolProvider(targetPlatform.operatingSystem, String.format("Don't know how to build for %s.", targetPlatform.displayName))
		}
	}

	
}

class GFortranPlatformToolProvider extends GccPlatformToolProvider {
	
	private final ToolSearchPath toolSearchPath
	private final ToolRegistry toolRegistry
	private final ExecActionFactory execActionFactory
	private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
	private final boolean useCommandFile
	private final WorkerLeaseService workerLeaseService
	private final CompilerMetaDataProvider<GccMetadata> metadataProvider

	GFortranPlatformToolProvider(BuildOperationExecutor buildOperationExecutor, OperatingSystemInternal targetOperatingSystem, ToolSearchPath toolSearchPath, ToolRegistry toolRegistry, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, boolean useCommandFile, WorkerLeaseService workerLeaseService, CompilerMetaDataProvider<GccMetadata> metadataProvider) {
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
	protected Compiler<CppCompileSpec> createCppCompiler() {
		def compilerTool = toolRegistry.getTool(ToolType.CPP_COMPILER)
		if (compilerTool == null) {
			new ComponentNotFound<GccMetadata>("Tool $ToolType.CPP_COMPILER is not available")
		} else {
			def compiler = new CppCompiler(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineTool(compilerTool), context(compilerTool), getObjectFileExtension(), useCommandFile, workerLeaseService) 
			def outputCleaningCompiler = new OutputCleaningCompiler<FortranCompileSpec>(compiler, compilerOutputFileNamingSchemeFactory, getObjectFileExtension())
			
			def searchResult = toolSearchPath.locate(compilerTool.toolType, compilerTool.executable)
			
			def language = "f95"  // TODO: BADD HARCODING
			List<String> languageArgs = ImmutableList.of("-x", language)
	
			def gccMetadata = metadataProvider.getCompilerMetaData(toolSearchPath.path) { spec ->
				spec.executable(searchResult.tool).args(languageArgs)
			}
			
			new VersionAwareCompiler<FortranCompileSpec>(
				compiler, 
				new DefaultCompilerVersion(metadataProvider.compilerType.identifier, gccMetadata.component.vendor, gccMetadata.component.version)
			)
		}
	}

	def commandLineTool(GccCommandLineToolConfigurationInternal tool) {
		ToolType key = tool.toolType
		String exeName = tool.executable
		new DefaultCommandLineToolInvocationWorker(key.toolName, toolSearchPath.locate(key, exeName).tool, execActionFactory)
	}

	def context(GccCommandLineToolConfigurationInternal toolConfiguration) {
		MutableCommandLineToolContext baseInvocation = new DefaultMutableCommandLineToolContext()
		// MinGW requires the path to be set
		baseInvocation.addPath(toolSearchPath.getPath())
		baseInvocation.addEnvironmentVar("CYGWIN", "nodosfilewarning")
		baseInvocation.argAction = toolConfiguration.argAction

		String developerDir = System.getenv("DEVELOPER_DIR")
		if (developerDir != null) {
			baseInvocation.addEnvironmentVar("DEVELOPER_DIR", developerDir)
		}
		
		baseInvocation
	}

	
}

