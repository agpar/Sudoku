
import java.util.*;
import java.io.*;

class Sudoku{

	/* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For 
	 * a standard Sudoku puzzle, SIZE is 3 and N is 9. */
	int SIZE, N;
	int recs = 0;

	/* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
	 * not yet been revealed are stored as 0. */
	int Grid[][];

	/* The solve() method removes all the unknown characters ('x') in the Grid
	 * and replace them with the numbers from 1-9 that satisfy the Sudoku puzzle. */

	public void solve(){
		
		LinkedList<Unknown> unList = new LinkedList<Unknown>(); //a list of all Unknown squares

		//Initialize all unknowns in the Grid. Unknowns will attempt to solve
		//themselves if they are obvious (constructor behaviour). If the Grid entry
		//remains 0 after initialization, (no obvious answer found by constructor) the
		//unknown is added to a list for further processing.	
		for (int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(Grid[i][j] == 0){
					Unknown temp = new Unknown(i,j);
					if(Grid[i][j] == 0){
						unList.add(temp);
					}
				}
			}
		}
		if(isSolved()) return;

		//Loops through all the unknowns and finds all obvious ones. If puzzle can
		//be solved without guessing, this will do it.
		System.out.println("Eliminating obvious impossibilities.. ");
		System.out.println("");
		elimPoss(unList, Grid);
		print();
		System.out.println();


