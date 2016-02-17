
public class AlphaBetaAgent implements Agent {
	
	private String role; // the name of this agent's role (white or BLACK)
	private int playclock; // this is how much time (in seconds) we have before nextAction needs to return a move
	private boolean myTurn; // whether it is this agent's turn or not
	private int width, height; // dimensions of the board
	
	@Override
	public void init(String role, int width, int height, int playclock) {
		this.role = role;
		this.playclock = playclock;
		myTurn = !role.equals("white");
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String nextAction(int[] lastmove) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
