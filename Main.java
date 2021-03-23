import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class Main {
	
	public static void main(String[] args) {

		System.out.println("Welcome to the bridge crossing AI interface.");
		Scanner in = new Scanner(System.in);

		while(true) {

			try {
				//readNumberOfPeople
				int number_of_people;
				boolean check = true;
				do {
					if (check)
						System.out.println("How many people are about to cross the bridge?");
					else
						System.out.println("The number of people must be greater than zero! So how many people are about to cross the bridge?");
					number_of_people = Integer.parseInt(in.nextLine());
					check = false;
				} while (number_of_people <= 0);

				//readTimes
				ArrayList<Integer> toCross = new ArrayList<>();
				int value;
				for (int i = 1; i <= number_of_people; i++) {
					boolean check1 = true;
					do {
						if (check1)
							System.out.println("Please give the time that the person number " + i + " will need to cross the bridge:");
						else
							System.out.println("The time for each person must be greater than zero! So please give the time that the person number " + i + " will need to cross the bridge:");
						value = Integer.parseInt(in.nextLine());
						check1 = false;
					} while (value <= 0);
					toCross.add(value);
				}

				//readMaximumTime
				int time;
				boolean check2 = true;
				do {
					if (check2)
						System.out.println("What is the maximum time of an acceptable solution?");
					else
						System.out.println("The maximum time must be greater than zero! So what is the maximum time of an acceptable solution?");
					check2 = false;
					time = Integer.parseInt(in.nextLine());
				} while (time <= 0);

				System.out.println("Initiating the matrix...");
				System.out.println("***********************************************************************************************************************");

				State initialState = new State(toCross);
				State terminalState;

				initialState.print();

				long start = System.currentTimeMillis();
				terminalState = SpaceSearcher.AstarClosedSet(initialState, time);
				long end = System.currentTimeMillis();

				if (terminalState == null) {
					System.out.println("Could not find solution within time.");
				} else {

					State temp = terminalState;
					ArrayList<State> path = new ArrayList<>();
					path.add(terminalState);

					while (temp.getFather() != null) {
						path.add(temp.getFather());
						temp = temp.getFather();
					}

					Collections.reverse(path);

					System.out.println("********************\n\n");
					System.out.printf("Finished in %d steps!\n", path.size() - 1);
					System.out.printf("Time for all members to cross the bridge : %d\n\n", terminalState.getCost());
					System.out.println("********************");

					for (State item : path) {
						item.print();
					}

				}

				System.out.println("A*'s search time with closed set: " + (double) (end - start) / 1000 + " sec.");

				System.out.println("\n\nDo you want to run the program again with different inputs?\n" +
						"Answer -yes- or -no-");
				String answer = in.nextLine();

				if (answer.equals("no")) {
					System.exit(0);
				}
				else if (answer.equals("yes")) {
					continue;
				}
				else {
					throw new Exception("You should answer either -yes- or -no- !");
				}
			}
			catch (NumberFormatException nfe) {
				System.out.println("\n~~ERROR : You must insert a number, try again!~~\n");
			}
			catch (Exception exc) {
				System.exit(0);
			}

		}
		
	}
	
}
