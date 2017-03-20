# Two-pass Linker

This Linker processes a series of object modules and creates a map of all the absolute addresses of the input. 

Modules contain:
1. Definition List - Symbols and their relative addresses
2. Use List - Represents the order that the symbols will be referenced by
3. Program Text - Length of the module followed by 5-digit integers 

First Pass:
- Determines the base address of each module.
- Determines the absolute address of each symbol and stores it in the Symbol Table

Second Pass:
- Generates absolute address by relocating the relative addresses and resolving external references via the base addresses and symbol table.


To compile the file, run the following on your terminal:
⋅⋅⋅```javac Linker.java```

The program takes one argument (input file). To run it, run the program with the name of your input file as the argument:
⋅⋅⋅```java Linker <input file>```
