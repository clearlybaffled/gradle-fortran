package io.github.clearlybaffled.gradle.language.fortran

import org.gradle.api.file.SourceDirectorySet
import org.gradle.language.cpp.CppSourceSet
import org.gradle.language.cpp.internal.DefaultCppPlatform
import org.gradle.language.cpp.internal.DefaultCppSourceSet

interface FortranSourceSet extends CppSourceSet {}

class DefaultFortranSourceSet extends DefaultCppSourceSet implements FortranSourceSet {

	@Override
	protected String getLanguageName() {
		"Fortran"
	}

	@Override
	public SourceDirectorySet getSource() {
		super.source
		     .srcDirs("src/main/fortran")
		     .include("**/*.f")
	}
}

class FortranPlatform extends DefaultCppPlatform {}