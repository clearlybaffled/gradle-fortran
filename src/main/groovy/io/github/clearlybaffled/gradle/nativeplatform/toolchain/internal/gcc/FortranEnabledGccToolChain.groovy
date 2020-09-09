package io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.gcc

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.GnuFortran
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
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.UnavailablePlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.UnsupportedPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain
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
 * Adds Fortran as a language option to Gcc toolchain
 */
public class FortranEnabledGccToolChain extends GccToolChain implements GnuFortran {

    public static final String DEFAULT_NAME = "gfortran"
    private final List<TargetPlatformConfiguration> platformConfigs = []
    private final Map<NativePlatform, PlatformToolProvider> toolProviders = [:]
    private final Instantiator instantiator
    private final SystemLibraryDiscovery standardLibraryDiscovery
    private final ToolSearchPath toolSearchPath
    private final ExecActionFactory execActionFactory
    private final WorkerLeaseService workerLeaseService
    private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory

    public FortranEnabledGccToolChain(Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, WorkerLeaseService workerLeaseService) {
        super(instantiator, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
                compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
        this.platformConfigs = platformConfigs
        this.instantiator = instantiator
        this.standardLibraryDiscovery = standardLibraryDiscovery
        this.toolSearchPath = new ToolSearchPath(operatingSystem)
        this.execActionFactory = execActionFactory
        this.workerLeaseService = workerLeaseService
        this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory

        //target("linux-x86_64")
    }

    @Override
    public PlatformToolProvider select(NativePlatformInternal targetMachine) {
        select(NativeLanguage.ANY, targetMachine)
    }
    
    @Override
    public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
        if (sourceLanguage == NativeLanguage.ANY) {
            def toolProvider = super.select(targetMachine)
            def decoratedToolProvider = createPlatformToolProvider(targetMachine, toolProvider)
            def compiler = decoratedToolProvider.locateTool(ToolType.C_COMPILER)
            if (compiler?.isAvailable()) {
                toolProviders.put(targetMachine, decoratedToolProvider)
            } else {
                toolProvider
            }
        } else {
            super.select(sourceLanguage, targetMachine)
        }
    }

    private PlatformToolProvider createPlatformToolProvider(NativePlatformInternal targetPlatform, PlatformToolProvider toolProvider) {
        TargetPlatformConfiguration targetPlatformConfigurationConfiguration = getPlatformConfiguration(targetPlatform)
        if (targetPlatformConfigurationConfiguration) {
            DefaultGccPlatformToolChain configurableToolChain = instantiator.newInstance(GFortranPlatformToolChain, targetPlatform)
            configurableToolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.C_COMPILER, "gcc"))
            configureDefaultTools(configurableToolChain)
            targetPlatformConfigurationConfiguration.apply(configurableToolChain)
            configureActions.execute(configurableToolChain)
            configurableToolChain.compilerProbeArgs("-x f77", "-cpp")
            configurableToolChain.compilerProbeArgs(standardLibraryDiscovery.compilerProbeArgs(targetPlatform))

            ToolChainAvailability result = new ToolChainAvailability()
            initTools(configurableToolChain, result)

            if (!result.isAvailable()) {
                 new UnavailablePlatformToolProvider(targetPlatform.operatingSystem, result)
            } else {
                new FortranEnabledGccPlatformToolProvider(toolProvider, buildOperationExecutor, targetPlatform.operatingSystem, toolSearchPath, configurableToolChain, execActionFactory, compilerOutputFileNamingSchemeFactory, configurableToolChain.canUseCommandFile, workerLeaseService, new CompilerMetaDataProviderWithDefaultArgs(configurableToolChain.getCompilerProbeArgs(), metaDataProvider))
            }
        } else {
            new UnsupportedPlatformToolProvider(targetPlatform.operatingSystem, String.format("Don't know how to build for %s.", targetPlatform.displayName))
        }
    }
}

class GFortranPlatformToolChain extends DefaultGccPlatformToolChain {

    public GFortranPlatformToolChain(NativePlatform platform) {
        super(platform)
    }

    @Override
    public Collection<GccCommandLineToolConfigurationInternal> getCompilers() {
        [fortranCompiler]
    }

    public GccCommandLineToolConfigurationInternal getFortranCompiler() {
        super.metaClass.@tools.get(ToolType.C_COMPILER);
    }
    
}

