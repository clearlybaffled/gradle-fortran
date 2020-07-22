/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.nativeplatform.toolchain.plugins

import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.TaskExecuter
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.internal.tasks.execution.DefaultTaskExecutionContext
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.execution.ProjectExecutionServices
import org.gradle.internal.TriAction
import org.gradle.model.internal.core.DirectNodeInputUsingModelAction
import org.gradle.model.internal.core.ModelActionRole
import org.gradle.model.internal.core.ModelPath
import org.gradle.model.internal.core.ModelReference
import org.gradle.model.internal.core.ModelView
import org.gradle.model.internal.core.MutableModelNode
import org.gradle.model.internal.core.rule.describe.SimpleModelRuleDescriptor
import org.gradle.model.internal.registry.ModelRegistry
import org.gradle.model.internal.type.ModelType
import org.gradle.nativeplatform.toolchain.NativeToolChain
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification

abstract class NativeToolChainPluginTest extends Specification {

    @Rule TemporaryFolder testDir = new TemporaryFolder()

    ModelRegistry registry

    ProjectInternal project
    ProjectExecutionServices executionServices

    abstract Class<? extends Plugin> getPluginClass()

    abstract Class<? extends NativeToolChain> getToolchainClass()

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testDir.folder).build()
        executionServices = new ProjectExecutionServices(project)
        registry = project.modelRegistry
        project.pluginManager.apply(getPluginClass())
    }

    void execute(Task task) {
        executionServices.get(TaskExecuter).execute((TaskInternal) task, (TaskStateInternal) task.state, new DefaultTaskExecutionContext(null))
        task.state.rethrowFailure()
    }

    String getToolchainName() {
        "toolchain"
    }

    NativeToolChainInternal getToolchain() {
        registry.realize(ModelPath.nonNullValidatedPath("toolChains"), ModelType.of(NativeToolChainRegistryInternal)).getByName(getToolchainName()) as NativeToolChainInternal
    }

    void register() {
        mutate(NativeToolChainRegistry) {
            it.create(getToolchainName(), getToolchainClass())
        }
    }

    def "tool chain is extended"() {
        when:
        register()

        then:
        with(toolchain) {
            it instanceof ExtensionAware
            it.ext instanceof ExtraPropertiesExtension
        }
    }

    protected <T> ModelRegistry mutate(Class<T> type, Closure cl) {
        def role = ModelActionRole.Mutate
        def reference = ModelReference.of(ModelType.of(type))
        def descriptor = new SimpleModelRuleDescriptor("testrule")
        def path = reference.path
        def subject = path != null ? ModelReference.of(path, type) : ModelReference.of(type).inScope(ModelPath.ROOT)
        def triaction = new TriAction<MutableModelNode, T, List<ModelView<?>>>() {
                    @Override
                    public void execute(MutableModelNode mutableModelNode, T t, List<ModelView<?>> inputs) {
                        cl.call(t)
                    }
                }
        def action = DirectNodeInputUsingModelAction.of(subject, descriptor, Collections.emptyList(),  new TriAction<MutableModelNode, T, List<ModelView<?>>>() {
                    @Override
                    public void execute(MutableModelNode modelNode, T t, List<ModelView<?>> inputs) {
                        triaction.execute(modelNode, t, inputs)
                    }
                })
        registry.configure(role, action)
    }
}
