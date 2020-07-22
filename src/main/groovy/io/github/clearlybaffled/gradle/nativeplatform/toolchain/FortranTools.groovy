package io.github.clearlybaffled.gradle.nativeplatform.toolchain;

import org.gradle.nativeplatform.toolchain.Gcc

/**
 * The <a href="http://gcc.gnu.org/fortran/">GNU Fortran</a> tool chain.
 */
interface GFortran extends Gcc {}

/**
 * The <a href="https://software.intel.com/content/www/us/en/develop/tools/compilers/fortran-compilers.html">Intel Fortran</a> tool chain.
 */
interface IntelFortran extends Gcc {}