import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

class Pager{

	Scanner rand;
	int m; // Machine Size
	int p; // Page Size
	int s; // Process size
	int j; // Job Mix
	int n; // Number of references per Process
	String r; // Replacement Algorithm
	int[][] frameTable; // Contains the words of each frame, and the process number
	int[] t; // Load Times for each frame 
	Process[] processes; // Array containing all the processes
	int numFrames; 
	int numProcesses;
	ArrayList<Integer> lru = new ArrayList<Integer>(); // Keeps track of the frames used in order

	public static void main(String[] args) throws NumberFormatException, FileNotFoundException{

		if (args.length != 6){
			System.out.println("Please enter the valid amount of inputs (6)");
		}else{

			int m = Integer.parseInt(args[0]);
			int p = Integer.parseInt(args[1]);
			int s = Integer.parseInt(args[2]);
			int j = Integer.parseInt(args[3]);
			int n = Integer.parseInt(args[4]);
			String r = args[5];
	
			File f = new File("random-numbers.txt");
			Scanner rand = new Scanner(f);
			
			Pager pager = new Pager(m, p, s, j, n, r, rand);
			pager.run();
			pager.printResults();

		}

	}	

	public Pager(int m, int p, int s, int j, int n, String r, Scanner rand){
		this.m = m;
		this.p = p;
		this.s = s;
		this.j = j;
		this.n = n;
		this.r = r;
		this.rand = rand;
		numFrames = m/p;
		if (j == 1){
			frameTable = new int[numFrames][2];
			numProcesses = 1;
		}else{
			frameTable = new int[numFrames][2];
			numProcesses = 4;

		}t = new int[numFrames];
		processes = new Process[numProcesses + 1]; // Does not use row 0 so that row 1 can represent process 1
		

		for (int[] row : frameTable){
			Arrays.fill(row, -1); 
		}

		for (int x = 1; x <= numProcesses; x++){
			processes[x] = new Process(n);
		}

		for (int f = 0; f < numFrames; f++){
			lru.add(f);
		}

	}

	public void run(){
		int time = 1;
		int process = 1; // current process number
		int page; // page of current reference
		int frame = -1; // frame of current reference 
		int ra = 0; // keeps track of current random int
		int lifo = 0; // keeps track of last frame to go in for LIFO
		Process p; // current process
		
		while(!allDone()){
			p = processes[process];
			for (int q = 0; q < 3; q++){ //quantum = 3	
				if (p.getRemaining() > 0){
					// First calculate the next word 
					if (p.getRemaining() == n){
						p.setWord((111 * process) % s);
					}

					// System.out.printf("%d references word %d (page %d) at time %d: ", process, p.getWord(), getPage(p.getWord()), time);

					// Check to see if any hits occur
					page = getPage(p.getWord());
					for (int x = 0; x < numFrames; x++){
						if (process == frameTable[x][0] && page == frameTable[x][1]){
							frame = x;
							lru.remove(new Integer(frame));
							lru.add(frame);
							// System.out.printf("Hit in frame %d\n", x);
						}
					}
					
					// A fault has occurred
					int next = nextEmpty();
					if (frame == -1){
						p.fault();

						// Look for empty space
						if (nextEmpty() >= 0){
							// System.out.printf("Fault using free frame %d\n", next);
							frameTable[next][0] = process;
							frameTable[next][1] = getPage(p.getWord());
							lru.remove(new Integer(next));
							lru.add(next);
							t[next] = time;
							lifo = next;
						}

						// If no empty space, use replacement algorithms						
						else if (nextEmpty() == -1){
							if (r.equals("lifo") || r.equals("LIFO") || r.equals("Lifo")){
								frame = lifo;
								processes[frameTable[frame][0]].evict();
								processes[frameTable[frame][0]].addResidency(time - t[frame]);
								// System.out.printf("Fault, evicting page %d of %d from frame %d\n", frameTable[frame][1],frameTable[frame][0], frame);
								frameTable[frame][0] = process;
								frameTable[frame][1] = getPage(p.getWord());
								t[frame] = time;
							}else if (r.equals("random") || r.equals("RANDOM") || r.equals("Random")){
								int rr = rand.nextInt();
								frame = rr % numFrames;
								processes[frameTable[frame][0]].evict();
								processes[frameTable[frame][0]].addResidency(time - t[frame]);
								// System.out.printf("Fault, evicting page %d of %d from frame %d\n", frameTable[frame][1],frameTable[frame][0], frame);
								frameTable[frame][0] = process;
								frameTable[frame][1] = getPage(p.getWord());
								t[frame] = time;
							}else if (r.equals("lru") || r.equals("LRU") || r.equals("Lru")){
								frame = lru.get(0);
								lru.remove(new Integer(frame));
								lru.add(frame);
								processes[frameTable[frame][0]].evict();
								processes[frameTable[frame][0]].addResidency(time - t[frame]);
								// System.out.printf("Fault, evicting page %d of %d from frame %d\n", frameTable[frame][1],frameTable[frame][0], frame);
								frameTable[frame][0] = process;
								frameTable[frame][1] = getPage(p.getWord());
								t[frame] = time;
							}
						}
					}ra = rand.nextInt();

					int nword = nextWord(p.getWord(), process, ra);
					// System.out.printf("\nSetting next word: (%d) - (%d) - (%d) ==> %d\n", p.getWord(), process, ra, nword);
					p.setWord(nword);
					p.load();
					time++;
					frame = -1;
				}
			}process = (process % numProcesses) + 1;		
			
		}
	}

