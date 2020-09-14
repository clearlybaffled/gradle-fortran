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
import org.gradle.model.Defaults
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.plugins.NativeComponentPlugin
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.GnuFortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.IntelFortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.gcc.GnuFortranToolChain
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.intel.IntelFortranToolChain


class FortranToolChains implements Plugin<Project> {
    @Override
    public void apply (Project project) {
        project.getPluginManager().apply(StandardToolChainsPlugin)
        project.getPluginManager().apply(GnuFortranCompilerPlugin)
        project.getPluginManager().apply(IntelFortranCompilerPlugin)      
    }
}

/**
 * A {@link Plugin} which makes the <a href="http://gcc.gnu.org/fortran/">GNU Fortran compiler</a> available for compiling Fortran code.
 */
class GnuFortranCompilerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin)

        project.ext.GnuFortran = GnuFortran
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

            toolChainRegistry.registerFactory(GnuFortran, new NamedDomainObjectFactory<GnuFortran>() {
                        @Override
                        public GnuFortran create(String name) {
                            return instantiator.newInstance(GnuFortranToolChain, instantiator, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
                        }
                    })
            toolChainRegistry.registerDefaultToolChain(GnuFortranToolChain.DEFAULT_NAME, GnuFortran)
        }
    }
}
/**
 * A {@link Plugin} which makes the <a href="https://software.intel.com/content/www/us/en/develop/tools/compilers/fortran-compilers.html">Intel Fortran Compiler</a> available for compiling Fortran code.
 */
class IntelFortranCompilerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin)

        project.ext.IntelFortran = IntelFortran
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

            toolChainRegistry.registerFactory(IntelFortran, new NamedDomainObjectFactory<IntelFortran>() {
                        @Override
                        public IntelFortran create(String name) {
                            return instantiator.newInstance(IntelFortranToolChain, instantiator, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
                        }
                    })
            toolChainRegistry.registerDefaultToolChain(IntelFortranToolChain.DEFAULT_NAME, IntelFortran)
        }
    }
}