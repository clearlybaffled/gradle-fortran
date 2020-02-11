package io.github.clearlybaffled.gradle.nativeplugin.toolchain

import javax.annotation.Nullable

import org.gradle.internal.reflect.Instantiator
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
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultGccCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.platform.base.internal.toolchain.ToolChainAvailability



abstract class AbstractFortranCompatibleToolChain extends AbstractGccCompatibleToolChain {

	private final List<TargetPlatformConfiguration> platformConfigs = []
	private final Map<NativePlatform, PlatformToolProvider> toolProviders = [:]
	private final Instantiator instantiator
	private final SystemLibraryDiscovery standardLibraryDiscovery
	
	@Override
	public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
		if (sourceLanguage == ANY) {
			getProviderForPlatform(targetMachine).locateTool(ToolType.C_COMPILER) ?:
				new UnavailablePlatformToolProvider(targetMachine.operatingSystem, ToolType.C_COMPILER)
		} else {
			 new UnsupportedPlatformToolProvider(targetMachine.operatingSystem, String.format("Don't know how to compile language %s.", sourceLanguage))
		}
	}
	
	private PlatformToolProvider getProviderForPlatform(NativePlatformInternal targetPlatform) {
		toolProviders.get(targetPlatform) ?: toolProviders.put(targetPlatform, createPlatformToolProvider(targetPlatform))
	}

	
	private PlatformToolProvider createPlatformToolProvider(NativePlatformInternal targetPlatform) {
		TargetPlatformConfiguration targetPlatformConfigurationConfiguration = getPlatformConfiguration(targetPlatform)
		if (targetPlatformConfigurationConfiguration) {
			DefaultFortranPlatformToolChain configurableToolChain = instantiator.newInstance(FortranPlatformToolChain, targetPlatform)
			addDefaultTools(configurableToolChain)
			configureDefaultTools(configurableToolChain)
			targetPlatformConfigurationConfiguration.apply(configurableToolChain)
			configureActions.execute(configurableToolChain)
			configurableToolChain.compilerProbeArgs(standardLibraryDiscovery.compilerProbeArgs(targetPlatform))
	
			ToolChainAvailability result = new ToolChainAvailability()
			initTools(configurableToolChain, result)
			if (!result.isAvailable()) {
				return new UnavailablePlatformToolProvider(targetPlatform.getOperatingSystem(), result)
			}
	
			return new GccPlatformToolProvider(buildOperationExecutor, targetPlatform.getOperatingSystem(), toolSearchPath, configurableToolChain, execActionFactory, compilerOutputFileNamingSchemeFactory, configurableToolChain.isCanUseCommandFile(), workerLeaseService, new CompilerMetaDataProviderWithDefaultArgs(configurableToolChain.getCompilerProbeArgs(), metaDataProvider))
		} else {
			new UnsupportedPlatformToolProvider(targetPlatform.operatingSystem, String.format("Don't know how to build for %s.", targetPlatform.displayName))
		}
	}

	protected void initTools(DefaultGccPlatformToolChain platformToolChain, ToolChainAvailability availability) {
		// Attempt to determine whether the compiler is the correct implementation
		for (GccCommandLineToolConfigurationInternal tool : platformToolChain.getCompilers()) {
			CommandLineToolSearchResult compiler = locate(tool)
			if (compiler.isAvailable()) {
				SearchResult<GccMetadata> gccMetadata = getMetaDataProvider().getCompilerMetaData(toolSearchPath.getPath(), spec -> spec.executable(compiler.getTool()).args(platformToolChain.getCompilerProbeArgs()))
				availability.mustBeAvailable(gccMetadata)
				if (!gccMetadata.isAvailable()) {
					return
				}
				// Assume all the other compilers are ok, if they happen to be installed
				LOGGER.debug("Found {} with version {}", tool.getToolType().getToolName(), gccMetadata)
				initForImplementation(platformToolChain, gccMetadata.getComponent())
				break
			}
		}
	}

	protected void initForImplementation(DefaultGccPlatformToolChain platformToolChain, GccMetadata versionResult) {
	}

	private void addDefaultTools(DefaultGccPlatformToolChain toolChain) {
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.C_COMPILER, "gcc"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.CPP_COMPILER, "g++"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.LINKER, "g++"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.STATIC_LIB_ARCHIVER, "ar"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.OBJECTIVECPP_COMPILER, "g++"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.OBJECTIVEC_COMPILER, "gcc"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.ASSEMBLER, "gcc"))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.SYMBOL_EXTRACTOR, SymbolExtractorOsConfig.current().getExecutableName()))
		toolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.STRIPPER, "strip"))
	}

	protected void configureDefaultTools(DefaultGccPlatformToolChain toolChain) {
	}

	@Nullable
	protected TargetPlatformConfiguration getPlatformConfiguration(NativePlatformInternal targetPlatform) {
		platformConfigs.find { it.supportsPlatform(targetPlatform) }
	}
}
