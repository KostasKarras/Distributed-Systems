
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class SpaceSearcher {
	
	private static ArrayList<State> states;
	private static HashSet<State> closedSet;
	
	private static SpaceSearcher spaceSearcher = null;

	/**
	 * Private constructor called from A* algorithm to initialise our states and closed Set
	 * We made constructor private in order to make SpaceSearcher a Class that does not make objects,
	 * but is used for the implementation of A*
	 */
	private SpaceSearcher() {
		states = new ArrayList<>();
		closedSet = new HashSet<>();
	}


	/**
	 * A* algorithm implementation
	 * @param initialState our initial state with no family member having crossed the bridge
	 * @param time Maximum accepted time for all family members to pass the bridge
	 * @return	Either a terminal state if the algorithm reaches one, or null.
	 */
	public static State AstarClosedSet(State initialState, int time) {
		
		spaceSearcher = new SpaceSearcher();
		
		states.add(initialState);
		
		while(states.size() > 0) {

			State currentState = states.remove(0);
			
			if (currentState.getCost() > time) { // to limit the acceptable cost of the paths.
				continue;
			}
			
			if(currentState.isTerminal()) {
				return currentState;
			}
			
			if(!closedSet.contains(currentState)) {
				closedSet.add(currentState);
				states.addAll(currentState.getChildren());
				Collections.sort(states);
			}
		}
		
		return null;
		
	}
	
}