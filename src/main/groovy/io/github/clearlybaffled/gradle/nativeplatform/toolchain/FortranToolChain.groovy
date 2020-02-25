package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.nativeplatform.internal.AbstractNativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.compilespec.CppCompileSpec

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins.GFortranCompilePlugins


class FortranToolChains implements Plugin<Project> {
	@Override
	public void apply (Project project) {
		project.getPluginManager().apply(GFortranCompilePlugins)
		//project.getPluginManager().apply(IFortranCompilePlugins)
	}
}

interface FortranCompileSpec extends CppCompileSpec {} 
class DefaultFortranCompileSpec extends AbstractNativeCompileSpec implements FortranCompileSpec {}