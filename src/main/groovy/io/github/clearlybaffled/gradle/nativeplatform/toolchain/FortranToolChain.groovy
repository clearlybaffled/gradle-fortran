package io.github.clearlybaffled.gradle.nativeplatform.toolchain

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.nativeplatform.internal.AbstractNativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin

import io.github.clearlybaffled.gradle.internal.EnumHelper
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins.FortranCompilerPlugin


class FortranToolChains implements Plugin<Project> {
	@Override
	public void apply (Project project) {
		 EnumHelper.addEntry(ToolType, "FORTRAN_COMPILER", "Fortran Compiler")
         project.getPluginManager().apply(FortranCompilerPlugin)
		//project.getPluginManager().apply(IFortranCompilePlugin)
		project.getPluginManager().apply(StandardToolChainsPlugin)
        
       
	}
}

interface FortranCompileSpec extends NativeCompileSpec {} 
class DefaultFortranCompileSpec extends AbstractNativeCompileSpec implements FortranCompileSpec {}