package application;

class City {
	String name; 
	Next[] next;
	int nextCount; 
	int index; 
	int phase; 

	City(String name, int maxConnections, int index) {
		this.name = name;
		this.next = new Next[maxConnections]; // preallocated
		this.nextCount = 0;
		this.index = index;
		this.phase = -1; // we eilluse it after we build the graph
	}

	void addNext(String dest, int petrol, int hotel) {
		next[nextCount++] = new Next(dest, petrol, hotel);
	}

	void print() {
		System.out.print("City " + name + " [Phase: " + phase + "] → ");
		for (int i = 0; i < nextCount; i++) {
			Next c = next[i];
			System.out.print("[" + c.destination + ", " + c.petrol + ", " + c.hotel + "] ");
		}
		System.out.println();
	}

}
