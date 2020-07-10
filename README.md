# Gradle Fortran Plugin

This plugin adds the ability for gradle to compile native code written in fortran, provided you have either the GNU Fortran or Intel Fortran compilers installed on your system.

## How to use

Add to your plugins section

```
plugins {
    id 'io.github.clearlybaffled.fortran' version '0.1'
}

```

Add the toolchain to your model configuration

```
model {
    toolchain {
        gfort(GFortran)
        // or
        ifort(IntelFortran)
    }
}
```

The plugin assumes that you place your fortran in `src/$component/fortran`, although it is configurable like any other model language plugin:

```
model {
    components {
        funProg(NativeBinaryExecutable) {
            fortran {
                src 'src/funProg/fortran'    // default

            }
        }
    }
}
```
