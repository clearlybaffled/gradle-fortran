package io.github.clearlybaffled.gradle.language.fortran

import org.gradle.language.c.CSourceSet
import org.gradle.language.cpp.internal.DefaultCppPlatform
import org.gradle.language.nativeplatform.internal.AbstractHeaderExportingDependentSourceSet
import org.gradle.language.nativeplatform.internal.AbstractNativeCompileSpec

import io.github.clearlybaffled.gradle.nativeplatform.toolchain.gfortran.FortranCompileSpec

interface FortranSourceSet extends CSourceSet {}

class DefaultFortranSourceSet extends AbstractHeaderExportingDependentSourceSet implements FortranSourceSet {}

class DefaultFortranCompileSpec extends AbstractNativeCompileSpec implements FortranCompileSpec {}

class FortranPlatform extends DefaultCppPlatform {}