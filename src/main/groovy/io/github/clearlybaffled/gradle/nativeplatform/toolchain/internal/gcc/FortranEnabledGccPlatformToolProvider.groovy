package io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.gcc

import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.language.base.internal.compile.CompileSpec
import org.gradle.language.base.internal.compile.Compiler
import org.gradle.language.base.internal.compile.CompilerUtil
import org.gradle.language.base.internal.compile.DefaultCompilerVersion
import org.gradle.language.base.internal.compile.VersionAwareCompiler
import org.gradle.nativeplatform.internal.BinaryToolSpec
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal
import org.gradle.nativeplatform.toolchain.internal.AbstractPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.DefaultCommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.DefaultMutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.MutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.OutputCleaningCompiler
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.GccMetadata
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetadata
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider.CompilerExecSpec
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.nativeplatform.toolchain.internal.tools.ToolRegistry
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.platform.base.internal.toolchain.ComponentNotFound
import org.gradle.platform.base.internal.toolchain.SearchResult
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.compilespec.FortranCompileSpec

class FortranEnabledGccPlatformToolProvider extends AbstractPlatformToolProvider {

    private final ToolSearchPath toolSearchPath
    private final ToolRegistry toolRegistry
    private final ExecActionFactory execActionFactory
    private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
    private final boolean useCommandFile
    private final WorkerLeaseService workerLeaseService
    private final CompilerMetaDataProvider<GccMetadata> metadataProvider
    private final PlatformToolProvider delegate


    FortranEnabledGccPlatformToolProvider(PlatformToolProvider toolProvider, BuildOperationExecutor buildOperationExecutor, OperatingSystemInternal targetOperatingSystem, ToolSearchPath toolSearchPath, ToolRegistry toolRegistry, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, boolean useCommandFile, WorkerLeaseService workerLeaseService, CompilerMetaDataProvider<GccMetadata> metadataProvider) {
        super(buildOperationExecutor, targetOperatingSystem)
        this.toolRegistry = toolRegistry
        this.toolSearchPath = toolSearchPath
        this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory
        this.useCommandFile = useCommandFile
        this.execActionFactory = execActionFactory
        this.workerLeaseService = workerLeaseService
        this.metadataProvider = metadataProvider

        this.delegate = toolProvider
        
    }

    def methodMissing(String name, Object args) {
        delegate."$name"(args)
    }


    @Override
    public <T extends CompileSpec> Compiler<T> newCompiler(Class<T> spec) {
        FortranCompileSpec.isAssignableFrom(spec) ? CompilerUtil.castCompiler(createFortranCompiler()) : delegate.newCompiler(spec)
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
        GccCommandLineToolConfigurationInternal compiler = toolRegistry.getTool(compilerType)
        if (compiler == null) {
            return new ComponentNotFound<GccMetadata>("Tool " + compilerType.getToolName() + " is not available")
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
        delegate.getSystemLibraries(compilerType)
    }


    @Override
    public CommandLineToolSearchResult locateTool(ToolType compilerType) {
        delegate.locateTool(compilerType)
    }

    @Override
    public CompilerMetadata getCompilerMetadata(ToolType toolType) {
        delegate.getCompilerMetadata(toolType)
    }

}
