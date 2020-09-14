package io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.intel

import org.gradle.api.Action
import org.gradle.internal.Transformers
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.operations.BuildOperationQueue
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.internal.LinkerSpec
import org.gradle.nativeplatform.toolchain.internal.AbstractCompiler
import org.gradle.nativeplatform.toolchain.internal.ArgsTransformer
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolContext
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocation
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker
import org.gradle.nativeplatform.toolchain.internal.MacroArgsConverter
import org.gradle.nativeplatform.toolchain.internal.NativeCompiler
import org.gradle.nativeplatform.toolchain.internal.OptionsFileArgsWriter
import org.gradle.nativeplatform.toolchain.internal.gcc.GccCompilerArgsTransformer
import org.gradle.nativeplatform.toolchain.internal.gcc.GccOptionsFileArgsWriter

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.compilespec.FortranCompileSpec

class IntelFortranCompiler extends NativeCompiler<FortranCompileSpec> {
		
	IntelFortranCompiler(BuildOperationExecutor buildOperationExecutor, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, String objectFileExtension, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
		super(buildOperationExecutor, compilerOutputFileNamingSchemeFactory, commandLineToolInvocationWorker, invocationContext, new IntelFortranCompileArgsTransformer(), Transformers.<FortranCompileSpec>noOpTransformer(), objectFileExtension, useCommandFile, workerLeaseService)
	}

	private static class IntelFortranCompileArgsTransformer implements ArgsTransformer<FortranCompileSpec> { 
		
		@Override
		public List<String> transform(FortranCompileSpec spec) {
			def args = ['-fpp']

			if (spec.debuggable) args << "-g"
			
			if (spec.optimized) args << "-O2"
			
			new MacroArgsConverter().transform(spec.macros).each { args << "-D${it}" }
			
			if (spec.targetPlatform.operatingSystem.windows) args << "-DWIN32"

			args.addAll(spec.allArgs)
			
			if (!spec.targetPlatform.operatingSystem.macOsX) args << "-nostdinc"
				
			spec.includeRoots.each { args << "-I" << it.absolutePath }
			spec.systemIncludeRoots.each { args << "-isystem" << it.absolutePath }
			
			args
		}	
	}

	@Override
	protected List<String> getOutputArgs(FortranCompileSpec spec, File outputFile) {
		return Arrays.asList("-o", outputFile.getAbsolutePath());
	}

	@Override
	protected void addOptionsFileArgs(List<String> args, File tempDir) {
		OptionsFileArgsWriter writer = new GccOptionsFileArgsWriter(tempDir);
        // modifies args in place
        writer.execute(args);
	}

	@Override
	protected List<String> getPCHArgs(FortranCompileSpec spec) {
		[]
	}
}

class IntelFortranLinker extends AbstractCompiler<LinkerSpec>  {
	IntelFortranLinker(BuildOperationExecutor buildOperationExecutor, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CommandLineToolContext invocationContext, boolean useCommandFile, WorkerLeaseService workerLeaseService) {
		super(buildOperationExecutor, commandLineToolInvocationWorker, invocationContext, new IntelFortranLinkerArgsTransformer(), useCommandFile, workerLeaseService)
	}

	private static class IntelFortranLinkerArgsTransformer implements ArgsTransformer<LinkerSpec> {
		@Override
		public List<String> transform(LinkerSpec spec) {
			def args = []

			args << spec.systemArgs

			if (spec.targetPlatform.operatingSystem.windows) 
				args << "--enable-auto-import" << "-Wl,--add-stdcall-alias"
			else if (spec.targetPlatform.operatingSystem.linux)
				args << "-rdynamic"
			
			
			args << "-o" << spec.outputFile.absolutePath
			
		    spec.objectFiles.each { args << it.absolutePath }
			spec.libraries.each {  args << it.absolutePath }  			

			if (!spec.libraryPath.empty) {
				//throw new UnsupportedOperationException("Library Path not yet supported on GCC")
			}

			args.addAll(spec.args)
            
            return args.flatten()
					
		}
	}

    @Override
    protected Action<BuildOperationQueue<CommandLineToolInvocation>> newInvocationAction(final LinkerSpec spec, List<String> args) {
        final CommandLineToolInvocation invocation = newInvocation(
            "linking " + spec.getOutputFile().getName(), args, spec.getOperationLogger());

        return new Action<BuildOperationQueue<CommandLineToolInvocation>>() {
            @Override
            public void execute(BuildOperationQueue<CommandLineToolInvocation> buildQueue) {
                buildQueue.setLogLocation(spec.getOperationLogger().getLogLocation());
                buildQueue.add(invocation);
            }
        };
    }

    @Override
    protected void addOptionsFileArgs(List<String> args, File tempDir) {
        new GccOptionsFileArgsWriter(tempDir).execute(args);
    }

}