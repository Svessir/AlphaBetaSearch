package breakthrough;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.sun.org.apache.xpath.internal.WhitespaceStrippingElementMatcher;

/**
 * This type of state is an attempt to save memory and speed up state generation
 * This state accepts a pawn board and actions moves(actions) can be on applied on it. 
 * The state can then be rewound to get the previous state. The board and pawns used
 * are only generated once.
 * Generating the successor state takes constant time as well as rewinding it.
 * @author Sverrir
 *
 */
public class State {
	
	public class SPawn {
		public boolean isWhite; 	// Is the pawn white or not?
		public boolean isDead;		// True if the pawn is on the dead stack
		public int x;				// The pawn x coordinate on the board
		public int y;				// The pawn y coordinate on the board
		
		public SPawn(boolean isWhite, boolean isDead, int x, int y) {
			this.isWhite = isWhite;
			this.isDead = isDead;
			this.x = x;
			this.y = y;
		}
	}
	
	public SPawn[][] board;
	public boolean isWhiteTurn;
	public boolean isTerminal;
	public ArrayList<SPawn> blackList;
	public ArrayList<SPawn> whiteList;
	public Stack<SPawn> blackDeadStack;
	public Stack<SPawn> whiteDeadStack;
	public Stack<int[]> moveStack;
	public int[][] whiteGrid;
	public int[][] blackGrid;
	public int numberOfWhitesLeft;
	public int numberOfBlacksLeft;
	ArrayList<int[]> legalMoves;
	public boolean isWinningMove = false;

	public State(Pawn[][] board, boolean isWhiteTurn) {
		this.isWhiteTurn = isWhiteTurn;
		whiteDeadStack = new Stack<>();
		blackDeadStack = new Stack<>();
		moveStack = new Stack<>();
		whiteList = new ArrayList<>();
		blackList = new ArrayList<>();
		convertToSpawnBoard(board);
		numberOfWhitesLeft = whiteList.size();
		numberOfBlacksLeft = blackList.size();
		whiteGrid = makeGridWhite(board.length, board[0].length);
		blackGrid = makeGridBlack(board.length, board[0].length);
	}
	
	private void convertToSpawnBoard(Pawn[][] board) {
		this.board = new SPawn[board.length][board[0].length];
		
		for(int i = 1; i < board.length - 1; i++) {
			for(int j = 1; j < board[0].length - 1; j++) {
				if(board[i][j] != null){
					SPawn pawn = board[i][j].equals(Pawn.WHITE) ? new SPawn(true, false, j, i) : new SPawn(false, false, j, i);
					this.board[i][j] = pawn;
					ArrayList<SPawn> list = pawn.isWhite ? whiteList : blackList;
					list.add(pawn);
				}
			}
		}
	}
	
	public ArrayList<int[]> legalMoves() {
		if(legalMoves == null){
			legalMoves = new ArrayList<int[]>();
			ArrayList<SPawn> pawnList = isWhiteTurn ? whiteList : blackList; 	// Get the pawns that belong to the player
			for(SPawn pawn : pawnList) {
				if(!pawn.isDead)
					addPawnMovesToList(pawn, legalMoves);			// If the pawn is not dead then we can include actions for it
			}
		}
		return legalMoves;
	}
	
	private void addPawnMovesToList(SPawn pawn, ArrayList<int[]> moves) {
		int[] move;
		int upDown = pawn.isWhite ? 1 : -1;				// If white pawn moves it moves upwards, else if black pawn it moves downwards
		if((pawn.isWhite && pawn.y + 1 > board.length - 2) || !pawn.isWhite && pawn.y - 1 < 1)
			return;																// If move will move pawn out of board then there is no legal move
		if(board[pawn.y + upDown][pawn.x - 1] != null && board[pawn.y + upDown][pawn.x - 1].isWhite != pawn.isWhite) {
			move = new int[] {pawn.x, pawn.y, pawn.x - 1, pawn.y + upDown};		// Moving the pawn diagonally left if there is an enemy pawn there
			addMoveTolist(moves, move);
		}
		if(board[pawn.y + upDown][pawn.x + 1] != null && board[pawn.y + upDown][pawn.x + 1].isWhite != pawn.isWhite) {
			move = new int[] {pawn.x, pawn.y, pawn.x + 1, pawn.y + upDown};		// Moving the pawn diagonally right if there is an enemy pawn there
			addMoveTolist(moves, move);
		}
		if(board[pawn.y + upDown][pawn.x] == null) {
			move = new int[] {pawn.x, pawn.y, pawn.x, pawn.y + upDown};			// Moving the pawn to the front tile if there is no pawn there
			addMoveTolist(moves, move);
		}
	}
	
