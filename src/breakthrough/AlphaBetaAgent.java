package breakthrough;

public class AlphaBetaAgent implements Agent {
	
	private class OutOfTimeException extends RuntimeException {};
	
	private String role; 		// the name of this agent's role (white or BLACK)
	private int playclock; 		// this is how much time (in seconds) we have before nextAction needs to return a move
	private boolean myTurn; 	// whether it is this agent's turn or not
	private int width, height; 	// dimensions of the board
	private Pawn[][] board;		// The game board
	private long startTime;		// The start time of the search
	private boolean isCutOff;
	private int numberOfExpansion;

	@Override
	public void init(String role, int width, int height, int playclock) {
		this.role = role;
		this.playclock = playclock * 1000; 	// Convert to milliseconds
		myTurn = !role.equals("white");
		this.width = width;
		this.height = height;
		initBoard();
	}
	
	private void initBoard() {
		height += 2; width += 2; 				// Padding the board
		board = new Pawn[height][width];
		for(int i = 0; i < board[1].length - 1; i++){
			board[1][i] = Pawn.WHITE;
			board[2][i] = Pawn.WHITE;
			board[height - 2][i] = Pawn.BLACK;
			board[height - 3][i] = Pawn.BLACK;
		}
	}
	
	@Override
	public String nextAction(int[] lastmove) {
		startTime = System.currentTimeMillis();
		if(lastmove != null) {
			board[lastmove[3]][lastmove[2]] = board[lastmove[1]][lastmove[0]];
			board[lastmove[1]][lastmove[0]] = null;
		}
		myTurn = !myTurn;
		if(!myTurn)
			return "NOOP";
		return searchForBestNextAction();
	}
	
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
	
	private String searchForBestNextAction() {
		State state = new State(board, role.equals("white"));
		int depth = 0;
		int[] move = null;
		while(true){
			try {
				isCutOff = false;
				numberOfExpansion = 0;
				move = depthLimitedAlphaBetaSearch(state, depth);
				if(!isCutOff)
					break;
				depth++;
				//System.out.println(numberOfExpansion);
			} catch(OutOfTimeException e) {
				break;
			}
		}
		//System.out.println(depth);
		return "(move " + move[0] + " " + move[1] + " " + move[2] + " " + move[3] + ")";
	}
	
	private int[] depthLimitedAlphaBetaSearch(State state, int depth) {
		if(depth == 0) {
			isCutOff = true;
			return null;
		}
		int[] bestMove = null;
		int bestValue = Integer.MIN_VALUE;
		int alpha = Integer.MIN_VALUE;
		int beta =  Integer.MAX_VALUE;
		for(int[] move : state.legalMoves()) {
			numberOfExpansion++;
			int value = MIN(state.successorState(move), alpha, beta, depth - 1);
			alpha = Math.max(alpha, value);
			if(value > bestValue) {
				bestMove = move;
				bestValue = value;
			}
			state.rewindState();
		}
		return bestMove;
	}
	
	private int MAX(State state, int alpha, int beta, int depth) {
		if(System.currentTimeMillis() - startTime >= playclock)
			throw new OutOfTimeException();
		else if(depth == 0) {
			isCutOff = true;
			return -state.eval();
		}
		else if(state.isTerminalState())
			return -state.eval();
		int value = Integer.MIN_VALUE;
		for(int[] move : state.legalMoves()) {
			numberOfExpansion++;
			value = Math.max(value, MIN(state.successorState(move), alpha, beta, depth - 1));
			state.rewindState();
			if(value >= beta)
				return value;
			alpha = Math.max(alpha, value);
		}
		return value;
	}
	
	private int MIN(State state, int alpha, int beta, int depth) {
		if(System.currentTimeMillis() - startTime >= playclock)
			throw new OutOfTimeException();
		else if(depth == 0) {
			isCutOff = true;
			return state.eval();
		}
		else if(state.isTerminalState())
			return state.eval();
		int value = Integer.MAX_VALUE;
		for(int[] move : state.legalMoves()) {
			numberOfExpansion++;
			value = Math.min(value, MAX(state.successorState(move), alpha, beta, depth - 1));
			state.rewindState();
			if(value <= alpha)
				return value;
			beta = Math.min(beta, value);
		}
		return value;
	}
}
