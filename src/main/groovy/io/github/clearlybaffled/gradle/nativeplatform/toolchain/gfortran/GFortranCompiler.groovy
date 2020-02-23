package io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran

import org.gradle.internal.Transformers
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.internal.LinkerSpec
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.toolchain.internal.ArgsTransformer
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.gcc.GccCompatibleNativeCompiler
import org.gradle.nativeplatform.toolchain.internal.gcc.GccCompilerArgsTransformer
import org.gradle.nativeplatform.toolchain.internal.gcc.GccLinker

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.FortranCompileSpec

class FortranCompiler extends GccCompatibleNativeCompiler<FortranCompileSpec> {
		
	FortranCompiler(BuildOperationExecutor buildOperationExecutor, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, String objectFileExtension, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
		super(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineToolInvocationWorker, invocationContext, new GFortranCompileArgsTransformer(), Transformers.<FortranCompileSpec>noOpTransformer(), objectFileExtension, useCommandFile, workerLeaseService)
	}

	private static class GFortranCompileArgsTransformer extends GccCompilerArgsTransformer<FortranCompileSpec> { 
		@Override
		protected String getLanguage() {
			return "f95"
		}

		@Override
		protected void addToolSpecificArgs(FortranCompileSpec spec, List<String> args) {
			// TODO Auto-generated method stub
			super.addToolSpecificArgs(spec, args)
		}
	}
}

class GFortranLinker extends GccLinker {
	GFortranLinker(BuildOperationExecutor buildOperationExecutor, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
		super(buildOperationExecutor, commandLineToolInvocationWorker, invocationContext, new GFortranLinkerArgsTransformer(), useCommandFile, workerLeaseService)
	}

	private static class GFortranLinkerArgsTransformer implements ArgsTransformer<LinkerSpec> {
		@Override
		public List<String> transform(LinkerSpec spec) {
			def args = []

			args << spec.systemArgs

			switch (spec.targetPlatform.operatingSystem) {
				case { it.windows }: args << "--enable-auto-import" << "-Wl,--add-stdcall-alias"
				case { it.unix } :   args << "-rdynamic"
			}
			
			args << "-o" << spec.outputFile.absolutePath
		    args << spec.objectFiles.map { it -> it.absolutePath }
			args << spec.libraries.map { it -> it.absolutePath }  			

			if (!spec.libraryPath.empty) {
				throw new UnsupportedOperationException("Library Path not yet supported on GCC")
			}

			args << spec.args			
		}
	}
}