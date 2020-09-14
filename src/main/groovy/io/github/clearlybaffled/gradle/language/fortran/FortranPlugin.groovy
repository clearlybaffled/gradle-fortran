package io.github.clearlybaffled.gradle.language.fortran

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.service.ServiceRegistry
import org.gradle.language.base.internal.SourceTransformTaskConfig
import org.gradle.language.base.internal.registry.LanguageTransformContainer
import org.gradle.language.base.plugins.ComponentModelBasePlugin
import org.gradle.language.nativeplatform.internal.DependentSourceSetInternal
import org.gradle.language.nativeplatform.internal.NativeLanguageTransform
import org.gradle.model.Each
import org.gradle.model.Finalize
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.internal.DefaultPreprocessingTool
import org.gradle.nativeplatform.plugins.NativeComponentModelPlugin
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.platform.base.ComponentType
import org.gradle.platform.base.TypeBuilder

import io.github.clearlybaffled.gradle.language.nativeplatform.internal.FortranSourceCompileTaskConfig
import io.github.clearlybaffled.gradle.nativeplatform.NativeExecutableFortranSpec
import io.github.clearlybaffled.gradle.nativeplatform.internal.FortranCompilerBinaryExecSpec
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins.FortranToolChains


/**
 * A plugin for projects wishing to build native binary components from Fortran sources.
 *
 * <p>Automatically includes the {@link org.gradle.nativeplatform.plugins.NativeComponentPlugin} for native component support.</p>
 *
 * <ul>
 * <li>Creates a {@link io.github.clearlybaffled.gradle.language.fortran.tasks.FortranCompile} task for each {@link io.github.clearlybaffled.gradle.language.fortran.FortranSourceSet} to compile the Fortran sources.</li>
 * </ul>
 */
class FortranPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(NativeComponentModelPlugin)
        project.getPluginManager().apply(FortranLangPlugin)
		project.getPluginManager().apply(FortranToolChains)
    }
    
    static class Rules extends RuleSource {
        @ComponentType
        void nativeExecutable(TypeBuilder<NativeExecutableFortranSpec> builder) {
            builder.defaultImplementation(FortranCompilerBinaryExecSpec)
        }

    }
}


/**
 *  Adds core Fortran language support.
 */
class FortranLangPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        project.getPluginManager().apply(ComponentModelBasePlugin)
    }

    static class Rules extends RuleSource {
        @ComponentType
        void registerLanguage(TypeBuilder<FortranSourceSet> builder) {
            builder.defaultImplementation(DefaultFortranSourceSet)
            builder.internalView(DependentSourceSetInternal)
		}
		
		@Finalize
		void createFortranSourceSets(@Each NativeBinarySpec spec) {
			spec.sources.create("fortran", FortranSourceSet)
		}

        @Mutate
        void registerLanguageTransform(LanguageTransformContainer languages, ServiceRegistry serviceRegistry) {
            languages.add(new Fortran())
        }
    }

    private static class Fortran extends NativeLanguageTransform<FortranSourceSet> {
        @Override
        public Class<FortranSourceSet> getSourceSetType() {
            FortranSourceSet
        }

        @Override
        public Map<String, Class<?>> getBinaryTools() {
            Collections.unmodifiableMap([fortranCompiler: DefaultPreprocessingTool])
        }

        @Override
        public String getLanguageName() {
            "fortran"  
        }

		@Override
        public ToolType getToolType() {
            null
        }

        @Override
        public SourceTransformTaskConfig getTransformTask() {
            new FortranSourceCompileTaskConfig(this)
        }

    }
}
