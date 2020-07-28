package io.github.clearlybaffled.gradle.nativeplatform

import org.gradle.nativeplatform.PreprocessingTool
import org.gradle.nativeplatform.internal.AbstractNativeBinarySpec
import org.gradle.nativeplatform.internal.DefaultPreprocessingTool

class NativeBinarySpecHelper {
    
    private final static PreprocessingTool fortranCompiler = new DefaultPreprocessingTool() 
    
    static PreprocessingTool getFortranCompiler(AbstractNativeBinarySpec spec) {
        fortranCompiler
    }
}
