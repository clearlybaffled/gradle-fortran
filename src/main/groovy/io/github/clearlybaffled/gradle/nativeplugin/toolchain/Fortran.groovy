package io.github.clearlybaffled.gradle.nativeplugin.toolchain

import org.gradle.internal.Transformers
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.gcc.GccCompatibleNativeCompiler
import org.gradle.nativeplatform.toolchain.internal.gcc.GccCompilerArgsTransformer
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerType

public interface Fortran extends GccCompatibleToolChain {}

public interface FortranCompileSpec extends NativeCompileSpec {}

class FortranCompiler extends GccCompatibleNativeCompiler<FortranCompileSpec> {
		
	FortranCompiler(BuildOperationExecutor buildOperationExecutor, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, String objectFileExtension, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
		super(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineToolInvocationWorker, invocationContext, new FortranCompileArgsTransformer(), Transformers.<FortranCompileSpec>noOpTransformer(), objectFileExtension, useCommandFile, workerLeaseService);
	}

	private static class FortranCompileArgsTransformer extends GccCompilerArgsTransformer<FortranCompileSpec> { 
		@Override
		protected String getLanguage() {
			return "fortran"
		}
	}
}

public enum FortranCompilerType implements CompilerType {
	GFORTRAN("gfortran", "GFortran"),
	IFORTRAN("ifort","IFortran")
	
	FortranCompilerType(String identifier, String description) {
		this.identifier = identifier;
		this.description = description;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getDescription() {
		return description;
	}

}