	private void addMoveTolist(ArrayList<int[]> moves, int[] move) {
			moves.add(move);
			
	}
	
	public State successorState(int[] move) {
		Stack<SPawn> deadStack = isWhiteTurn ? blackDeadStack : whiteDeadStack;
		SPawn killedPawn = board[move[3]][move[2]];
		SPawn movedPawn = board[move[1]][move[0]];
		legalMoves = null;									// legalMoves for previous state do not apply to the new state
		
		if(killedPawn != null) {
			killedPawn.isDead = true;						// Mark the killed pawn as dead
			if(killedPawn.isWhite) numberOfWhitesLeft--;
			else numberOfBlacksLeft--;
		}
		if(move[3] == 1 || move[3] == board.length - 2) {
			isTerminal = true;// If a pawn is being moved to the bottom row or the top row, then we have a terminal state
			isWinningMove = true;
		}
		deadStack.push(killedPawn);							// Pushing the pawn that is on conquered tile on dead stack (Could be null)
		board[move[3]][move[2]] = movedPawn;  				// Moving the pawn according to the action
		board[move[1]][move[0]] = null; 					// Removing the pawn from it previous tile
		movedPawn.x = move[2];								// Updating the pawn x coordinate
		movedPawn.y = move[3];								// Updating the pawn y coordinate
		moveStack.push(move);								// Store move so we can rewind it later
		isWhiteTurn = !isWhiteTurn;							// Switch player
		return this;
	}
	
	public State rewindState() {
		// Get the tile that was moved to as it were before move
		SPawn killedPawn = isWhiteTurn ? whiteDeadStack.pop() : blackDeadStack.pop();
		int[] move = moveStack.pop();						// Pop the move of the stack
		SPawn movedPawn = board[move[3]][move[2]];			// Get the pawn that was moved with the action
		legalMoves = null;									// Legal moves of the successor state do not apply to the parent state
		
		if(killedPawn != null) {
			killedPawn.isDead = false;						// Mark the killed pawn as alive
			if(killedPawn.isWhite) numberOfWhitesLeft++;
			else numberOfBlacksLeft++;
		}
		if(isTerminal)
			isTerminal = false;								// If the state was a terminal then the previous state should not be terminal
		
		board[move[1]][move[0]] = movedPawn; 				// Undoing the move
		board[move[3]][move[2]] = killedPawn;				// Restore the killed pawn
		movedPawn.x = move[0];								// Updating the pawn x coordinate
		movedPawn.y = move[1];								// Updating the pawn y coordinate
		isWhiteTurn = !isWhiteTurn;							// Switch player
		return this;
	}
	
	public boolean isTerminalState() {
		if(!isTerminal) {									// If there are no pawns in a terminal position
			if(legalMoves == null)
				legalMoves();							// Get the legalMoves
			return legalMoves.isEmpty();					// If there are no legal moves then we have a terminal state
		}
		return true;
	}
	
	public int eval() {
		if(isWinningMove) {
			isWinningMove = false;
			return Integer.MAX_VALUE;
		}


		int white = 0;
		int black = 0;
		for(SPawn p : whiteList){
			if(!p.isDead){
				white += whiteGrid[p.x][p.y];
			}
		}
		for(SPawn p : blackList){
			if(!p.isDead){
				black += blackGrid[p.x][p.y];
			}
		}
		int wbSize = whiteList.size() - blackList.size();
		if(wbSize < 0) {
			wbSize *= -10;
			black += wbSize;
		}
		else{
			wbSize *= 10;
			white += wbSize;
		}

		if(isWhiteTurn){
			return white - black;
		}
		return black - white;
	}
	
