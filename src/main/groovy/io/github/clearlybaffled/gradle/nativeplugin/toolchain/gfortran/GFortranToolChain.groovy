package io.github.clearlybaffled.gradle.nativeplugin.toolchain.gfortran

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplugin.toolchain.AbstractFortranCompatibleToolChain
import io.github.clearlybaffled.gradle.nativeplugin.toolchain.Fortran

public class GFortranToolChain extends AbstractFortranCompatibleToolChain implements Fortran {

	public static final String DEFAULT_NAME = "gfortran"
	
		public GFortranToolChain(String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, Instantiator instantiator, WorkerLeaseService workerLeaseService) {
			super(name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory.gcc(), standardLibraryDiscovery, instantiator, workerLeaseService)
		}
	
		@Override
		protected String getTypeName() {
			return "GNU Fortran"
		}
		

	
}

