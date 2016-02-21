package unitTests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import breakthrough.Pawn;
import breakthrough.State;

public class StateTests {
	
	@Test
	public void convertToSPawnBoardTest(){
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
		
		for(int i = 1; i < state.board.length - 1; i++) {
			for(int j = 1; j < state.board[0].length - 1; j++) {
				if(i < 3)
					assertEquals(state.board[i][j].isWhite, true);
				else
					assertEquals(state.board[i][j].isWhite, false);
			}
		}
		
		assertEquals(6, state.whiteList.length);
		assertEquals(6, state.blackList.length);
		
		for(int i = 0; i < state.blackList.length; i++) {
			assertEquals(state.blackList[i].id, i);
			assertEquals(state.whiteList[i].id, i);
		}
	}
	
	@Test
	public void getWhiteLegalMoveTest() {
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
		
		ArrayList<int[]> moves = (ArrayList<int[]>) state.legalMoves();
		assertArrayEquals(new int[]{1,2,2,3}, moves.get(0));
		assertArrayEquals(new int[]{2,2,1,3}, moves.get(1));
		assertArrayEquals(new int[]{2,2,3,3}, moves.get(2));
		assertArrayEquals(new int[]{3,2,2,3}, moves.get(3));
	}
	
	@Test
	public void getBlackLegalMoveTest() {
		Pawn[][] pawnBoard = new Pawn[][]
		{
			{null, null, null, null, null},
			{null, Pawn.WHITE, Pawn.WHITE, Pawn.WHITE, null},
			{null, Pawn.WHITE, Pawn.WHITE, Pawn.WHITE, null},
			{null, Pawn.BLACK, Pawn.BLACK, Pawn.BLACK, null},
			{null, Pawn.BLACK, Pawn.BLACK, Pawn.BLACK, null},
			{null, null, null, null, null}
		};
		
		State state = new State(pawnBoard, false);
		
		ArrayList<int[]> moves = (ArrayList<int[]>) state.legalMoves();
		assertArrayEquals(new int[]{1,3,2,2}, moves.get(0));
		assertArrayEquals(new int[]{2,3,1,2}, moves.get(1));
		assertArrayEquals(new int[]{2,3,3,2}, moves.get(2));
		assertArrayEquals(new int[]{3,3,2,2}, moves.get(3));
	}
	
	@Test
	public void makeWhiteMoveTest(){
		Pawn[][] pawnBoard = new Pawn[][]
		{
			{null, null, null, null, null},
			{null, Pawn.WHITE, Pawn.WHITE, Pawn.WHITE, null},
			{null, Pawn.WHITE, Pawn.WHITE, Pawn.WHITE, null},
			{null, Pawn.BLACK, Pawn.BLACK, Pawn.BLACK, null},
			{null, Pawn.BLACK, Pawn.BLACK, Pawn.BLACK, null},
			{null, null, null, null, null}
		};
		Pawn[][] expectedBoard = new Pawn[][]
		{
			{null, null, null, null, null},
			{null, Pawn.WHITE, Pawn.WHITE, Pawn.WHITE, null},
			{null, null, 	   Pawn.WHITE, Pawn.WHITE, null},
			{null, Pawn.BLACK, Pawn.WHITE, Pawn.BLACK, null},
			{null, Pawn.BLACK, Pawn.BLACK, Pawn.BLACK, null},
			{null, null, null, null, null}
		};
		
		State state = new State(pawnBoard, true);
		
		ArrayList<int[]> moves = (ArrayList<int[]>) state.legalMoves();
		state.successorState(moves.get(0)); 										// Change state according to this action
		assertArrayEquals(expectedBoard, state.getPawnBoard());	
		Pawn pawn = state.blackDeadStack.peek().isWhite ? Pawn.WHITE : Pawn.BLACK;
		assertEquals(pawnBoard[3][2], pawn);
		assertEquals(moves.get(0), state.moveStack.peek());
		assertEquals(false, state.isWhiteTurn);
	}
	
	@Test
	public void makeRewindTest() {
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
		ArrayList<int[]> moves = (ArrayList<int[]>) state.legalMoves();
		state.successorState(moves.get(0));
		state.rewindState();
		assertArrayEquals(pawnBoard, state.getPawnBoard());
		assertEquals(true, state.blackDeadStack.isEmpty());
		assertEquals(true, state.moveStack.isEmpty());
		assertEquals(true, state.isWhiteTurn);
		
	}

}
