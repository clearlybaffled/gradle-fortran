package io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran

import org.gradle.internal.logging.text.DiagnosticsVisitor
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.language.base.internal.compile.CompileSpec
import org.gradle.language.base.internal.compile.Compiler
import org.gradle.language.base.internal.compile.CompilerUtil
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal
import org.gradle.nativeplatform.toolchain.internal.AbstractPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.gcc.GccPlatformToolProvider
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetadata
import org.gradle.nativeplatform.toolchain.internal.tools.CommandLineToolSearchResult

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.FortranCompileSpec

class FortranEnabledGccPlatformToolProvider extends AbstractPlatformToolProvider {
    
    GccPlatformToolProvider gccToolProvider


    public FortranEnabledGccPlatformToolProvider(BuildOperationExecutor buildOperationExecutor,
            OperatingSystemInternal targetOperatingSystem, GccPlatformToolProvider toolProvider) {
        super(buildOperationExecutor, targetOperatingSystem)
        gccToolProvider = toolProvider 
    }

    public boolean isAvailable() {
        return gccToolProvider.isAvailable();
    }

    public boolean isSupported() {
        return gccToolProvider.isSupported();
    }

    public void explain(DiagnosticsVisitor visitor) {
        gccToolProvider.explain(visitor);
    }

    public String getExecutableName(String executablePath) {
        return gccToolProvider.getExecutableName(executablePath);
    }

    public String getSharedLibraryName(String libraryPath) {
        return gccToolProvider.getSharedLibraryName(libraryPath);
    }

    public boolean producesImportLibrary() {
        return gccToolProvider.producesImportLibrary();
    }

    public boolean requiresDebugBinaryStripping() {
        return gccToolProvider.requiresDebugBinaryStripping();
    }

    public String getImportLibraryName(String libraryPath) {
        return gccToolProvider.getImportLibraryName(libraryPath);
    }

    public String getSharedLibraryLinkFileName(String libraryPath) {
        return gccToolProvider.getSharedLibraryLinkFileName(libraryPath);
    }

    public String getStaticLibraryName(String libraryPath) {
        return gccToolProvider.getStaticLibraryName(libraryPath);
    }

    public String getExecutableSymbolFileName(String libraryPath) {
        return gccToolProvider.getExecutableSymbolFileName(libraryPath);
    }

    public String getLibrarySymbolFileName(String libraryPath) {
        return gccToolProvider.getLibrarySymbolFileName(libraryPath);
    }

    public <T> T get(Class<T> toolType) {
        return gccToolProvider.get(toolType);
    }

    public <T extends CompileSpec> Compiler<T> newCompiler(Class<T> spec) {
        FortranCompileSpec.isAssignableFrom(spec) ? CompilerUtil.castCompiler(createFortranCompiler()) : gccToolProvider.newCompiler(spec)
        
    }

    public CommandLineToolSearchResult locateTool(ToolType compilerType) {
        return gccToolProvider.locateTool(compilerType);
    }

    public String getObjectFileExtension() {
        return gccToolProvider.getObjectFileExtension();
    }

    public SystemLibraries getSystemLibraries(ToolType compilerType) {
        return gccToolProvider.getSystemLibraries(compilerType);
    }

    public CompilerMetadata getCompilerMetadata(ToolType toolType) {
        return gccToolProvider.getCompilerMetadata(toolType);
    }
    
    
}
