1. entry the directory: project
	cd  project
2. exec cmd to compile project: 
	javac -d ./classes src/*.java src/http/*.java
3. entry directory: classes
	cd classes
4. Then exec cmd: 
	java Main


Notice: This Java program requires JDK 17 or higher for compilation and execution.This is due to the use of the .readString() function, which necessitates a JDK 11+ version in the compilation environment.
Before compiling, it is safe to remove all contents within the "classes" folder, with the exception of the "logs" and "static" folders. These two folders should remain untouched for the project to function correctly.
