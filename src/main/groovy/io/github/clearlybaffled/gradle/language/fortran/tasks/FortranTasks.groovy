package io.github.clearlybaffled.gradle.language.fortran.tasks

import org.gradle.api.tasks.CacheableTask
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.DefaultFortranCompileSpec
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.FortranCompileSpec

/**
 * Compiles Fortran source files into object files.
 */
@CacheableTask
public class FortranCompile extends AbstractNativeSourceCompileTask {
    @Override
    protected NativeCompileSpec createCompileSpec() {
        return new DefaultFortranCompileSpec();
    }

	@Override
	protected void configureSpec(NativeCompileSpec spec) {
		FortranCompileSpec fSpec = (FortranCompileSpec) spec
		fSpec
	}
}
