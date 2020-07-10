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
import org.gradle.language.nativeplatform.internal.AbstractNativeCompileSpec
import org.gradle.model.Defaults
import org.gradle.model.Each
import org.gradle.model.Finalize
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory
import org.gradle.nativeplatform.internal.NativeExecutableBinarySpecInternal
import org.gradle.nativeplatform.plugins.NativeComponentPlugin
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal
import org.gradle.nativeplatform.toolchain.internal.compilespec.CCompileSpec
import org.gradle.nativeplatform.toolchain.internal.gcc.GccToolChain
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin
import org.gradle.process.internal.ExecActionFactory


class FortranToolChains implements Plugin<Project> {
    @Override
    public void apply (Project project) {
        project.getPluginManager().apply(FortranCompilerPlugin)
        project.getPluginManager().apply(StandardToolChainsPlugin)
    }
}


class FortranCompilerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin)
        
        project.ext.GFortran = GFortran
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
        void configureToolChains(NativeToolChainRegistryInternal toolChains) {
            toolChains.withType(GFortran) {
                eachPlatform {
                    cCompiler.executable = "gfortran"
                    cCompiler.withArguments { args ->
                        def x = args.findIndexOf { '-x' }
                        args.set(x+1, 'none')
                    }
                    linker.executable = "gfortran"
                }
            }
            toolChains.withType(IntelFortran) {
                eachPlatform {
                    cCompiler.executable = "ifort"
                    cCompiler.withArguments { args ->
                        def x = args.findIndexOf { '-x' }
                        args.remove(x+1)
                        args.remove(x)
                    }
                    linker.executable = "ifort"
                }
            }
        }
        
        
        @Finalize
        static void configureBinaries(@Each NativeExecutableBinarySpecInternal binary) {
            def platform = binary.targetPlatform
            if (binary.toolChain in GFortran) {
                binary.getcCompiler().define(binary.flavor.name + '_BUILD')
                if (platform.operatingSystem.isWindows()) {
                    binary.getcCompiler().define('WIN32')
                } else if (platform.operatingSystem.isLinux()) {
                    binary.getcCompiler().define('UNIX')
                }
                
                binary.getcCompiler().define('GFORTRAN')
                binary.getcCompiler().args('-cpp')
            } else if (binary.toolChain in IntelFortran) {
                if (!binary.getcCompiler().args.empty) {
                    binary.getcCompiler().args("-march=core2","-Wall", "-ansi","-pendantic","-std=c99")
                }
            }
        }
    }
}

interface FortranCompileSpec extends CCompileSpec {}

class DefaultFortranCompileSpec extends AbstractNativeCompileSpec implements FortranCompileSpec {}

interface GFortran extends Gcc {}

public class GFortranToolChain extends GccToolChain implements GFortran {
    public static final String DEFAULT_NAME = "gfortran"    


    public GFortranToolChain(Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor,
            OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory,
            CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory,
            CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery,
            WorkerLeaseService workerLeaseService) {
        super(instantiator, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
                compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
    }    
}

interface IntelFortran extends Gcc {}

public class IntelFortranToolChain extends GccToolChain implements IntelFortran {
    public static final String DEFAULT_NAME = "ifort"


    public IntelFortranToolChain(Instantiator instantiator, String name, BuildOperationExecutor buildOperationExecutor,
            OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory,
            CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory,
            CompilerMetaDataProviderFactory metaDataProviderFactory, SystemLibraryDiscovery standardLibraryDiscovery,
            WorkerLeaseService workerLeaseService) {
        super(instantiator, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory,
                compilerOutputFileNamingSchemeFactory, metaDataProviderFactory, standardLibraryDiscovery, workerLeaseService)
    }    
    
}