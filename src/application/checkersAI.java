package application;

public class checkersAI {

	// side: 1 = red, 2 = black
	public int[] makeMove(int[][] board, int side, int turn) {
		int[] move = new int[4];
		return move;
	}
	
	private int numberOfRed(int[][] board) {
		int count = 0;
		for(int i=0;i<8;i++) {
			for(int k=0; k<8; k++) {
				if(board[i][k]%10==1)
					count++;
			}
		}
		return count;
	}
	
	private int numberOfBlack(int[][] board) {
		int count = 0;
		for(int i=0;i<8;i++) {
			for(int k=0; k<8; k++) {
				if(board[i][k]%10==2)
					count++;
			}
		}
		return count;
	}
	
	
}
