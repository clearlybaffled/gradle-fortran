# Gradle Fortran Plugin

This plugin adds the ability for gradle to compile native code written in fortran, provided you have either the GNU Fortran or Intel Fortran compilers installed on your system.  It relies on the older software model method of defining native tool chains (see the [official Gradle documentation](https://docs.gradle.org/current/userguide/native_software.html)).

## How to use

Add to your plugins section

```
plugins {
    id 'io.github.clearlybaffled.fortran' version '0.1'
}

```

Add one toolchain to your model configuration, for which ever compiler is installed on your system. Multiple compilers will be search in the order listed. Add other language tool chains using the [docs](https://docs.gradle.org/current/userguide/native_software.html) if needed - declaring the `toolChains { }` block here removes the defaults.

```
model {
    toolChains {
        gfort(GFortran) {
            eachPlatform {
                cCompiler.withArguments { args ->
                	   args << "-x f95"
                }
            }
        }
        ifort(IntelFortran) {
            eachPlatform {
                cCompiler.executable '/opt/intel/bin/ifort'
            }
        }
    }
}
```

Declare your component(s) NativeBinary.  This plugin follows the convention of other native source assumes that you place your source in `src/${componentName}/fortran`, although it is configurable like any other model language plugins:

```
model {
    components {
        funProg(NativeBinaryExecutable) {
           sources {
               fortran {
                   lib library: "myLib"
                   exclude "**/*.f77"    // Let's keep it newer, please
               }
           }        
        }
        myLib(NativeLibraryExecutable) {
            sources {
                fortran {
                    exportedHeaders {
                        srcDir "src/headers"
                }
            }
         }
    }
}
```
