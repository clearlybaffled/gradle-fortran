package io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.ifort;

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.toolchain.internal.gcc.GccToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.IntelFortran

public class IntelFortranToolChain extends GccToolChain implements IntelFortran {
	public static final String DEFAULT_NAME = "ifort"


	public IntelFortranToolChain(Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor,
			OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory,
			CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory,
			CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery,
			WorkerLeaseService workerLeaseService) {
		super(instantiator, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
				compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
	}
	
}