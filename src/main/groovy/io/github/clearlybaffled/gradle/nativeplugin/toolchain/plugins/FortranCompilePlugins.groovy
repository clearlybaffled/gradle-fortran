package io.github.clearlybaffled.gradle.nativeplugin.toolchain.plugins

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
import org.gradle.model.Managed
import org.gradle.model.ModelMap
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.plugins.NativeComponentPlugin
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.platform.base.*
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.nativeplugin.toolchain.Fortran
import io.github.clearlybaffled.gradle.nativeplugin.toolchain.gfortran.GFortranToolChain

public class GFortranCompilePlugins implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin.class)
		
    }

    static class Rules extends RuleSource {
		@Managed
		interface FortranBinary extends NativeBinarySpec {}
		
        @ComponentBinaries
		void generateFortranNativeBinaries(ModelMap<FortranBinary> binaries) {
			binaries.all { binary ->
				binary
			}
		}
		@Defaults
        public static void addToolChain(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry) {
			
            final FileResolver fileResolver = serviceRegistry.get(FileResolver.class)
            final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class)
            final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory.class)
            final Instantiator instantiator = serviceRegistry.get(Instantiator.class)
            final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class)
            final CompilerMetaDataProviderFactory metaDataProviderFactory = serviceRegistry.get(CompilerMetaDataProviderFactory.class)
            final SystemLibraryDiscovery standardLibraryDiscovery = serviceRegistry.get(SystemLibraryDiscovery.class)
            final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService.class)

            toolChainRegistry.registerFactory(Fortran.class, new NamedDomainObjectFactory<Fortran>() {
                @Override
                public Fortran create(String name) {
                    return instantiator.newInstance(GFortranToolChain.class, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, instantiator, workerLeaseService)
                }
            });
            toolChainRegistry.registerDefaultToolChain(GFortranToolChain.DEFAULT_NAME, Fortran.class)
        }

    }
}
