import java.util.Scanner;
import java.io.*;
import java.util.Collections;
import java.util.ArrayList;


class Scheduler{
	
	public static boolean verbose = false; 
	public static ArrayList<String> input = new ArrayList<String>();
	public static ArrayList<Process> Processes = new ArrayList<Process>();
	public static ArrayList<Integer> randList = new ArrayList<Integer>();
	public static int randc;
	public static int overlap;

	public static void main(String[] args) throws FileNotFoundException{
		if (args.length == 0){
			System.out.println("Please enter a valid filename");
		}else if (args.length > 2){
			System.out.println("Do not enter more than two arguments");
		}else if (args.length > 1 && !args[0].equals("--verbose")){
			System.out.println("Incorrect flag argument. Did you mean --verbose"); 
		}else{
			if (args[0].equals("--verbose")){
				verbose = true;
			}try{				
				File f = new File(args[args.length-1]);
				File rn = new File("random-numbers.txt");		
				Scanner s = new Scanner(f);
				Scanner r = new Scanner(rn);

				Scheduler sched = new Scheduler();
				randList = sched.getNumList(r);

				sched.setProcesses(s);
				sched.printHeading(Integer.parseInt(input.get(0)));

				
				sched.printSummary("Uniprocessing");
				sched.reset();
				sched.printSummary("First Come First Serve");
				sched.reset();
				sched.printSummary("Shortest Job First");
				sched.reset();
				sched.printSummary("Round Robin");

				s.close();					
			}catch (FileNotFoundException ex){
				System.out.println("File not found");
			}
		}
	}

	public void setProcesses(Scanner s){

		// Put all data from inputs into ArrayList
		while(s.hasNextLine()){
			String i = s.nextLine();
			for (String str: i.split(" ")){
				if (str.trim().length() > 0){
					str = str.replaceAll("[()]","");
					input.add(str);
				}
			}
		}

		//Create an ArrayList of all the processes from the input
		for (int n = 0; n < Integer.parseInt(input.get(0)); n++){
			int index = (n * 4) + 1;
			Processes.add(new Process(Integer.parseInt(input.get(index)), Integer.parseInt(input.get(index + 1)), Integer.parseInt(input.get(index + 2)), Integer.parseInt(input.get(index + 3))));
		}
	}

	public static void setOverlap(int n){
		overlap = n;
	}

	public static int getOverlap(){
		return overlap;
	}

	public ArrayList<Integer> getNumList(Scanner s){
		ArrayList<Integer> NumList = new ArrayList<Integer>();
		while (s.hasNextInt()){
			NumList.add(s.nextInt());
		}return NumList;
	}

	public void printHeading(int n){
		System.out.printf("The original input was: %d %s\n", n, printProcesses());
		Collections.sort(Processes);
		System.out.printf("The (sorted) input is:  %d %s\n", n, printProcesses());		
	}

	public String printProcesses(){
		String result = "";
		for (Process m: Processes){
			result += String.format("%s ", m.toString());
		}return result;
	}

	public void printSummary(String algorithm){
		int fin = 0;
		float cpu = 0f;
		float io = 0f;
		float throughput = 0f;
		float aTurnaround = 0f;
		float aWaiting = 0f;
		int l;

		System.out.printf("\n\n##########     %s     ##########\n\n", algorithm);
		if (algorithm.equals("Uniprocessing")){
			Uniprogram uni = new Uniprogram(Processes, randList, verbose);
			uni.run();
		}if (algorithm.equals("First Come First Serve")){
			FCFS fcfs = new FCFS(Processes, randList, verbose);
			fcfs.run();			
		}if (algorithm.equals("Shortest Job First")){
			SJF sjf = new SJF(Processes, randList, verbose);
			sjf.run();			
		}if (algorithm.equals("Round Robin")){
			RR rr = new RR(Processes, randList, verbose);
			rr.start();			
		}



		for(l = 0; l < Processes.size(); l++){
			Process pl = Processes.get(l);
			pl.printProcessInfo(l);
			if (pl.getFinTime() > fin){
				fin = pl.getFinTime();
			}
			cpu += pl.getCPUTime();
			io += pl.getIOTime();	
			aTurnaround += pl.getFinTime() - pl.getArrival();
			aWaiting += pl.getWaitingTime();				
		}
		System.out.printf("Summary Data:\n");
		System.out.printf("\tFinishing time: %d\n", fin);
		System.out.printf("\tCPU Utilization: %f\n", cpu / fin);
		System.out.printf("\tI/O Utilization: %f\n", (io-getOverlap()) / fin);
		System.out.printf("\tThroughput: %f processes per hundred cycles\n", (float)l / fin * 100);
		System.out.printf("\tAverage turnaround time: %f\n", (float)aTurnaround/l);
		System.out.printf("\tAverage Waiting time: %f\n\n", (float)aWaiting/l);
	
	}

	public static int randomOS(int u){
		int x = randList.get(randc);
		randc++;
		return 1+(x % u);
	}

	public static void reset(){
		randc = 0;
		overlap = 0;
		for (Process pr: Processes){
			pr.reset();
		}
	}

}

	