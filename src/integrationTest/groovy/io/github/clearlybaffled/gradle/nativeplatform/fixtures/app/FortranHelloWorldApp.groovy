package io.github.clearlybaffled.gradle.nativeplatform.fixtures.app

class FortranHelloWorldApp extends HelloWorldApp {

	@Override	
	List<SourceFile> getSourceFiles() {
		librarySources + mainSource
	}

	@Override
	List<SourceFile> getHeaderFiles() {
		[]
	}

	List<String> pluginList = ['io.github.clearlybaffled.fortran']
	
	@Override
	SourceFile getMainSource() {
		new SourceFile("fortran", "hello.f95", """
program hello
#ifdef FRENCH
     print *, "${HELLO_WORLD_FRENCH}"
#else
     print *, "${HELLO_WORLD}"
#endif
end program hello
""")
	}
	
	
	@Override
	public SourceFile getLibraryHeader() {
		
	}
 
	
	List<SourceFile> librarySources = []
	
	String sourceSetType = "FortranSourceSet"
	
}