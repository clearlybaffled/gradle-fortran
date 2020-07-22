package io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins

import org.gradle.api.Plugin
import org.gradle.nativeplatform.toolchain.NativeToolChain
import org.gradle.nativeplatform.toolchain.plugins.NativeToolChainPluginTest

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.GnuFortran
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.internal.gcc.GnuFortranToolChain

class GnuFortranCompilerPluginTest extends NativeToolChainPluginTest {

    @Override
    Class<? extends Plugin> getPluginClass() {
        GnuFortranCompilerPlugin
    }

    @Override
    Class<? extends NativeToolChain> getToolchainClass() {
        GnuFortran
    }

    @Override
    String getToolchainName() {
        "gfortran"
    }

    def "can apply plugin by id"() {
        given:
        project.apply plugin: 'io.github.clearlybaffled.gnu-fortran-compiler'

        expect:
        project.plugins.hasPlugin(pluginClass)
    }

    def "makes a Gnu Fortran tool chain available"() {
        when:
        register()

        then:
        toolchain instanceof GnuFortranToolChain
        toolchain.displayName == "Tool chain 'gfortran' (GNU GCC)"
    }

}