package io.github.clearlybaffled.gradle.language.fortran

import org.gradle.api.file.SourceDirectorySet
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.nativeplatform.DependentSourceSet
import org.gradle.language.nativeplatform.HeaderExportingSourceSet
import org.gradle.language.nativeplatform.internal.AbstractHeaderExportingDependentSourceSet

interface FortranSourceSet extends HeaderExportingSourceSet, LanguageSourceSet, DependentSourceSet {}

class DefaultFortranSourceSet extends AbstractHeaderExportingDependentSourceSet implements FortranSourceSet {

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