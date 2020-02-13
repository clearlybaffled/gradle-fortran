package io.github.clearlybaffled.gradle.language.fortran

import org.apache.groovy.util.Maps
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.service.ServiceRegistry
import org.gradle.language.base.internal.SourceTransformTaskConfig
import org.gradle.language.base.internal.registry.LanguageTransformContainer
import org.gradle.language.base.plugins.ComponentModelBasePlugin
import org.gradle.language.c.tasks.CPreCompiledHeaderCompile
import org.gradle.language.nativeplatform.internal.DependentSourceSetInternal
import org.gradle.language.nativeplatform.internal.NativeLanguageTransform
import org.gradle.language.nativeplatform.internal.PCHCompileTaskConfig
import org.gradle.language.nativeplatform.internal.SourceCompileTaskConfig
import org.gradle.model.Mutate
import org.gradle.model.RuleSource
import org.gradle.nativeplatform.internal.DefaultPreprocessingTool
import org.gradle.nativeplatform.internal.pch.PchEnabledLanguageTransform
import org.gradle.nativeplatform.plugins.NativeComponentPlugin
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.platform.base.ComponentType
import org.gradle.platform.base.TypeBuilder

import io.github.clearlybaffled.gradle.language.fortran.tasks.FortranCompile
import io.github.clearlybaffled.gradle.nativeplatform.toolchain.plugins.GFortranCompilePlugins

/**
 * A plugin for projects wishing to build native binary components from Fortran sources.
 *
 * <p>Automatically includes the {@link org.gradle.nativeplatform.plugins.NativeComponentPlugin} for native component support.</p>
 *
 * <ul>
 * <li>Creates a {@link io.github.clearlybaffled.gradle.language.fortran.tasks.FortranCompile} task for each {@link io.github.clearlybaffled.gradle.language.fortran.FortranSourceSet} to compile the C sources.</li>
 * </ul>
 */
class FortranPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin.class);
        project.getPluginManager().apply(FortranLangPlugin.class);
		project.getPluginManager().apply(GFortranCompilePlugins.class);
    }
}


class FortranLangPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        project.getPluginManager().apply(ComponentModelBasePlugin.class);
    }

    @SuppressWarnings("UnusedDeclaration")
    static class Rules extends RuleSource {
        @ComponentType
        void registerLanguage(TypeBuilder<FortranSourceSet> builder) {
            builder.defaultImplementation(DefaultFortranSourceSet.class);
            builder.internalView(DependentSourceSetInternal.class);
        }

        @Mutate
        void registerLanguageTransform(LanguageTransformContainer languages, ServiceRegistry serviceRegistry) {
            languages.add(new Fortran());
        }
    }

    private static class Fortran extends NativeLanguageTransform<FortranSourceSet> implements PchEnabledLanguageTransform<FortranSourceSet> {
        @Override
        public Class<FortranSourceSet> getSourceSetType() {
            return FortranSourceSet.class;
        }

        @Override
        public Map<String, Class<?>> getBinaryTools() {
            Map<String, Class<?>> tools = Maps.newLinkedHashMap();
            tools.put("fortranCompiler", DefaultPreprocessingTool.class);
            return tools;
        }

        @Override
        public String getLanguageName() {
            return "fortran";
        }

        // TODO ??
		@Override
        public ToolType getToolType() {
            return ToolType.C_COMPILER;
        }

        @Override
        public SourceTransformTaskConfig getTransformTask() {
            return new SourceCompileTaskConfig(this, FortranCompile.class);
        }

        @Override
        public SourceTransformTaskConfig getPchTransformTask() {
            return new PCHCompileTaskConfig(this, CPreCompiledHeaderCompile.class);
        }
    }
}
