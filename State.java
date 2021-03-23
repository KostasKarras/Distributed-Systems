import java.util.ArrayList;
import java.lang.Math;
import java.util.Iterator;

public class State implements Comparable<State> {
	
	private final ArrayList<Integer> toCross;
	private final ArrayList<Integer> crossed;
	private final boolean lamp; // 0 when lamp in the starting side, 1 when lamp in the destination side
	private final int score; // score = g(n) + h(n), where g(n) = trueCost && h(n) = heuristic
	private int cost; // cost to travel in the tree so far
	//private int extraTime; // cost of the next move
	private State father;

	/**
	 * Constructor
	 * @param toCross	list of members that have not crossed the bridge
	 * @param crossed	list of members that have crossed the bridge
	 * @param lamp		side of the lamp(false for right, true for left)
	 * @param extraTime	cost of the next move
	 * @param father	father state of this state
	 */
	public State(ArrayList<Integer> toCross, ArrayList<Integer> crossed, boolean lamp, int extraTime, State father) { // made it public
		this.toCross = toCross;
		this.crossed = crossed;
		this.lamp = lamp;
		//this.extraTime = extraTime;
		this.father = father;
		score = trueCost(extraTime) + heuristic();
	}

	/**
	 * Constructor used for initial state, overrides previous constructor
	 * @param toCross	list of members that have not crossed the bridge
	 */
	public State(ArrayList<Integer> toCross) {
		this(toCross, null, false, 0, null);
	}

	/**
	 * Inside method used to represent implementation of getChildren
	 * @param a		represents either toCross or crossed arraylist, depending on where the lamp is
	 * @param lamp	side of the lamp(false for right, true for left)
	 * @return		a list of children of the current state
	 */
	private ArrayList<State> findChildren(ArrayList<Integer> a, boolean lamp){
		
		ArrayList<State> children = new ArrayList<>();
		ArrayList<Integer> temp ;
		ArrayList<Integer> temp1 ;
		State child;
		
		int child_extraTime;

		if(!lamp){
			if(getFather() == null && a.size() == 1){
				temp = new ArrayList<>();
				temp.add(this.getToCross().get(0));

				temp1 = new ArrayList<>();

				temp1.add(a.get(0));
				temp.remove(a.get(0));
				
				child_extraTime = a.get(0);

				child = new State(temp, temp1, true, child_extraTime, this);

				children.add(child);

				return children;
			}
			for(int i = 0; i < a.size() - 1; i++){
				for(int j = i + 1; j < a.size(); j++){

					temp = new ArrayList<>();
					if(getToCross() != null){
						Iterator<Integer> person = this.toCross.iterator();
						while (person.hasNext()) {
							temp.add(person.next());
						}
					}

					temp1 = new ArrayList<>();
					if(getCrossed() != null){
						Iterator<Integer> person2 = this.crossed.iterator();
						while (person2.hasNext()) {
							temp1.add(person2.next());
						}
					}

					temp1.add(a.get(i));
					temp1.add(a.get(j));
					temp.remove(a.get(i));
					temp.remove(a.get(j));

					child_extraTime = Math.max(a.get(i), a.get(j));

					child = new State(temp, temp1, true, child_extraTime, this);

					children.add(child);
				}
			}
		}
		else{
			for(int i = 0;i < a.size();i++){
				temp = new ArrayList<>();
				if(getToCross() != null){
					Iterator<Integer> person = this.toCross.iterator();
					while (person.hasNext()) {
						temp.add(person.next());
					}
				}

				temp1 = new ArrayList<>();
				if(getCrossed() != null){
					Iterator<Integer> person2 = this.crossed.iterator();
					while (person2.hasNext()) {
						temp1.add(person2.next());
					}
				}

				temp.add(a.get(i));
				temp1.remove(a.get(i));

				child_extraTime = a.get(i);

				child = new State(temp, temp1, false, child_extraTime, this);

				children.add(child);
			}
		}
		return children;
	}

	/**
	 * Method that returns the children of the current state
	 * @return	a list of children of the current state
	 */
	public ArrayList<State> getChildren() {
		if(!this.lamp) {
			return this.findChildren(this.getToCross(), this.lamp);
		}
		else {
			return this.findChildren(this.getCrossed(), this.lamp);
		}
	}

	/**
	 * Method that finds the value of the cost of this state and returns it
	 * @param extraTime	cost of the next move
	 * @return			cost of the current state
	 */
	private int trueCost(int extraTime) {
		if(getFather() == null)
			return 0;
		setCost(getFather().getCost() + extraTime);
		return getCost();
	}

	/**
	 * Implementation of heuristic function
	 * @return	h(n)
	 */
	private int heuristic() {

		int heuristic_score = 0;
		int i = 0;
		if(toCross.size() != 0){
			if(toCross.size() % 2 == 0){
				while(i < toCross.size()){
					heuristic_score += toCross.get(++i);
					i++;
				}
			}
			else{
				while(i < toCross.size()){
					heuristic_score += toCross.get(i);
					i += 2;
				}
			}
		}
		return heuristic_score;
	}

	/**
	 * Method that determines whether current state is terminal
	 * @return	true if current state is terminal, false otherwise
	 */
	public boolean isTerminal() {
		if(toCross.size() == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Prints state's state
	 */
	public void print() {

		//Build to_Cross arraylist
		StringBuilder tocross_builder = new StringBuilder("");
		int tocross_size = 0;
		if(getToCross() != null){
			for(int person : toCross) {
				tocross_builder.append(person).append(", ");
			}
			tocross_size = toCross.size();
		}
		if (tocross_size != 0) {
			tocross_builder.deleteCharAt(tocross_builder.length() - 2);
		}
		String str_toCross = tocross_builder.toString();

		//Build crossed arraylist
		StringBuilder crossed_builder = new StringBuilder("");
		int crossed_size = 0;
		if(getCrossed() != null){
			for(int person : crossed) {
				crossed_builder.append(person).append(", ");
			}
			crossed_size = crossed.size();
		}
		if(crossed_size != 0){
			crossed_builder.deleteCharAt(crossed_builder.length() - 2);
		}
		String str_crossed = crossed_builder.toString();

		System.out.printf("On the right side there are %d people that need the following times respectively to cross the bridge: %s \n", tocross_size, str_toCross);
		System.out.printf("On the left side there are %d people that need the following times respectively to cross the bridge: %s \n", crossed_size, str_crossed);
		System.out.println("***********************************************************************************************************************");
	}

	public boolean equals(State s) {
		if (this.toCross != s.toCross) {
			return false;
		}
		if (this.lamp != s.lamp) {
			return false;
		}
		return true;
	}
	
	@Override
	public int compareTo(State s) {
		return Integer.compare(this.score, s.score);
	}

	/*
	Getters and Setters
	 */

	public ArrayList<Integer> getToCross(){
		return this.toCross;
	}

	public ArrayList<Integer> getCrossed(){
		return this.crossed;
	}

	public State getFather() {
		return father;
	}

	public void setFather(State father) {
		this.father = father;
	}

	public int getCost() {
		return this.cost;
	}

	public void setCost(int newCost) {
		this.cost = newCost;
	}
}