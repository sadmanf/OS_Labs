public class Process{
	
	int residency = 0;
	int evictions = 0;
	int faults = 0;
	int remaining;
	int word;

	public Process(){

	}

	public Process(int r){
		remaining = r;
	}

	public void load(){
		remaining--;
	}

	public void reside(){
		residency++;
	}

	public void evict(){
		evictions++;
	}

	public void fault(){
		faults++;
	}

	public void setWord(int w){
		word = w;
	}

	public void addResidency(int r){
		residency += r;
	}

	public int getResidency(){
		return residency;
	}

	public int getEvictions(){
		return evictions;
	}

	public int getFaults(){
		return faults;
	}

	public int getRemaining(){
		return remaining;
	}

	public int getWord(){
		return word;
	}
}