		//Recursively tests all possibilities in the puzzle.
		if(!isSolved()){
			unList = orderList(unList);
			System.out.println("Starting recursive possibility search... ");
			System.out.println("");
			recSolve(unList, Grid);
		}
	}
	
	//The Unknown object represents an empty box in the Sudoku puzzle. Each Unknown
	//keep track of is possibilities (based on which numbers are already present in
	//its row/box/col) and it's location on the grid. Some processing is done to attempt
	//to eliminate as many Unknowns as possible without guessing, and once this is done,
	//a full permutation search of all Unknowns and their possibilities is performed to
	//solve the puzzle.
	class Unknown{
		int counter, rcounter, row, col, box;
		int[] poss, rposs;

		//Constructor initializes unknowns with its position and immediately
		//eliminates all known impossibilities, and will update Grid with answer
		//if it is obvious. 
		public Unknown(int r, int c){
			row = r;
			col = c;
			box = -1;
			counter = N;
			poss = new int[N];
			rposs = new int[N];

			//Fills the possibility array.
			for(int k = 0; k < N; k++){
				poss[k] = k + 1;
			}
			
			int x = shallowElim(false, Grid);
			if(x > 0) Grid[row][col] = x;
		}
		
		//Finds the box the Unknown is in which is somehwat tricky as puzzles can be of arbitrary size,
		//so can not simply be solved by a case statement. 
		void findBox(){
			int m;
			for(int i = 0; i < SIZE; i++){
				m = i*SIZE;
				for(int j = 0; j < SIZE; j++){
					for(int k = (i/SIZE)*SIZE; k < ((i/SIZE)*SIZE) + SIZE; k++){
						for(int l = (j/SIZE)*SIZE; l < ((j/SIZE)*SIZE) + SIZE; l++){
							if((row/SIZE) == i  && (col/SIZE)== j){
								box = m;
								return;
							}
						}
					}
				m++;
				}
			}
		}


		/* These three methods scan rows/cols/boxes for ints and eliminate possibilities 
		 * for the unknown.  Each also has a 'recursive' argument, which is used when testing
		 * a permutation in the recursive search (rCounter and rPoss are changed instead
		 * of counter and poss - this is to simplify the eventual full permutation search
		 * by only modifying the 'hypothetical' values when testing a branch.
		 */

		public void rowElim(boolean r, int[][] Grid){
			if(r){
				for (int j = 0; j < N; j++){
					if(Grid[row][j] != 0){
						if(rposs[Grid[row][j] - 1] != 0){
							rposs[Grid[row][j] - 1] = 0;
							rcounter--;
						}
					}
				}
			}
			else{
				for (int j = 0; j < N; j++){
					if(Grid[row][j] != 0){
						if(poss[Grid[row][j] - 1] != 0){
							poss[Grid[row][j] - 1] = 0;
							counter--;
						}
					}
				}
			}
		}

		public void colElim(boolean r, int[][] Grid){
			if(r){
				for (int i = 0; i < N; i++){
					if(Grid[i][col] != 0){
						if(rposs[Grid[i][col] - 1] != 0){
							rposs[Grid[i][col] - 1] = 0;
							rcounter--;
						}
					}
				}
			}
			else{
				for (int i = 0; i < N; i++){
					if(Grid[i][col] != 0){
						if(poss[Grid[i][col] - 1] != 0){
							poss[Grid[i][col] - 1] = 0;
							counter--;
						}
					}
				}
			}
		}

		public void boxElim(boolean r, int[][] Grid){
			if(r){
				for(int i = (row/SIZE)*SIZE; i < ((row/SIZE)*SIZE+ SIZE); i++){
					for(int j = (col/SIZE)*SIZE; j < ((col/SIZE)*SIZE + SIZE); j++){
						if(Grid[i][j] != 0){
							if(rposs[Grid[i][j] - 1] != 0){
								rposs[Grid[i][j] - 1] = 0;
								rcounter--;
							}
						}
					}
				}
			}
			else{
				for(int i = (row/SIZE)*SIZE; i < ((row/SIZE)*SIZE+ SIZE); i++){
					for(int j = (col/SIZE)*SIZE; j < ((col/SIZE)*SIZE + SIZE); j++){
						if(Grid[i][j] != 0){
							if(poss[Grid[i][j] - 1] != 0){
								poss[Grid[i][j] - 1] = 0;
								counter--;
							}
						}
					}
				}
			}
		}

		/* shallowElim will run the three possibility elimination methods aboce
		 * and check after each call if the unknown has been solved. Returns an
		 * int based on the countcheck method.
		 */
		public int shallowElim(boolean r, int[][] grid){
			int x = N;
			if(r){
				rowElim(r, grid);
				x = countCheck(rcounter, rposs);
				if(x == 0) return 0;
				if(x > 0) return x;

				colElim(r, grid);
				x = countCheck(rcounter, rposs);
				if(x == 0) return 0;
				if(x > 0) return x;

				boxElim(r, grid);
				x = countCheck(rcounter, rposs);
				if(x == 0) return 0;

				return x;
			}
			else{
				rowElim(r, grid);
				x = countCheck(counter, poss);
				if(x == 0) return 0;
				if(x > 0) return x;

				colElim(r, grid);
				x = countCheck(counter, poss);
				if(x == 0) return 0;
				if(x > 0) return x;

				boxElim(r, grid);
				x = countCheck(counter, poss);
				if(x == 0) return 0;

				return x;
			}
		}

		/* countCheck
		 * Returns 0 if the unknown has no more possibilities, -1 if there are multiple
		 * possibilities remaining, and the value of the possibility if there remains
		 * exactly 1.
		 * 
		 * IMPORTANT: All ADT management must be handled by caller.
		 */
		public int countCheck(int counter, int[] poss){
			if (counter == 0){
				return 0;
			}
			else if (counter == 1){
				int value = 1;
				for(int i = 0; i < N; i++){
					if(poss[i] != 0){
						value = poss[i];
						break;
					}
				}
				return value;
			}			
			else{
				return -1;
			}
		}
	}


	/* elimPoss
	 * Loops through all Unknowns in list and commands them to eliminate possibilities.
	 * Continues to loop until no more unknowns can be solved outright. This method will solve
	 * puzzles where no guessing is required.
	 */

	int[][] elimPoss(LinkedList<Unknown> unList, int[][] grid){
		boolean foundNew = true;
		while(foundNew){
			foundNew = false;
			Iterator<Unknown> iter = unList.iterator();
			while(iter.hasNext()){
				int x = N;
				Unknown temp = iter.next();
				x = temp.shallowElim(false, grid);
				if(x > 0){
					foundNew = true;
					grid[temp.row][temp.col] = x;
					iter.remove();
				}
				if(x == 0){
					System.out.println("ElimPoss found an impossibility in " + temp.row + "," + temp.col);
					grid[0][0] = -1;
					return grid;
				}
			}
		}
		return grid;
	}

	/* Orders the list in an intelligent way. The Unknown with the least possibilities
	 * is moved to the front, and from there the list is re-ordered so that the unknowns
	 * close together (same box AND same row/col) are consecutive, followed by the unknowns
	 * in only the same box/grid/col. Because the permutation search attempts to find contradictions
	 * while searching, there is a large benefit to processing the Unknowns that are most closely 
	 * related, as the contradictions will appear first in the neighbour unknowns. 
	 */
	public LinkedList<Unknown> orderList(LinkedList<Unknown> unList){
		Iterator<Unknown> iter1 = unList.iterator();
		LinkedList<Unknown> newList = new LinkedList<Unknown>();
		Unknown min = unList.getFirst();
		
		//figure out boxes and move unknown with least possibilites to front.
		while(iter1.hasNext()){
			Unknown temp = iter1.next();
			if(min.counter > temp.counter){
				min = temp;
			}
			temp.findBox();
		}
		unList.remove(min);
		newList.add(min);

		while(!unList.isEmpty()){
			iter1 = unList.iterator();
			Unknown last = newList.getLast();
			boolean foundNew = false;
			while(iter1.hasNext()){
				Unknown temp = iter1.next();
				if((temp.row == last.row && temp.box == last.box)||
						(temp.col == last.col && temp.box == last.box)){
					newList.add(temp);
					iter1.remove();
					foundNew = true;
				}
			}
			if(!foundNew){
				iter1 = unList.iterator();
				while(iter1.hasNext()){
					Unknown temp = iter1.next();
					if((temp.row == newList.getLast().row || temp.col == newList.getLast().col) 
							|| temp.box == newList.getLast().box){
						newList.add(temp);
						iter1.remove();
						foundNew = true;
						break;
					}
				}
			}
			if(!foundNew){
				newList.add(unList.getFirst());
				unList.removeFirst();
			}
		}
		return newList;
	}

	/* recSolve
	 * Recursively searches for the solution to the given grid. Uses the list
	 * of Unknowns, and tests their remaining possibilities, rather than all N numbers.
	 * Each loop tests if the possibility is still valid and if the resulting puzzle is
	 * solvable before traversing. If a puzzle is unsolvable (an Unknown has 0 possibilites),
	 * the function steps back one frame and eliminates the possiblity. This greatly reduces
	 * the size of the permutation search.
	 */
	boolean recSolve(LinkedList<Unknown> unList, int[][]grid){

		if(unList.isEmpty()){
			return true;
		}
		recs++;
		Unknown current = unList.getFirst();
		for(int i = 0; i < N; i++){
			if(current.poss[i] != 0){
				if(isValid(current.poss[i], current.row, current.col, Grid)){
					Grid[current.row][current.col] = current.poss[i];
					unList.removeFirst();
					if(isSolvable(unList)){	
						if(recSolve(unList, grid)){
							return true;
						}
					}		
					unList.addFirst(current);
				}
			}
			Grid[current.row][current.col] = 0; 
		}
		return false;

	}


	/* Scans rows/boxes/cols to make sure the possibility the Unknown holds is still valid
	 * in the current permutation of the grid. isValid() is called before any branch is
	 * traversed in the recursive solution search. 
	 */
	boolean isValid(int val, int row, int col, int[][] grid){
		for(int j = 0; j < N; j ++){
			if(grid[row][j] == val && j != col){
				return false;
			}
		}
		for(int i = 0; i < N; i++){
			if(grid[i][col] == val && i != row){
				return false;
			}
		}
		for(int i = (row/SIZE)*SIZE; i < (((row/SIZE)*SIZE) + SIZE); i ++){
			for(int j = (col/SIZE)*SIZE; j < (((col/SIZE)*SIZE) + SIZE); j ++){
				if(grid[i][j] == val && (i != row && j != col)){
					return false;
				}
			}
		}
		return true;	
	}

	/* Treats the given puzzle like a new puzzle and attempts to solve without making
	 * a guess. If a contradiction can be found, the whole branch can be eliminated from
	 * the search. Implementing this check dramatically reduced the time to solve
	 * the veryHard16x16 grid.
	 */

	boolean isSolvable(LinkedList<Unknown> unList){

		//copy current grid permutation
		int[][] rGrid = new int[N][N];
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				rGrid[i][j] = Grid[i][j];
			}
		}

		//reset all rCounter and rPoss for this test.
		Iterator<Unknown> recSetIter = unList.iterator();
		while(recSetIter.hasNext()){
			Unknown temp = recSetIter.next();
			temp.rcounter = temp.counter;
			temp.rposs = Arrays.copyOf(temp.poss, N);
		}

		boolean foundNew = true;
		while(foundNew){
			Iterator<Unknown> unIter= unList.iterator();
			foundNew = false;
			while(unIter.hasNext()){
				int x = N;
				Unknown temp = unIter.next();
				if(rGrid[temp.row][temp.col] == 0){
					x = temp.shallowElim(true, rGrid);
					if(x > 0){
						foundNew = true;
						rGrid[temp.row][temp.col] = x;
					}
					if(x == 0){
						return false;
					}
				}
			}

		}
		return true;
	}

	//Just a bunch of loops that confirm the puzzle is indeed solved (no bugs).
	// I found myself doing it so often, that it was worth writing.
	boolean isSolved(){
		int[] set = new int[N];
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				set[j] = Grid[i][j];
			}
			Arrays.sort(set);
			for(int j = 0; j < N; j++){
				if(set[j] != j+1){
					return false;
				}
			}
		}
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				set[j] = Grid[j][i];
			}
			Arrays.sort(set);
			for(int j = 0; j < N; j++){
				if(set[j] != j+1){
					return false;
				}
			}
		}
		for(int i = 0; i < SIZE; i++){
			for(int j = 0; j < SIZE; j++){
				int m = 0;
				for(int k = (i/SIZE)*SIZE; k < ((i/SIZE)*SIZE+ SIZE); k++){
					for(int l = (j/SIZE)*SIZE; l < ((j/SIZE)*SIZE + SIZE); l++){
						set[m] = Grid[k][l];
						m++;
					}
				}
				Arrays.sort(set);
				for(int k = 0; k < N; k++){
					if(set[k] != k+1){
						return false;
					}
				}
			}
		}

		return true;
	}

	/*****************************************************************************/
	/*      The functions below here were written by the course instructors      */
	/*****************************************************************************/

	/* Default constructor.  This will initialize all positions to the default 0
	 * value.  Use the read() function to load the Sudoku puzzle from a file or
	 * the standard input. */
	public Sudoku( int size )
	{
		SIZE = size;
		N = size*size;

		Grid = new int[N][N];
		for( int i = 0; i < N; i++ ) 
			for( int j = 0; j < N; j++ ) 
				Grid[i][j] = 0;
	}


	/* readInteger is a helper function for the reading of the input file.  It reads
	 * words until it finds one that represents an integer. For convenience, it will also
	 * recognize the string "x" as equivalent to "0". */
	static int readInteger( InputStream in ) throws Exception
	{
		int result = 0;
		boolean success = false;

		while( !success ) {
			String word = readWord( in );

			try {
				result = Integer.parseInt( word );
				success = true;
			} catch( Exception e ) {
				// Convert 'x' words into 0's
				if( word.compareTo("x") == 0 ) {
					result = 0;
					success = true;
				}
				// Ignore all other words that are not integers
			}
		}

		return result;
	}


	/* readWord is a helper function that reads a word separated by white space. */
	static String readWord( InputStream in ) throws Exception
	{
		StringBuffer result = new StringBuffer();
		int currentChar = in.read();
		String whiteSpace = " \t\r\n";
		// Ignore any leading white space
		while( whiteSpace.indexOf(currentChar) > -1 ) {
			currentChar = in.read();
		}

		// Read all characters until you reach white space
		while( whiteSpace.indexOf(currentChar) == -1 ) {
			result.append( (char) currentChar );
			currentChar = in.read();
		}
		return result.toString();
	}


	/* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
	 * grid is filled in one row at at time, from left to right.  All non-valid
	 * characters are ignored by this function and may be used in the Sudoku file
	 * to increase its legibility. */
	public void read( InputStream in ) throws Exception
	{
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				Grid[i][j] = readInteger( in );
			}
		}
	}


	/* Helper function for the printing of Sudoku puzzle.  This function will print
	 * out text, preceded by enough ' ' characters to make sure that the printint out
	 * takes at least width characters.  */
	void printFixedWidth( String text, int width )
	{
		for( int i = 0; i < width - text.length(); i++ )
			System.out.print( " " );
		System.out.print( text );
	}


	/* The print() function outputs the Sudoku grid to the standard output, using
	 * a bit of extra formatting to make the result clearly readable. */
	public void print()
	{
		// Compute the number of digits necessary to print out each number in the Sudoku puzzle
		int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

		// Create a dashed line to separate the boxes 
		int lineLength = (digits + 1) * N + 2 * SIZE - 3;
		StringBuffer line = new StringBuffer();
		for( int lineInit = 0; lineInit < lineLength; lineInit++ )
			line.append('-');

		// Go through the Grid, printing out its values separated by spaces
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				printFixedWidth( String.valueOf( Grid[i][j] ), digits );
				// Print the vertical lines between boxes 
				if( (j < N-1) && ((j+1) % SIZE == 0) )
					System.out.print( " |" );
				System.out.print( " " );
			}
			System.out.println();

			// Print the horizontal line between boxes
			if( (i < N-1) && ((i+1) % SIZE == 0) )
				System.out.println( line.toString() );
		}
	}


	/* The main function reads in a Sudoku puzzle from the standard input, 
	 * unless a file name is provided as a run-time argument, in which case the
	 * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
	 * outputs the completed puzzle to the standard output. */
	public static void main( String args[] ) throws Exception
	{
		InputStream in;
		if( args.length > 0 ) 
			in = new FileInputStream( args[0] );
		else
			in = System.in;

		// The first number in all Sudoku files must represent the size of the puzzle.  See
		// the example files for the file format.
		int puzzleSize = readInteger( in );
		if( puzzleSize > 100 || puzzleSize < 1 ) {
			System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
			System.exit(-1);
		}

		Sudoku s = new Sudoku( puzzleSize );

		// read the rest of the Sudoku puzzle
		s.read( in );

		// Solve the puzzle.  We don't currently check to verify that the puzzle can be
		// successfully completed.  You may add that check if you want to, but it is not
		// necessary.
		s.solve();

		// Print out the (hopefully completed!) puzzle
		s.print();
		System.out.println("");
		System.out.println(s.isSolved() + " in " + s.recs + " recursions");

	}
}