	public int getPage(int word){
		return (word / p);
	}

	public boolean allDone(){
		for (int x = 1; x <= numProcesses; x++){
			if (processes[x].getRemaining() > 0){
				return false;
			}
		}return true;
	}

	public int nextEmpty(){
		for (int n = numFrames-1; n >=0; n--){
			if (frameTable[n][0] == -1){
				return n;
			}			
		}return -1;
	}

	public void getRemaining(){
		for (int x = 1; x <= numProcesses; x++){
			System.out.printf("Process %d has %d remaining\n", x, processes[x].getRemaining());
		}
	}

	public void printResults(){
		double res = 0;
		int ev = 0;
		int faults = 0;

		System.out.printf("The machine size is %d\n", m);
		System.out.printf("The page size is %d\n", p);
		System.out.printf("The process size is %d\n", s);
		System.out.printf("The job mix is %d\n", j);
		System.out.printf("The numer of references per process is %d\n", n);
		System.out.printf("The replacement algorithm is %s\n\n", r);

		for (int x = 1; x <= numProcesses; x++){
			res += (double)processes[x].getResidency();
			ev += processes[x].getEvictions();
			faults += processes[x].getFaults();
			System.out.printf("Process %d had %d faults", x, processes[x].getFaults());
			if (processes[x].getEvictions() == 0){
				System.out.printf(".\n\tWith no evictions, the average residence is undefined.\n");
			}else if (processes[x].getEvictions() > 0){
				System.out.printf(" and %f average residency.\n", (double)processes[x].getResidency()/processes[x].getEvictions());
			}
		}System.out.printf("\nThe total number of faults is %d and the overall average residency is %f\n", faults, res/ev);
	}

	public int nextWord(int currWord, int process, int r){
		double y = r/(Integer.MAX_VALUE + 1d);
		
		if (j == 1 | j == 2){
			if (y < 1){
				return (currWord + 1) % s;
			}else{
				r = rand.nextInt();
				return r % s;
			}

		}else if (j == 3){
			r = rand.nextInt();
			return r % s;
		}else if (j == 4){
			if (process == 1){
				if (y < .75){
					return (currWord + 1) % s;
				}else if (y < 1){
					return (currWord - 5 + s) % s;
				}else {
				r = rand.nextInt();
					return r % s;
				}
			}else if (process == 2){
				if (y < .75){
					return (currWord + 1) % s;
				}else if (y < 1){
					return (currWord + 4) % s;
				}else {
					r = rand.nextInt();
					return r % s;
				}
			}else if (process == 3){
				if (y < .75){
					return (currWord + 1) % s;
				}else if (y < .875){
					return (currWord - 5 + s) % s;
				}else if (y < 1){
					return (currWord + 4) % s;
				}else {
					r = rand.nextInt();
					return r % s;
				}
			}else if (process == 4){
				if (y < .5){
					return (currWord + 1) % s;
				}else if (y < .625){
					return (currWord - 5 + s) % s;
				}else if (y < .75){
					return (currWord + 4) % s;
				}else {
					r = rand.nextInt();
					return r % s;
				}
			}
		}return -1;

	}

}