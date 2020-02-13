package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import org.gradle.internal.Transformers
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.gcc.GccCompatibleNativeCompiler
import org.gradle.nativeplatform.toolchain.internal.gcc.GccCompilerArgsTransformer

interface FortranCompileSpec extends NativeCompileSpec {}

class FortranCompiler extends GccCompatibleNativeCompiler<FortranCompileSpec> {
		
	FortranCompiler(BuildOperationExecutor buildOperationExecutor, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, String objectFileExtension, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
		super(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineToolInvocationWorker, invocationContext, new FortranCompileArgsTransformer(), Transformers.<FortranCompileSpec>noOpTransformer(), objectFileExtension, useCommandFile, workerLeaseService)
	}

	private static class FortranCompileArgsTransformer extends GccCompilerArgsTransformer<FortranCompileSpec> { 
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