	public Pawn[][] getPawnBoard() {
		Pawn[][] pBoard = new Pawn[board.length][board[0].length];
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[0].length; j++) {
				if(board[i][j] != null)
					pBoard[i][j] = board[i][j].isWhite ? Pawn.WHITE : Pawn.BLACK;
				else
					pBoard[i][j] = null;
			}
		}
		return pBoard;
	}
	public static int[][] makeGridWhite(int x, int y) {
		int boardWidth = x;
		int boardLength = y;
		int count = 0;
		int pointBoard[][] = new int[boardLength][boardWidth];
		for(int i = 0; i < boardLength; i++){
			for(int j = 0; j < boardWidth; j++) {
				if (i == 0) {
					if (j == 0 || j == (boardWidth-1)){
						pointBoard[i][j] = 5;
						continue;
					}
					pointBoard[i][j] = 15;
					continue;
				}
				if (i == 1) {
					if(j == 0 || j == (boardWidth-1)){
						pointBoard[i][j] = 2;
						continue;
					}
					pointBoard[i][j] = 3;
					continue;
				}
				if (j == 0 || j == (boardWidth-1)){
					pointBoard[i][j] = pointBoard[i-1][j] + 2 + count;
					continue;
				}
				pointBoard[i][j] = pointBoard[i-1][j] + 3 + count;
			}
			if(i > 1) {
				count++;
			}
		}
		return pointBoard;
	}
	public static int[][] makeGridBlack(int x, int y) {
		int boardWidth = x;
		int boardLength = y;
		int count = 0;
		int pointBoard[][] = new int[boardLength][boardWidth];
		for(int i = boardLength - 1 ; i >= 0; i--){
			for(int j = boardWidth - 1 ; j >= 0; j--) {
				if (i == boardLength - 1) {
					if (j == 0 || j == (boardWidth-1)){
						pointBoard[i][j] = 5;
						continue;
					}
					pointBoard[i][j] = 15;
					continue;
				}
				if (i == boardLength - 2) {
					if(j == 0 || j == (boardWidth-1)){
						pointBoard[i][j] = 2;
						continue;
					}
					pointBoard[i][j] = 3;
					continue;
				}
				if (j == 0 || j == (boardWidth-1)){
					pointBoard[i][j] = pointBoard[i+1][j] + 2 + count;
					continue;
				}
				pointBoard[i][j] = pointBoard[i+1][j] + 3 + count;
			}
			if(i < boardLength - 2) {
				count++;
			}
		}
		return pointBoard;
	}
	
	public static void main(String[] args) {
		Pawn[][] pawnBoard = new Pawn[][]
		{
			{null, null, null, null, null},
			{null, Pawn.WHITE, Pawn.WHITE, Pawn.WHITE, null},
			{null, Pawn.WHITE, Pawn.WHITE, Pawn.WHITE, null},
			{null, Pawn.BLACK, Pawn.BLACK, Pawn.BLACK, null},
			{null, Pawn.BLACK, Pawn.BLACK, Pawn.BLACK, null},
			{null, null, null, null, null}
		};
		
		State state = new State(pawnBoard, true);
		int count = 0;
		while(!state.isTerminalState()) {
			ArrayList<int[]> moves = (ArrayList<int[]>) state.legalMoves();
			state.successorState(moves.get(0));
			count++;
		}
		
		for(Pawn[] pl : state.getPawnBoard()) {
			for(Pawn p : pl) {
				if(p != null) {
					if(p.equals(Pawn.WHITE))
						System.out.print(" WHITE ");
					else
						System.out.print(" BLACK ");
				}
				else
					System.out.print(" NULL ");
			}
			System.out.println();
		}
		System.out.println();
		for(int i = count; i > 0; i--)
			state.rewindState();
		System.out.println();
		for(Pawn[] pl : state.getPawnBoard()) {
			for(Pawn p : pl) {
				if(p != null) {
					if(p.equals(Pawn.WHITE))
						System.out.print(" WHITE ");
					else
						System.out.print(" BLACK ");
				}
				else
					System.out.print(" NULL ");
			}
			System.out.println();
		}
	}
}
