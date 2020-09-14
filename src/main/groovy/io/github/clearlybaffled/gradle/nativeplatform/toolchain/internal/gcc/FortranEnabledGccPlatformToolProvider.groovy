package io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.gcc

import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.language.base.internal.compile.CompileSpec
import org.gradle.language.base.internal.compile.Compiler
import org.gradle.language.base.internal.compile.CompilerUtil
import org.gradle.language.base.internal.compile.DefaultCompilerVersion
import org.gradle.language.base.internal.compile.VersionAwareCompiler
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal
import org.gradle.nativeplatform.toolchain.internal.AbstractPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.DefaultCommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.DefaultMutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.EmptySystemLibraries
import org.gradle.nativeplatform.toolchain.internal.MutableCommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.OutputCleaningCompiler
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.gcc.GccPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.GccMetadata
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetadata
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider.CompilerExecSpec
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult
import org.gradle.nativeplatform.toolchain.internal.tools.GccCommandLineToolConfigurationInternal
import org.gradle.platform.base.internal.toolchain.ComponentNotFound
import org.gradle.platform.base.internal.toolchain.SearchResult

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.compilespec.FortranCompileSpec

class FortranEnabledGccPlatformToolProvider extends AbstractPlatformToolProvider {

    
    private final FortranEnabledGccPlatformToolChain toolRegistry
    private final GccPlatformToolProvider delegate


    FortranEnabledGccPlatformToolProvider(BuildOperationExecutor buildOperationExecutor, OperatingSystemInternal targetOperatingSystem, PlatformToolProvider toolProvider, FortranEnabledGccPlatformToolChain toolRegistry) {
        super(buildOperationExecutor, targetOperatingSystem)
        this.toolRegistry = toolRegistry
        this.delegate = toolProvider as GccPlatformToolProvider
        
    }

    def methodMissing(String name, Object args) {
        delegate."$name"(args)
    }


    @Override
    public <T extends CompileSpec> Compiler<T> newCompiler(Class<T> spec) {
        FortranCompileSpec.isAssignableFrom(spec) ? CompilerUtil.castCompiler(createFortranCompiler()) : delegate.newCompiler(spec)
    }

    protected Compiler<FortranCompileSpec> createFortranCompiler() {
        GccCommandLineToolConfigurationInternal compilerTool = toolRegistry.fortranCompiler
        FortranCompiler compiler = new FortranCompiler(buildOperationExecutor, delegate.@compilerOutputFileNamingSchemeFactory, commandLineTool(compilerTool), context(compilerTool), getObjectFileExtension(), delegate.@useCommandFile, delegate.@workerLeaseService)
        OutputCleaningCompiler<FortranCompileSpec> outputCleaningCompiler = new OutputCleaningCompiler<FortranCompileSpec>(compiler, delegate.@compilerOutputFileNamingSchemeFactory, getObjectFileExtension())

        def gccMetadata = getGccMetadata(compilerTool)
        
        new VersionAwareCompiler<FortranCompileSpec>(
            compiler,
            new DefaultCompilerVersion(
                delegate.@metadataProvider.compilerType.identifier,
                gccMetadata.component.vendor,
                gccMetadata.component.version)
            )
    }

    private SearchResult<GccMetadata> getGccMetadata() {
        GccCommandLineToolConfigurationInternal compiler = toolRegistry.fortranCompiler
        if (compiler == null) {
            return new ComponentNotFound<GccMetadata>("Tool $compiler.toolType.toolName is not available")
        }
        def searchResult = delegate.@toolSearchPath.locate(compiler.toolType, compiler.executable)

        delegate.@metadataProvider.getCompilerMetaData(delegate.@toolSearchPath.path) { CompilerExecSpec spec ->
            spec.executable(searchResult.tool)
        }
    }

    def commandLineTool(GccCommandLineToolConfigurationInternal tool) {
        ToolType key = tool.toolType
        String exeName = tool.executable
        new DefaultCommandLineToolInvocationWorker(key.toolName, delegate.@toolSearchPath.locate(key, exeName).tool, delegate.@execActionFactory)
    }

    def context(GccCommandLineToolConfigurationInternal toolConfiguration) {
        MutableCommandLineToolContext baseInvocation = new DefaultMutableCommandLineToolContext()
        // MinGW requires the path to be set
        baseInvocation.addPath(delegate.@toolSearchPath.path)
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
        if (compilerType) {
            delegate.getSystemLibraries(compilerType)
        } else {
            final SearchResult<GccMetadata> gccMetadata = getGccMetadata();
            if (gccMetadata.isAvailable()) {
                gccMetadata.component.systemLibraries
            }
            new EmptySystemLibraries()
        }
    }


    @Override
    public CommandLineToolSearchResult locateTool(ToolType compilerType) {
        compilerType ? delegate.locateTool(compilerType) : delegate.@toolSearchPath.locate(compilerType, toolRegistry.fortranCompiler.executable)
    }

    @Override
    public CompilerMetadata getCompilerMetadata(ToolType toolType) {
        toolType ? delegate.getCompilerMetadata(toolType) : getGccMetadata()
    }

}
