import java.io.*;
import java.util.ArrayList;

class Uniprogram{

	ArrayList<Process> p;
	ArrayList<Integer> nums;
	boolean verbose;

	public Uniprogram(ArrayList<Process> p, ArrayList<Integer> nums, boolean v){
		this.p = p;
		this.nums = nums;
		this.verbose = v; 
	}

	public void run(){
		int numprocess = p.size();
		int cycle = 0;
		boolean terminated = false;
		int cpuburst;
		int ioburst;
		
		printVerbose(numprocess, cycle);
		for (int n = 0; n < numprocess; n++){
			Process pn = p.get(n);
			int cpuTime = 0;
			int ioTime = 0;
			pn.setWaitingTime(cycle - pn.getArrival());
			while (cpuTime < pn.getCPUTime()){
				while (pn.getArrival() > cycle){
					cycle++;
					printVerbose(numprocess,cycle);
				}update(numprocess,cycle,n);
				
				cpuburst = Scheduler.randomOS(p.get(n).getB());
				ioburst = cpuburst * p.get(n).getM(); 
				if (cpuburst > pn.getCPUTime() - cpuTime){
					cpuburst = pn.getCPUTime() - cpuTime;
				}

				for (int running = cpuburst; running > 0; running--){
					cycle++;
					cpuTime++;
					pn.setState("running");
					pn.setRemaining(running);
					update(numprocess, cycle, n);
					printVerbose(numprocess,cycle);
				}if (cpuTime != pn.getCPUTime()){
					for (int blocked = ioburst; blocked > 0; blocked--){
						cycle++;
						ioTime++;
						pn.setState("blocked");
						pn.setRemaining(blocked);
						update(numprocess, cycle, n);
						printVerbose(numprocess, cycle);
					}
				}
			}pn.setState("terminated");	
			pn.setFinTime(cycle);
			pn.setIOTime(ioTime);
		}
	}

	public void update(int numprocess, int cycle, int n){
		for (int c = n; c < numprocess; c++){
			Process pc = p.get(c);
			if (pc.getArrival() < cycle && pc.getState().equals("unstarted")){
				pc.setState("ready");			
			}
		}
	}

	public void printVerbose(int numprocess, int cycle){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < numprocess; m++){
				System.out.printf("%13s %2d", p.get(m).getState(), p.get(m).getRemaining());
			}System.out.println();
		}
	}


}