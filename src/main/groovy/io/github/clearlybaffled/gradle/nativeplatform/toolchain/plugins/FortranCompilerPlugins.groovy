package io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins

import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.reflect.Instantiator
import org.gradle.internal.service.ServiceRegistry
import org.gradle.internal.work.WorkerLeaseService
import org.gradle.language.base.internal.registry.LanguageTransformContainer
import org.gradle.language.nativeplatform.internal.NativeLanguageTransform
import org.gradle.model.Defaults
import org.gradle.model.Each
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.NativeExecutableBinarySpec
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.internal.NativeExecutableBinarySpecInternal
import org.gradle.nativeplatform.plugins.NativeComponentPlugin
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.language.fortran.FortranLangPlugin
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran.GFortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran.GFortranToolChain
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.ifort.IntelFortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.ifort.IntelFortranToolChain

class FortranCompilerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin)
    }

    static class Rules extends RuleSource {
        @Defaults
        public static void addToolChain(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry) {
            final FileResolver fileResolver = serviceRegistry.get(FileResolver)
            final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory)
            final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory)
            final Instantiator instantiator = serviceRegistry.get(Instantiator)
            final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor)
            final CompilerMetaDataProviderFactory metaDataProviderFactory = serviceRegistry.get(CompilerMetaDataProviderFactory)
            final SystemLibraryDiscovery standardLibraryDiscovery = serviceRegistry.get(SystemLibraryDiscovery)
            final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService)

            toolChainRegistry.registerFactory(GFortran, new NamedDomainObjectFactory<GFortran>() {
                @Override
                public GFortran create(String name) {
                    return instantiator.newInstance(GFortranToolChain, instantiator, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
                }
            });
            toolChainRegistry.registerDefaultToolChain(GFortranToolChain.DEFAULT_NAME, GFortran)
            //toolChainRegistry.create(GFortranToolChain.DEFAULT_NAME, GFortran)
            
            toolChainRegistry.registerFactory(IntelFortran, new NamedDomainObjectFactory<IntelFortran>() {
                @Override
                public IntelFortran create(String name) {
                    return instantiator.newInstance(IntelFortranToolChain, instantiator, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
                }
            });
            toolChainRegistry.registerDefaultToolChain(IntelFortranToolChain.DEFAULT_NAME, IntelFortran)
        }

        @Mutate
        public static void applyGFortranToToolChain(@Each NativeExecutableBinarySpec spec, NativeToolChainRegistryInternal toolChains, ServiceRegistry serviceRegistry, LanguageTransformContainer languageTransforms) {
            languageTransforms.with(FortranLangPlugin.Rules.Fortran) { NativeLanguageTransform transform -> 
                spec.sources.values().each { sources ->
                    if (sources.source.name.equals(transform.languageName) ) {
                        NativeToolChainInternal toolChain = toolChains.findByName(GFortranToolChain.DEFAULT_NAME)
                        ((NativeExecutableBinarySpecInternal)spec).toolChain = toolChain 
                        ((NativeExecutableBinarySpecInternal)spec).platformToolProvider = toolChain.select(spec.targetPlatform)
                    }
                }
            }
        }
    }
}