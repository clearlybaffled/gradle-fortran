package io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.intel


import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal
import org.gradle.nativeplatform.toolchain.CommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.NativePlatformToolChain
import org.gradle.nativeplatform.toolchain.NativeToolChain
import org.gradle.nativeplatform.toolchain.internal.ExtendableToolChain
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SymbolExtractorOsConfig
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.UnavailablePlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.UnsupportedPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.TargetPlatformConfiguration
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain.CompilerMetaDataProviderWithDefaultArgs
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.nativeplatform.toolchain.internal.tools.DefaultCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.platform.base.internal.toolchain.ToolChainAvailability
import org.gradle.process.internal.ExecActionFactory


interface IntelFortran extends NativeToolChain {
    void setIntelHome(Object path)
    File getIntelHome()
}

/*
 * Adds Fortran as a language option to Gcc toolchain
 */
public class IntelFortranToolChain extends ExtendableToolChain<IntelFortranPlatformToolChain> implements IntelFortran {

	public static final String DEFAULT_NAME = "ifort"
	private final List<TargetPlatformConfiguration> platformConfigs = []
	private final Map<NativePlatform, PlatformToolProvider> toolProviders = [:]
	private final Instantiator instantiator
	private final SystemLibraryDiscovery standardLibraryDiscovery
	private final ToolSearchPath toolSearchPath
	private final ExecActionFactory execActionFactory
	private final WorkerLeaseService workerLeaseService
	private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
    private final ExecActionFactory
    private final MetaDa
    private File intelHome

		
	public IntelFortranToolChain(Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, WorkerLeaseService workerLeaseService) {
		super(name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
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
    public PlatformToolProvider select(NativePlatformInternal targetPlatform) {
         select(NativeLanguage.ANY, targetPlatform)
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
			DefaultIntelFortranPlatformToolChain configurableToolChain = instantiator.newInstance(DefaultIntelFortranPlatformToolChain, targetPlatform)
			configureDefaultTools(configurableToolChain)
			targetPlatformConfigurationConfiguration.apply(configurableToolChain)
			configureActions.execute(configurableToolChain)
			//configurableToolChain.compilerProbeArgs(standardLibraryDiscovery.compilerProbeArgs(targetPlatform))
	
			ToolChainAvailability result = new ToolChainAvailability()
            toolSearchPath.path(getIntelHome())
            CommandLineToolSearchResult compiler = toolSearchPath.locate(ToolType.C_COMPILER, "ifort")
            result.mustBeAvailable(compiler)
			if (!result.isAvailable()) {
				 new UnavailablePlatformToolProvider(targetPlatform.operatingSystem, result)
			} else {
				new IntelFortranPlatformToolProvider(buildOperationExecutor, targetPlatform.operatingSystem, toolSearchPath, configurableToolChain, execActionFactory, compilerOutputFileNamingSchemeFactory, configurableToolChain.canUseCommandFile, workerLeaseService, new CompilerMetaDataProviderWithDefaultArgs(configurableToolChain.getCompilerProbeArgs(), metaDataProvider))
			}
		} else {
			new UnsupportedPlatformToolProvider(targetPlatform.operatingSystem, String.format("Don't know how to build for %s.", targetPlatform.displayName))
		}
	}

    protected void configureDefaultTools(DefaultIntelFortranPlatformToolChain toolChain) {
        toolChain.add(instantiator.newInstance(DefaultCommandLineToolConfiguration, ToolType.C_COMPILER, "gfortran"))
        toolChain.add(instantiator.newInstance(DefaultCommandLineToolConfiguration, ToolType.LINKER, "gfortran"))
        toolChain.add(instantiator.newInstance(DefaultCommandLineToolConfiguration, ToolType.STATIC_LIB_ARCHIVER, "ar"))
        toolChain.add(instantiator.newInstance(DefaultCommandLineToolConfiguration, ToolType.SYMBOL_EXTRACTOR, SymbolExtractorOsConfig.current().getExecutableName()))
        toolChain.add(instantiator.newInstance(DefaultCommandLineToolConfiguration, ToolType.STRIPPER, "strip"))
        
    }
    @Override
    protected String getTypeName() {
        "Intel Fortran Compiler"
    }

    @Override
    public void setIntelHome(Object path) {
        intelHome = resolve(path)
    }

    @Override
    public File getIntelHome() {
        intelHome ?: guessIntelHome() 
    }

    private File guessIntelHome() {
        intelHome = resolve(System.getenv('INTEL_HOME'))
                        ?: resolve(System.getenv('IFORT_COMPILER11'))
                        ?: resolve(System.getenv('IFORT_COMPILER10'))
                        ?: resolve(System.getenv('IFORT_COMPILER9'))
                        ?: resolve(System.getenv('IFORT_COMPILER8'))
    }
	
}

interface IntelFortranPlatformToolChain extends NativePlatformToolChain {

    /**
     * Returns the compiler tool.
     */
    CommandLineToolConfiguration getCompiler();

    /**
     * Returns the linker tool.
     */
    CommandLineToolConfiguration getLinker();

    /**
     * Returns the settings to use for the archiver.
     *
     */
    CommandLineToolConfiguration getStaticLibArchiver();

    /**
     * Returns the tool for extracting symbols.
     *
     */
    CommandLineToolConfiguration getSymbolExtractor();

    /**
     * Returns the tool for stripping symbols.
     *
     */
    CommandLineToolConfiguration getStripper();
    
}

class DefaultIntelFortranPlatformToolChain implements IntelFortranPlatformToolChain {
    
    NativePlatform platform
    def tools = [:]
    
	public IFortPlatformToolChain(NativePlatform platform) {
		this.platform = platform
	}

    public void add(DefaultCommandLineToolConfiguration tool) {
        tools.put(tool.getToolType(), tool);
    }

    @Override
    public CommandLineToolConfiguration getCompiler() {
        return tools.get(ToolType.C_COMPILER);
    }

    @Override
    public CommandLineToolConfiguration getLinker() {
        return tools.get(ToolType.LINKER);
    }

    @Override
    public CommandLineToolConfiguration getStaticLibArchiver() {
        return tools.get(ToolType.STATIC_LIB_ARCHIVER);
    }

    @Override
    public CommandLineToolConfiguration getSymbolExtractor() {
        return tools.get(ToolType.SYMBOL_EXTRACTOR);
    }

    @Override
    public CommandLineToolConfiguration getStripper() {
        return tools.get(ToolType.STRIPPER);
    }

	
}

