package io.github.clearlybaffled.gradle.language.fortran.tasks

import io.github.clearlybaffled.gradle.language.fortran.internal.DefaultFortranCompileSpec
import org.gradle.api.tasks.CacheableTask
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec

/**
 * Compiles Fortran source files into object files.
 */
@CacheableTask
public class FortranCompile extends AbstractNativeSourceCompileTask {
    @Override
    protected NativeCompileSpec createCompileSpec() {
        return new DefaultFortranCompileSpec();
    }

}
