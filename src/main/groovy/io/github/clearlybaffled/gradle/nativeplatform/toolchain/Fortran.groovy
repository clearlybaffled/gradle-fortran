package io.github.clearlybaffled.gradle.nativeplatform.toolchain

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

interface Fortran extends GccCompatibleToolChain {}

