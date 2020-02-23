package io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.GccToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
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
		
	public GFortranToolChain(Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor,
			OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory,
			CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory,
			CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery,
			WorkerLeaseService workerLeaseService) {
		super(instantiator, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
				compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
	}

	@Override
	protected void configureDefaultTools(DefaultGccPlatformToolChain toolChain) {
		toolChain.with { 
			cppCompiler.executable = "gfortran"
			linker.executable = "gfortran"
		}
	}
		
}

