package io.github.clearlybaffled.gradle.nativeplatform.toolchain.ifortran

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.Fortran

public class IFortranToolChain extends AbstractGccCompatibleToolChain implements Fortran {

	public static final String DEFAULT_NAME = "ifort";
	
		public IFortranToolChain(String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, Instantiator instantiator, WorkerLeaseService workerLeaseService) {
			super(name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory.gcc(), standardLibraryDiscovery, instantiator, workerLeaseService)
		}
	
		@Override
		protected void configureDefaultTools(DefaultGccPlatformToolChain toolChain) {
			toolChain.getLinker().setExecutable("clang++")
			toolChain.getcCompiler().setExecutable("clang")
			toolChain.getCppCompiler().setExecutable("clang++")
			toolChain.getObjcCompiler().setExecutable("clang")
			toolChain.getObjcppCompiler().setExecutable("clang++")
			toolChain.getAssembler().setExecutable("clang")
		}
	
		@Override
		protected String getTypeName() {
			return "Intel Fortran"
		}

}

