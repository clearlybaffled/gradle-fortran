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

class GFortranCompiler extends GccCompatibleNativeCompiler<FortranCompileSpec> {
		
	GFortranCompiler(BuildOperationExecutor buildOperationExecutor, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, String objectFileExtension, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
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

		@Override
		protected void addIncludeArgs(FortranCompileSpec spec, List<String> args) {
			// TODO Auto-generated method stub
			super.addIncludeArgs(spec, args);
		}

		@Override
		protected void addMacroArgs(FortranCompileSpec spec, List<String> args) {
			// TODO Auto-generated method stub
			super.addMacroArgs(spec, args);
		}

		@Override
		protected void addUserArgs(FortranCompileSpec spec, List<String> args) {
			// TODO Auto-generated method stub
			super.addUserArgs(spec, args);
		}

		@Override
		protected boolean needsStandardIncludes(NativePlatform targetPlatform) {
			// TODO Auto-generated method stub
			return super.needsStandardIncludes(targetPlatform);
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
				case "windows": args << "--enable-auto-import" << "-Wl,--add-stdcall-alias"
				case "linux":   args << "-rdynamic"
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