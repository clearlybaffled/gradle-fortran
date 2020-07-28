package io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran

import org.gradle.api.Action
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain
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
 * Needs to extend {@link Gcc} because {@link AbstractNativeCompileTask} is looking for an instanceof a {@link Gcc}
 */
interface GFortran extends Gcc {}

/*
 * Adds Fortran as a language option to Gcc toolchain
 */
public class FortranEnabledGccToolChain extends GccToolChain implements Gcc {

    public static final String DEFAULT_NAME = "gfortran"
    private final List<TargetPlatformConfiguration> platformConfigs = []
    private final Map<NativePlatform, PlatformToolProvider> toolProviders = [:]
    private final Instantiator instantiator
    private final SystemLibraryDiscovery standardLibraryDiscovery
    private final ToolSearchPath toolSearchPath
    private final ExecActionFactory execActionFactory
    private final WorkerLeaseService workerLeaseService
    private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
    private final AbstractGccCompatibleToolChain delegate

    public FortranEnabledGccToolChain(GccToolChain delegate, Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, WorkerLeaseService workerLeaseService) {
        super(instantiator, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
                compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
        this.platformConfigs = platformConfigs
        this.instantiator = instantiator
        this.standardLibraryDiscovery = standardLibraryDiscovery
        this.toolSearchPath = new ToolSearchPath(operatingSystem)
        this.execActionFactory = execActionFactory
        this.workerLeaseService = workerLeaseService
        this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory
        this.delegate = delegate ?: this

        //target("linux-x86_64")
    }

    @Override
    public PlatformToolProvider select(NativePlatformInternal targetMachine) {
        select(NativeLanguage.ANY, targetMachine)
    }
    
    @Override
    public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
        if (sourceLanguage == NativeLanguage.FORTRAN) {
            def toolProvider = delegate.select(targetMachine)

            def compiler = toolProvider.locateTool(ToolType.FORTRAN_COMPILER)
            if (compiler?.isAvailable()) {
                def decoratedToolProvider = createPlatformToolProvider(targetMachine, toolProvider)
                delegate.@toolProviders.put(targetMachine, decoratedToolProvider)
                decoratedToolProvider
            } else {
                toolProvider
            }
        } else {
            delegate.select(sourceLanguage, targetMachine)
        }
    }

    private PlatformToolProvider createPlatformToolProvider(NativePlatformInternal targetPlatform, PlatformToolProvider toolProvider) {
        TargetPlatformConfiguration targetPlatformConfigurationConfiguration = getPlatformConfiguration(targetPlatform)
        if (targetPlatformConfigurationConfiguration) {
            DefaultGccPlatformToolChain configurableToolChain = instantiator.newInstance(GFortranPlatformToolChain, targetPlatform)
            configurableToolChain.add(instantiator.newInstance(DefaultGccCommandLineToolConfiguration.class, ToolType.FORTRAN_COMPILER, "gcc"))
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

    public String getName() {
         delegate.getName()
    }

    public String getDisplayName() {
         delegate.getDisplayName()
    }

    public String getOutputType() {
         delegate.getOutputType()
    }

    public void eachPlatform(Action<? super GccPlatformToolChain> action) {
        delegate.eachPlatform(action)
    }

    public void assertSupported() {
        delegate.assertSupported()
    }

    public List<File> getPath() {
         delegate.getPath()
    }

    public void path(Object... pathEntries) {
        delegate.path(pathEntries)
    }

    public void target(String platformName) {
        delegate.target(platformName)
    }

    public void target(String platformName, Action<? super GccPlatformToolChain> action) {
        delegate.target(platformName, action)
    }

    public void target(List<String> platformNames, Action<? super GccPlatformToolChain> action) {
        delegate.target(platformNames, action)
    }

    public void setTargets(String... platformNames) {
        delegate.setTargets(platformNames)
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
        super.metaClass.@tools.get(ToolType.FORTRAN_COMPILER);
    }
    
}

