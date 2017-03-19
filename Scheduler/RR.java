import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

class RR{

	public static int QUANTUM = 2;

	ArrayList<Process> processes;
	ArrayList<Integer> nums;
	ArrayList<Integer> q = new ArrayList<Integer>();
	boolean verbose;
	boolean notEnough = false;

	public RR(ArrayList<Process> p, ArrayList<Integer> nums, boolean v){
		this.processes = p;
		this.nums = nums;
		this.verbose = v; 
	}

	public void start(){
		int numprocess = processes.size();
		int cycle = 0;
		int rem = 100;
		while (processes.get(0).getArrival() > cycle){
			printVerbose(cycle);
			cycle++;
		}
		
		printVerbose(cycle);
		cycle++;
		while(!isDone()){
			cycle = run(cycle);
		}
	}

	public void update(int cycle){
		int numblocked = 0;
		for (int n = 0; n < processes.size(); n++){
			Process p = processes.get(n);
			if (p.getState().equals("terminated")){
			}else if (p.getState().equals("ready")){
				p.setWaitingTime(p.getWaitingTime() + 1);
			}else if (p.getState().equals("running")){
				p.setRemaining(p.getRemaining() - 1);
				p.increment();
			}else if (p.getState().equals("blocked")){
				p.setRemaining(p.getRemaining() - 1);
				p.setIOTime(p.getIOTime() + 1);
				numblocked++;
			}
		}if (numblocked > 1){
			Scheduler.setOverlap(Scheduler.getOverlap() + numblocked - 1);
		}
	}

	public void set(int cycle){

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
				if (notEnough){
				}
				if (p.getRemaining() == 0){
					p.setState("blocked");
					p.setRemaining(p.getioburst());
				}
			}
		}
	}

	public void checkUnstarted(int cycle){
		for (int c = 0; c < processes.size(); c++){
			Process p = processes.get(c);
			if (p.getArrival() < cycle && p.getState().equals("unstarted")){
				p.setState("ready");
				q.add(c);
			}
		}
	}

	public void checkQ(){
		for (int c = 0; c < processes.size(); c++){
			Process p = processes.get(c);
			if (p.getState().equals("ready") && !q.contains(c)){
				q.add(c);
			}else if (p.getState().equals("blocked") && p.getRemaining() == 0 && !q.contains(c)){
				q.add(c);
			}else if (p.getState().equals("running") && notEnough && !q.contains(c) && p.getCurrCPU() < p.getCPUTime()){
				q.add(c);
			}
		}
	}

	public int run(int cycle){
		checkUnstarted(cycle);
		checkTerm(cycle);
		while (isRunning()){
			update(cycle);
			set(cycle);
			checkQ();
			cycle++;
		}if (!isRunning() && q.size() == 0){
			printVerbose(cycle);
			update(cycle);
			set(cycle);
			checkQ();
			cycle++;
		}else if (!isRunning() && q.size() > 0){
			int index = q.get(0);
			q.remove(0);
			int r;
			int cpuburst;
			processes.get(index).setState("running");
			if (processes.get(index).getRemaining() == 0){
				cpuburst = Scheduler.randomOS(processes.get(index).getB());
				processes.get(index).setRemaining(cpuburst);
				processes.get(index).setioburst(cpuburst * processes.get(index).getM());				
			}
			
			r = (processes.get(index).getRemaining());
			
			boolean add = false;

			if (r > QUANTUM){
				r = QUANTUM;
				add = true;
			}
			boolean term = checkTerm(cycle);
			for (int n = r; n > 0; n--){
				term = checkTerm(cycle);							
				if (!term){
					checkUnstarted(cycle);
					if(add && n == 1){
						notEnough = true;	
					}
					printVerbose(cycle, n);
					update(cycle);
					checkQ();
					set(cycle);
					cycle++;
				}
				if (term && q.size() > 0){
					index = q.get(0);
					processes.get(index).setState("running");
					if (processes.get(index).getRemaining() == 0){
						cpuburst = Scheduler.randomOS(processes.get(index).getB());
						processes.get(index).setRemaining(cpuburst);
						processes.get(index).setioburst(cpuburst * processes.get(index).getM());
					}
				}
				
			}notEnough =false;
			if (processes.get(index).getRemaining() > 0 && processes.get(index).getState().equals("running")){
				processes.get(index).setState("ready");
			}else if (processes.get(index).getRemaining() == 0){
				processes.get(index).setState("blocked");
			}term = false;
		}return cycle;
	}

	public boolean checkTerm(int cycle){
		for (Process p:processes){
			if (!p.getState().equals("terminated") && p.getCPUTime() <= p.getCurrCPU()){
				p.setState("terminated");
				p.setRemaining(0);
				p.setFinTime(cycle-1);
				return true;
			}
		}return false;
	}

	public boolean isRunning(){
		for (Process pr:processes){
			if (pr.getState().equals("running")){
				return true;
			}
		}return false;
	}

	public boolean isDone(){
		for (Process pr:processes){
			if (!pr.getState().equals("terminated")){
				return false;
			}
		}return true;
	}

	public void printVerbose(int cycle){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < processes.size(); m++){
				System.out.printf("%13s %2d", processes.get(m).getState(), processes.get(m).getRemaining());
			}System.out.println();
		}
	}

	public void printVerbose(int cycle, int rem){
		if(verbose){
			System.out.printf("Before cycle %5d:", cycle);
			for (int m = 0; m < processes.size(); m++){
				if (processes.get(m).getState().equals("running")){
					System.out.printf("%13s %2d", processes.get(m).getState(), rem);
				}else if (processes.get(m).getState().equals("ready")){
					System.out.printf("%13s %2d", processes.get(m).getState(), 0);
				}else{
					System.out.printf("%13s %2d", processes.get(m).getState(), processes.get(m).getRemaining());
				}

			}System.out.println();
		}
	}

}
