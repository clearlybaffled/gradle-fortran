package io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran;

import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.language.base.internal.compile.CompileSpec;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.language.base.internal.compile.CompilerUtil;
import org.gradle.language.base.internal.compile.DefaultCompilerVersion
import org.gradle.language.base.internal.compile.VersionAwareCompiler
import org.gradle.nativeplatform.internal.BinaryToolSpec
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.internal.LinkerSpec;
import org.gradle.nativeplatform.internal.StaticLibraryArchiverSpec;
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal;
import org.gradle.nativeplatform.toolchain.internal.AbstractPlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.DefaultCommandLineToolInvocationWorker;
import org.gradle.nativeplatform.toolchain.internal.DefaultMutableCommandLineToolContext;
import org.gradle.nativeplatform.toolchain.internal.EmptySystemLibraries
import org.gradle.nativeplatform.toolchain.internal.MutableCommandLineToolContext;
import org.gradle.nativeplatform.toolchain.internal.OutputCleaningCompiler;
import org.gradle.nativeplatform.toolchain.internal.Stripper;
import org.gradle.nativeplatform.toolchain.internal.SymbolExtractor;
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.nativeplatform.toolchain.internal.gcc.ArStaticLibraryArchiver;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.GccMetadata;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetadata
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider.CompilerExecSpec;
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult;
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal;
import org.gradle.nativeplatform.toolchain.internal.tools.ToolRegistry;
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath;
import org.gradle.platform.base.internal.toolchain.ComponentNotFound;
import org.gradle.platform.base.internal.toolchain.SearchResult;
import org.gradle.process.internal.ExecActionFactory;

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.FortranCompileSpec;

class GFortranPlatformToolProvider extends AbstractPlatformToolProvider {
	
	private final ToolSearchPath toolSearchPath
	private final ToolRegistry toolRegistry
	private final ExecActionFactory execActionFactory
	private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
	private final boolean useCommandFile
	private final WorkerLeaseService workerLeaseService
	private final CompilerMetaDataProvider<GccMetadata> metadataProvider

	GFortranPlatformToolProvider(BuildOperationExecutor buildOperationExecutor, OperatingSystemInternal targetOperatingSystem, ToolSearchPath toolSearchPath, ToolRegistry toolRegistry, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, boolean useCommandFile, WorkerLeaseService workerLeaseService, CompilerMetaDataProvider<GccMetadata> metadataProvider) {
		super(buildOperationExecutor, targetOperatingSystem)
		this.toolRegistry = toolRegistry
		this.toolSearchPath = toolSearchPath
		this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory
		this.useCommandFile = useCommandFile
		this.execActionFactory = execActionFactory
		this.workerLeaseService = workerLeaseService
		this.metadataProvider = metadataProvider
	}

	@Override
	public <T extends CompileSpec> Compiler<T> newCompiler(Class<T> spec) {
		FortranCompileSpec.isAssignableFrom(spec) ? CompilerUtil.castCompiler(createFortranCompiler()) : super.newCompiler(spec)
	}

	@Override
	protected Compiler<LinkerSpec> createLinker() {
		def linkerTool = toolRegistry.getTool(ToolType.LINKER)
		versionAwareCompiler(new GFortranLinker(buildOperationExecutor, commandLineTool(linkerTool), context(linkerTool), useCommandFile, workerLeaseService), ToolType.LINKER);
	}
	
	@Override
	protected Compiler<StaticLibraryArchiverSpec> createStaticLibraryArchiver() {
		def staticLibArchiverTool = toolRegistry.getTool(ToolType.STATIC_LIB_ARCHIVER);
		new ArStaticLibraryArchiver(buildOperationExecutor, commandLineTool(staticLibArchiverTool), context(staticLibArchiverTool), workerLeaseService);
	}

	@Override
	protected Compiler<?> createSymbolExtractor() {
		def symbolExtractor = toolRegistry.getTool(ToolType.SYMBOL_EXTRACTOR);
		new SymbolExtractor(buildOperationExecutor, commandLineTool(symbolExtractor), context(symbolExtractor), workerLeaseService);
	}

	@Override
	protected Compiler<?> createStripper() {
		def stripper = toolRegistry.getTool(ToolType.STRIPPER);
		new Stripper(buildOperationExecutor, commandLineTool(stripper), context(stripper), workerLeaseService);
	}

	protected Compiler<FortranCompileSpec> createFortranCompiler() {
		GccCommandLineToolConfigurationInternal compilerTool = toolRegistry.getTool(ToolType.C_COMPILER)
		GFortranCompiler compiler = new GFortranCompiler(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineTool(compilerTool), context(compilerTool), getObjectFileExtension(), useCommandFile, workerLeaseService) 
		OutputCleaningCompiler<FortranCompileSpec> outputCleaningCompiler = new OutputCleaningCompiler<FortranCompileSpec>(compiler, compilerOutputFileNamingSchemeFactory, getObjectFileExtension())
		versionAwareCompiler(outputCleaningCompiler, ToolType.C_COMPILER)	
	}

	private <T extends BinaryToolSpec> VersionAwareCompiler<T> versionAwareCompiler(Compiler<T> compiler, ToolType toolType) {
		def gccMetadata = getGccMetadata(toolType)
		new VersionAwareCompiler<T>(compiler,
			new DefaultCompilerVersion(
				metadataProvider.compilerType.identifier, 
				gccMetadata.component.vendor, 
				gccMetadata.component.version))
	}
	
	private SearchResult<GccMetadata> getGccMetadata(ToolType compilerType) {
		GccCommandLineToolConfigurationInternal compiler = toolRegistry.getTool(compilerType);
		if (compiler == null) {
			return new ComponentNotFound<GccMetadata>("Tool " + compilerType.getToolName() + " is not available");
		}
		def searchResult = toolSearchPath.locate(compiler.toolType, compiler.executable)
		
		metadataProvider.getCompilerMetaData(toolSearchPath.path) { CompilerExecSpec spec ->
				spec.executable(searchResult.tool)
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


	@Override
	public SystemLibraries getSystemLibraries(ToolType compilerType) {
	    def gccMetadata = getGccMetadata(compilerType);
        gccMetadata.isAvailable() ? gccMetadata.getComponent().getSystemLibraries() : new EmptySystemLibraries()
	}


	@Override
	public CommandLineToolSearchResult locateTool(ToolType compilerType) {
		toolSearchPath.locate(compilerType, toolRegistry.getTool(compilerType).executable)
	}
	
	@Override
	public CompilerMetadata getCompilerMetadata(ToolType toolType) {
		getGccMetadata(toolType).component
	}
	
}