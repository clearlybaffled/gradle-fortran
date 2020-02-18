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
import org.gradle.process.internal.ExecActionFactory

import io.github.clearlybaffled.gradle.language.fortran.FortranLangPlugin.Fortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran.GFortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran.GFortranToolChain
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.ifortran.IFortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.ifortran.IFortranToolChain

class GFortranCompilePlugins implements Plugin<Project> {
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
                    return instantiator.newInstance(GFortranToolChain, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, instantiator, workerLeaseService)
                }
            });
            toolChainRegistry.registerDefaultToolChain(GFortranToolChain.DEFAULT_NAME, GFortran)
        }

    }
}

class IFortranCompilePlugins implements Plugin<Project> {
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

			toolChainRegistry.registerFactory(Fortran, new NamedDomainObjectFactory<IFortran>() {
				@Override
				public IFortran create(String name) {
					return instantiator.newInstance(IFortranToolChain, name, buildOperationExecutor, OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, instantiator, workerLeaseService)
				}
			});
			toolChainRegistry.registerDefaultToolChain(GFortranToolChain.DEFAULT_NAME, IFortran)
		}

	}
}