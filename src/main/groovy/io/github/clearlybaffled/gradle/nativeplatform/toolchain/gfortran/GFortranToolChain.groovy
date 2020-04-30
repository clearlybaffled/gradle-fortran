package io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran

import static org.gradle.nativeplatform.toolchain.internal.NativeLanguage.*

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SymbolExtractorOsConfig
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.UnavailablePlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.UnsupportedPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.GccToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.TargetPlatformConfiguration
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain.CompilerMetaDataProviderWithDefaultArgs
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultGccCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.platform.base.internal.toolchain.ToolChainAvailability
import org.gradle.process.internal.ExecActionFactory

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
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration, ToolType.C_COMPILER, "gfortran"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration, ToolType.LINKER, "gfortran")) 
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration, ToolType.STATIC_LIB_ARCHIVER, "ar"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration, ToolType.SYMBOL_EXTRACTOR, SymbolExtractorOsConfig.current().getExecutableName()))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration, ToolType.STRIPPER, "strip"))
		
	}
		
	@Override
	public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
		if (sourceLanguage == NativeLanguage.ANY) {
			def toolProvider = toolProviders.get(targetMachine)
			if (!toolProvider) {
				toolProvider = createPlatformToolProvider(targetMachine)
				toolProviders.put(targetMachine, toolProvider)
			}
			def compiler = toolProvider.locateTool(ToolType.C_COMPILER) 
			if (compiler?.isAvailable()) {
				toolProvider
			} else {
				new UnavailablePlatformToolProvider(targetMachine.getOperatingSystem(), compiler)
			}
		} else {
			new UnsupportedPlatformToolProvider(targetMachine.operatingSystem, String.format("Don't know how to compile language %s.", sourceLanguage))
		}
	}
	
	private PlatformToolProvider createPlatformToolProvider(NativePlatformInternal targetPlatform) {
		TargetPlatformConfiguration targetPlatformConfigurationConfiguration = getPlatformConfiguration(targetPlatform)
		if (targetPlatformConfigurationConfiguration) {
			DefaultGccPlatformToolChain configurableToolChain = instantiator.newInstance(GFortranPlatformToolChain, targetPlatform)
			configureDefaultTools(configurableToolChain)
			targetPlatformConfigurationConfiguration.apply(configurableToolChain)
			configureActions.execute(configurableToolChain)
			configurableToolChain.compilerProbeArgs(standardLibraryDiscovery.compilerProbeArgs(targetPlatform))
	
			ToolChainAvailability result = new ToolChainAvailability()
			initTools(configurableToolChain, result)
			
			if (!result.isAvailable()) {
				 new UnavailablePlatformToolProvider(targetPlatform.operatingSystem, result)
			} else {
				new GFortranPlatformToolProvider(buildOperationExecutor, targetPlatform.operatingSystem, toolSearchPath, configurableToolChain, execActionFactory, compilerOutputFileNamingSchemeFactory, configurableToolChain.canUseCommandFile, workerLeaseService, new CompilerMetaDataProviderWithDefaultArgs(configurableToolChain.getCompilerProbeArgs(), metaDataProvider))
			}
		} else {
			new UnsupportedPlatformToolProvider(targetPlatform.operatingSystem, String.format("Don't know how to build for %s.", targetPlatform.displayName))
		}
	}

	
}

class GFortranPlatformToolChain extends DefaultGccPlatformToolChain {

	def dummy
	public GFortranPlatformToolChain(NativePlatform platform) {
		super(platform)
		this.dummy = new DefaultGccCommandLineToolConfiguration(ToolType.LINKER, "/bin/true")
	}

	@Override
	public Collection<GccCommandLineToolConfigurationInternal> getCompilers() {
		[getcCompiler()]
	}
	
	public GccCommandLineToolConfigurationInternal getCompiler() {
		getcCompiler()
	}

	@Override
	public GccCommandLineToolConfigurationInternal getCppCompiler() {
		dummy
	}
	@Override
	public GccCommandLineToolConfigurationInternal getObjcCompiler() {
		dummy
	}
	@Override
	public GccCommandLineToolConfigurationInternal getObjcppCompiler() {
		dummy
	}

	@Override
	public GccCommandLineToolConfigurationInternal getAssembler() {
		dummy
	}
	
	
}

