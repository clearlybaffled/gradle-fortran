package io.github.clearlybaffled.gradle.nativeplatform.toolchain.ifort

import org.gradle.internal.impldep.com.google.common.collect.ImmutableList
import org.gradle.nativeplatform.toolchain.internal.metadata.AbstractMetadataProvider
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetadata
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerType
import org.gradle.nativeplatform.toolchain.internal.metadata.AbstractMetadataProvider.BrokenResultException
import org.gradle.nativeplatform.toolchain.internal.swift.metadata.SwiftcMetadata
import org.gradle.util.VersionNumber

interface IntelFortranMetadata extends CompilerMetadata {}

class IntelFortranMetadataProvider extends AbstractMetadataProvider<IntelFortranMetadata> {

    private static final CompilerType INTEL_COMPILER_TYPE = new CompilerType() {
        @Override
        public String getIdentifier() {
            'ifort'
        }

        @Override
        public String getDescription() {
            'IntelFortran'
        }
    }
    
    @Override
    public CompilerType getCompilerType() {
       INTEL_COMPILER_TYPE
    }

    @Override
    protected IntelFortranMetadata parseCompilerOutput(String output, String error, File binary, List<File> path)
            throws BrokenResultException {
        // TODO Fix silly default values
        return new DefaultIntelFortranMetadata("ifort",VersionNumber.parse("10"))
    }

    @Override
    protected List<String> compilerArgs() {
        Collections.unmodifiableList(['--version'])
    }
    
    private static class DefaultIntelFortranMetadata implements IntelFortranMetadata {
        private final String versionString;
        private final VersionNumber version;
    
        DefaultIntelFortranMetadata(String versionString, VersionNumber version) {
            this.versionString = versionString;
            this.version = version;
        }
    
        @Override
        public String getVendor() {
            return versionString;
        }
    
        @Override
        public VersionNumber getVersion() {
            return version;
        }
    }
}

