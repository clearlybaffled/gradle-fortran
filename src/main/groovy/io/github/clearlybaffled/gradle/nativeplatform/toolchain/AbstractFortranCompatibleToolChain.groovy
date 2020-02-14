package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import javax.annotation.Nullable

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SymbolExtractorOsConfig
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.UnavailablePlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.UnsupportedPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.GccPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.TargetPlatformConfiguration
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain.CompilerMetaDataProviderWithDefaultArgs
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.GccMetadata
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultGccCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.platform.base.internal.toolchain.ToolChainAvailability
import org.gradle.process.internal.ExecActionFactory

abstract class AbstractFortranCompatibleToolChain extends AbstractGccCompatibleToolChain {

	private final List<TargetPlatformConfiguration> platformConfigs = []
	private final Map<NativePlatform, PlatformToolProvider> toolProviders = [:]
	private final Instantiator instantiator
	private final SystemLibraryDiscovery standardLibraryDiscovery
	private final ToolSearchPath toolSearchPath
	
	//(String, org.gradle.internal.operations.DelegatingBuildOperationExecutor, org.gradle.internal.os.OperatingSystem$Linux, org.gradle.api.internal.file.BaseDirFileResolver, org.gradle.process.internal.DefaultExecActionFactory$DecoratingExecActionFactory, org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory, org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory$CachingCompilerMetaDataProvider, org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery, org.gradle.internal.instantiation.generator.DependencyInjectingInstantiator, org.gradle.internal.work.StopShieldingWorkerLeaseService)
	
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
	}

	@Override
	public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
		if (sourceLanguage == ANY) {
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
			FortranPlatformToolChain configurableToolChain = instantiator.newInstance(FortranPlatformToolChain, targetPlatform)		
			configureDefaultTools(configurableToolChain)
			targetPlatformConfigurationConfiguration.apply(configurableToolChain)
			configureActions.execute(configurableToolChain)
			configurableToolChain.compilerProbeArgs(standardLibraryDiscovery.compilerProbeArgs(targetPlatform))
	
			ToolChainAvailability result = new ToolChainAvailability()
			initTools(configurableToolChain, result)
			if (!result.isAvailable()) {
				return new UnavailablePlatformToolProvider(targetPlatform.operatingSystem, result)
			}
	
			return new GccPlatformToolProvider(buildOperationExecutor, targetPlatform.operatingSystem, toolSearchPath, configurableToolChain, execActionFactory, compilerOutputFileNamingSchemeFactory, configurableToolChain.isCanUseCommandFile(), workerLeaseService, new CompilerMetaDataProviderWithDefaultArgs(configurableToolChain.getCompilerProbeArgs(), metaDataProvider))
		} else {
			new UnsupportedPlatformToolProvider(targetPlatform.operatingSystem, String.format("Don't know how to build for %s.", targetPlatform.displayName))
		}
	}


	protected void initForImplementation(FortranPlatformToolChain platformToolChain, GccMetadata versionResult) {
	}


	abstract protected void configureDefaultTools(FortranPlatformToolChain toolChain) 
	
}
