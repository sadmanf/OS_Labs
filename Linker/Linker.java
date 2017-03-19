import java.util.Scanner;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;


class Linker{

	public static int MEMSIZE = 600; 

	public static ArrayList<String> input = new ArrayList<String>();
	public static ArrayList<Integer> moduleSizes = new ArrayList<Integer>();
	public static Map<String, Integer> symbolTable = new LinkedHashMap<String, Integer>();
	public static int numModules;
	
	public static void main(String[] args) throws FileNotFoundException{

		if (args.length == 0){
			System.out.println("Please enter a valid filename");
		}else if (args.length > 1){
			System.out.println("Do not enter more than one argument");
		}else{
			try{
				File f = new File(args[0]);		
				Scanner s = new Scanner(f);
				String i;
				
				
				while(s.hasNextLine()){
					i = s.nextLine();
					for (String str: i.split(" ")){
						if (str.trim().length() > 0){
							input.add(str);
						}
					}
				}//System.out.println(input);
				
				Linker l = new Linker();
				l.FirstPass();
				l.printSymbolTable();
				System.out.println();
				l.SecondPass();
				s.close();

			}catch (FileNotFoundException ex){
				System.out.println("File not found");
			}
		}

	}


	public void FirstPass(){
		numModules = Integer.parseInt(input.get(0));
		int index = 1;
		int n = 0;
		int mSize = 0;
		int baseAddress = 0; //absolute address of the beginning of the current module
			

		// Iterate through each module 
		for (int iter = 0; iter < numModules; iter++){
			// Calculate the size of the current module		
			mSize = index + (Integer.parseInt(input.get(index)) * 2) + 1;
			mSize += Integer.parseInt(input.get(mSize)) + 1;

			int size = 0;
			for (int s = 0; s < Integer.parseInt(input.get(mSize)); s++){
				if (input.get(mSize + s + 1).length() == 5){
					size++;
				}
			}moduleSizes.add(size);

			// int s = mSize+1;
			// while (input.get(s).length() == 5){
			// 	s++;
			// }System.out.println("size: " + (s - mSize));


			// Go through definition list and set up symbolTable
			int i = 0;
			for (i = 0; i < Integer.parseInt(input.get(index)); i++){
				n = index + (2 * i);
				if (!symbolTable.containsKey(input.get(n+1))){
					if (Integer.parseInt(input.get(n + 2)) > moduleSizes.get(iter) - 1){
						System.out.println("Error: Address in definition of " + input.get(n+1) + " exceeds the size of the module. The address will be treated as 0.");
						symbolTable.put(input.get(n+1), baseAddress);	
					}else{
						symbolTable.put(input.get(n+1), Integer.parseInt(input.get(n+2)) + baseAddress);
					}
				}else{
					System.out.println("Error: Symbol already defined. Original value is being used");
				}
			}index += (2*i) + 1;
			//Set the index to the start of the definition list of the next module
			index += Integer.parseInt(input.get(index)) + 1;
			// System.out.println(Integer.parseInt(input.get(index)) + " - " + size);
			index += size + 1;
			baseAddress += moduleSizes.get(iter);
		}
	}

	public void SecondPass(){
		int index = 1;
		int baseAddress = 0;
		ArrayList<String> used = new ArrayList<String>();

		for (int iter = 0; iter < numModules; iter++){
			ArrayList<String> useList = new ArrayList<String>();

			// Store the symbols that appear in the use list
			index += (Integer.parseInt(input.get(index)) * 2) + 1;
			int numUsed = Integer.parseInt(input.get(index));
			for (int i = 0; i < numUsed; i++){
				useList.add(input.get(index + i + 1));
			}
			// Set index to beginning of program text
			index += Integer.parseInt(input.get(index)) + 1;
			
			// Go through each module
			for (int n = 1; n <= moduleSizes.get(iter); n++){
				int num = Integer.parseInt(input.get(index + n));
				int addressType =  num % 10;
				int addressField = (num / 10) % 1000;
				num = num/10;

				if (addressType == 1){
					System.out.printf("%d: %d\n", baseAddress + n - 1, num);
				}else{
					if (addressField > MEMSIZE){
						System.out.println("Error: Address of below exceeds the size of the machine. A value of 0 is being used.");
						num -= addressField;
					}if (addressType == 2){
						System.out.printf("%d: %d\n", baseAddress + n - 1, num);
					}else if(addressType == 3){
						if(addressField > moduleSizes.get(iter)){
							num = num/1000 * 1000;
							System.out.println("Error: Relative address exceeds module size. A value of 0 is being used.");
						}else{
							num += baseAddress;
						}
						System.out.printf("%d: %d\n", baseAddress + n - 1, num);
					}else if(addressType == 4){
						num -= addressField;
						
						if (addressField > numUsed){
							num += addressField;
							System.out.printf("Error: The external address %d is too large to reference an entry in the use list. The address will be treated as immediate.\n", addressField);
						}else if (symbolTable.containsKey(useList.get(addressField))){
							num += symbolTable.get(useList.get(addressField));							
						}else{
							System.out.printf("Error: Symbol %s was used but not defined. It has been given the value 0.\n", useList.get(addressField));
						}if (addressField < numUsed && !used.contains(useList.get(addressField))){
							used.add(useList.get(addressField));
						}
						System.out.printf("%d: %d\n", baseAddress + n - 1, num);
					}
				}
			}
			for (int x = 0; x < useList.size(); x++){
				if (!used.contains(useList.get(x)))
						System.out.printf("Warning: Symbol %s appears in the use list but has not been used in the module\n", useList.get(x));
				}
			
			
			index += moduleSizes.get(iter) + 1;
			baseAddress += moduleSizes.get(iter);
		}for (String key: symbolTable.keySet()){
			if(!used.contains(key))
				System.out.printf("Warning: Symbol %s is defined but not used.\n", key);
		}	
	}

	public void printSymbolTable(){
		System.out.println("Symbol Table");
		for (String key: symbolTable.keySet()){
			System.out.println(key + " = " + symbolTable.get(key).toString() );
		}
		
	}

}