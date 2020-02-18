package io.github.clearlybaffled.gradle.language.fortran

import org.gradle.language.c.CSourceSet
import org.gradle.language.cpp.internal.DefaultCppPlatform
import org.gradle.language.nativeplatform.internal.AbstractHeaderExportingDependentSourceSet

interface FortranSourceSet extends CSourceSet {}

class DefaultFortranSourceSet extends AbstractHeaderExportingDependentSourceSet implements FortranSourceSet {}

class FortranPlatform extends DefaultCppPlatform {}