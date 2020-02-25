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
		super.source.with { 
		     srcDirs "src/main/fortran"
		     include "**/*.f", "**/*.for"
			 exclude "com*.f"
		}
	}

	@Override
	public SourceDirectorySet getExportedHeaders() {
		super.exportedHeaders.with {
		     srcDirs "src/main/include"
			 include "**/*.inc"
		}   
	}
	
}