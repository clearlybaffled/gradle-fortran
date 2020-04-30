package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.nativeplatform.internal.AbstractNativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins.GFortranCompilePlugin


class FortranToolChains implements Plugin<Project> {
	@Override
	public void apply (Project project) {
		project.getPluginManager().apply(GFortranCompilePlugin)
		//project.getPluginManager().apply(IFortranCompilePlugin)
		project.getPluginManager().apply(StandardToolChainsPlugin)
	}
}

interface FortranCompileSpec extends NativeCompileSpec {} 
class DefaultFortranCompileSpec extends AbstractNativeCompileSpec implements FortranCompileSpec {}