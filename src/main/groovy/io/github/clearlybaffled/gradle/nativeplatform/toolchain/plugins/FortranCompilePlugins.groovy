package io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.model.Defaults
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.plugins.NativeComponentPlugin
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran.GFortranToolChain

class GFortranCompilePlugins implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin)
    }

    static class Rules extends RuleSource {
        @Defaults
        public static void addToolChain(NativeToolChainRegistryInternal toolChainRegistry) {
            toolChainRegistry.registerDefaultToolChain("gfortran", Gcc)
        }

    }
}
