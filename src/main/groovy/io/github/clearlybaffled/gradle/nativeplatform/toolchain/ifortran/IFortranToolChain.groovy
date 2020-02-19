package io.github.clearlybaffled.gradle.nativeplatform.toolchain.ifortran

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain
import org.gradle.nativeplatform.toolchain.internal.SymbolExtractorOsConfig
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.AbstractFortranCompatibleToolChain
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.DefaultFortranPlatformToolChain
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.FortranCommandLineToolConfiguration

interface IFortran extends GccCompatibleToolChain {}

public class IFortranToolChain extends AbstractFortranCompatibleToolChain implements IFortran {

	public static final String DEFAULT_NAME = "ifort";
	private final Instantiator instantiator
		
	public IFortranToolChain(String name, BuildOperationExecutor buildOperationExecutor, OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery, Instantiator instantiator, WorkerLeaseService workerLeaseService) {
		super(name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory.gcc(), standardLibraryDiscovery, instantiator, workerLeaseService)
		this.instantiator = instantiator
	}

	@Override
	protected void configureDefaultTools(DefaultFortranPlatformToolChain toolChain) {
		toolChain.with {
			add(instantiator.newInstance(FortranCommandLineToolConfiguration, ToolType.C_COMPILER, "ifort"))
			add(instantiator.newInstance(FortranCommandLineToolConfiguration, ToolType.LINKER, "ifort"))
			add(instantiator.newInstance(FortranCommandLineToolConfiguration, ToolType.STATIC_LIB_ARCHIVER, "ar"))
			add(instantiator.newInstance(FortranCommandLineToolConfiguration, ToolType.ASSEMBLER, "gcc")) //TODO need this?
			add(instantiator.newInstance(FortranCommandLineToolConfiguration, ToolType.SYMBOL_EXTRACTOR, SymbolExtractorOsConfig.current().getExecutableName()))
			add(instantiator.newInstance(FortranCommandLineToolConfiguration, ToolType.STRIPPER, "strip"))
	
		}
	}
	
	@Override
	protected String getTypeName() {
		return "Intel Fortran"
	}

}

