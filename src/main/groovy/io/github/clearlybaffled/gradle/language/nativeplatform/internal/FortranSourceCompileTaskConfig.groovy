package io.github.clearlybaffled.gradle.language.nativeplatform.internal

import org.gradle.api.Task
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.service.ServiceRegistry
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.nativeplatform.internal.NativeLanguageTransform
import org.gradle.language.nativeplatform.internal.SourceCompileTaskConfig
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.internal.NativeBinarySpecInternal
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.platform.base.BinarySpec
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.language.fortran.FortranSourceSet
import io.github.clearlybaffled.gradle.language.fortran.tasks.FortranCompile
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.gcc.FortranEnabledGccToolChain
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.gcc.GnuFortranToolChain

class FortranSourceCompileTaskConfig extends SourceCompileTaskConfig {

    public FortranSourceCompileTaskConfig(NativeLanguageTransform<FortranSourceSet> languageTransform) {
        super(languageTransform, FortranCompile)
    }

    @Override
    public void configureTask(Task task, BinarySpec binary, LanguageSourceSet sourceSet, ServiceRegistry serviceRegistry) {
    
        final FileResolver fileResolver = serviceRegistry.get(FileResolver)
        final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory)
        final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory)
        final Instantiator instantiator = serviceRegistry.get(Instantiator)
        final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor)
        final CompilerMetaDataProviderFactory metaDataProviderFactory = serviceRegistry.get(CompilerMetaDataProviderFactory)
        final SystemLibraryDiscovery standardLibraryDiscovery = serviceRegistry.get(SystemLibraryDiscovery)
        final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService)

        def toolRegistry  = serviceRegistry.get(NativeToolChainRegistry) as NativeToolChainRegistryInternal
        
        def spec = binary as NativeBinarySpecInternal
         
        if (spec.toolChain in GccCompatibleToolChain && spec.toolChain.class != GnuFortranToolChain ) {
            // Decorate the existing GCC Toolchain to include fortran compile support
            NativeToolChainInternal toolChain = new FortranEnabledGccToolChain(spec.toolChain, instantiator, FortranEnabledGccToolChain.DEFAULT_NAME, buildOperationExecutor, spec.targetPlatform.operatingSystem, fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
            spec.toolChain = toolChain
            spec.platformToolProvider = toolChain.select(spec.targetPlatform)
        }
        
        super.configureTask(task, spec, sourceSet, serviceRegistry)
    }
    
}