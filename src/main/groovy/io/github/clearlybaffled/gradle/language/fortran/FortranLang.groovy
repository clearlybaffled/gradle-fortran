package io.github.clearlybaffled.gradle.language.fortran

import org.gradle.api.file.SourceDirectorySet
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.nativeplatform.DependentSourceSet
import org.gradle.language.nativeplatform.HeaderExportingSourceSet
import org.gradle.language.nativeplatform.internal.AbstractHeaderExportingDependentSourceSet

/**
 * A set of Fortran source files.
 *
 * <p>A Fortran source set contains a set of source files, together with an optional set of exported header files.</p>
 *
 * <pre class='autoTested'>
 * plugins {
 *     id 'io.github.clearlybaffled.fortran'
 * }
 *
 * model {
 *     components {
 *         main(NativeLibrarySpec) {
 *             sources {
 *                 fortran {
 *                     source {
 *                         srcDirs "src/main/fortran", "src/shared/fortran"
 *                         include "**{@literal /}*.f", "**{@literal /}*.F", "**{@literal /}*.f??", "**{@literal /}*.F??"
 *                     }
 *                     exportedHeaders {
 *                         srcDirs "src/main/include"
 *                         include "**{@literal /}*.inc"
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
interface FortranSourceSet extends HeaderExportingSourceSet, LanguageSourceSet, DependentSourceSet {}

class DefaultFortranSourceSet extends AbstractHeaderExportingDependentSourceSet implements FortranSourceSet {

	@Override
	protected String getLanguageName() {
		"fortran"
	}

	@Override
	public SourceDirectorySet getSource() {
		super.source.with { 
		     include "**/*.f", "**/*.F", "**/*.f??", "**/*.F??"
			 exclude "com*.f*"
		}
	}

	@Override
	public SourceDirectorySet getExportedHeaders() {
		super.exportedHeaders.with {
			 include "**/*.inc"
		}   
	}
	
}