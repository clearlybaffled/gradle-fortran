package io.github.clearlybaffled.gradle.nativeplatform.internal

import org.gradle.nativeplatform.PreprocessingTool
import org.gradle.nativeplatform.Tool
import org.gradle.nativeplatform.internal.DefaultNativeExecutableBinarySpec
import org.gradle.nativeplatform.internal.DefaultPreprocessingTool

class FortranCompilerBinaryExecSpec extends DefaultNativeExecutableBinarySpec {
    private final PreprocessingTool fortranCompiler = new DefaultPreprocessingTool()


    @Override
    public Tool getToolByName(String name) {
        name == "fortranCompiler" ? fortranCompiler : super.getToolByName(name)
    }    
    
    
}
