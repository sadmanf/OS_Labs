import java.io.*;
import java.util.ArrayList;

class SJF{

	ArrayList<Process> processes;
	ArrayList<Integer> nums;
	int[] remaining;

	boolean verbose;

	public SJF(ArrayList<Process> p, ArrayList<Integer> nums, boolean v){
		this.processes = p;
		this.nums = nums;
		this.verbose = v; 
		remaining = new int[processes.size()];
		for (int n = 0; n < processes.size(); n++){
			remaining[n] = processes.get(n).getCPUTime();
		}
	}

	public void run(){
		int numprocess = processes.size();
		int cycle = 0;
		int rem = numprocess;
		while (processes.get(0).getArrival() > cycle){
			printVerbose(cycle);
			cycle++;
		}update(cycle, rem);
		printVerbose(cycle);

		while (rem > 0){
			cycle++;
			rem = update(cycle, rem);
			printVerbose(cycle);
		}
	}

	public int update(int cycle, int rem){
		int numblocked = 0;
		int counter = rem;

		for (int n = 0; n < processes.size(); n++){
			Process p = processes.get(n);
			if (p.getState().equals("ready")){
				p.setWaitingTime(p.getWaitingTime() + 1);
			}else if (p.getState().equals("running")){
				p.setRemaining(p.getRemaining() - 1);
				p.increment();
				remaining[n]--;
				if(p.getCurrCPU() == p.getCPUTime()){
					p.setState("terminated");
					p.setFinTime(cycle-1);
					counter = rem - 1;
				}
			}else if (p.getState().equals("blocked")){
				numblocked++;
				p.setRemaining(p.getRemaining() - 1);
				p.setIOTime(p.getIOTime() + 1);
			}
		}if (numblocked > 1){
			Scheduler.setOverlap(Scheduler.getOverlap() + numblocked - 1);
		}numblocked = 0;

		for (int c = 0; c < processes.size(); c++){
			Process p = processes.get(c);	
			if (p.getState().equals("terminated")){
				}else if (p.getArrival() < cycle && p.getState().equals("unstarted")){
				p.setState("ready");		
			}else if (p.getState().equals("blocked")){
				if (p.getRemaining() == 0){
					p.setState("ready");
				}
			}else if (p.getState().equals("running")){
				if (p.getRemaining() == 0){
					p.setState("blocked");
					p.setRemaining(p.getioburst());
				}
			}
		}if (!isRunning() && lowestRem() >= 0){
			int cpuburst;
			int index = lowestRem();
			processes.get(index).setState("running");
			cpuburst = Scheduler.randomOS(processes.get(index).getB());
			processes.get(index).setRemaining(cpuburst);
			processes.get(index).setioburst(cpuburst * processes.get(index).getM());
		}

		return counter;
	}

	public int lowestRem(){
		int rem = 2147483646;
		int lowestindex = -1;
		for (int n = 0; n < processes.size(); n++){
			String state = processes.get(n).getState();
			if (state.equals("ready")){
				if (remaining[n] < rem){
					rem = remaining[n];
					lowestindex = n;
				}
			}
		}return lowestindex;
	}

	public boolean isRunning(){
		for (Process pr:processes){
			if (pr.getState().equals("running")){
				return true;
			}
		}return false;
	}


	public void printTime(){
		for (int n = 0; n < processes.size(); n++){
			System.out.printf("Process %d: %d remaining\t", n, processes.get(n).getCPUTime() - processes.get(n).getCurrCPU());
		}System.out.println();
	}

	public void printVerbose(int cycle){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < processes.size(); m++){
				System.out.printf("%13s %2d", processes.get(m).getState(), processes.get(m).getRemaining());
			}System.out.println();
		}
	}

}