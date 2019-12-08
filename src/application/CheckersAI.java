package application;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckersAI {
	
	private static int[][] minimax(int[][] board, int depth, boolean maximizing, 
			int alpha, int beta, int[] pruneCount, boolean pruneOn) {
		int[] pieceCount = pieceCount(board);
		if(depth==0 || pieceCount[0]+pieceCount[2]==0 || pieceCount[1]+pieceCount[3]==0) {
			pruneCount[1]++;
			return board;
		}
		if(maximizing) {
			int maxEval = -1000000;
			int[][] bestBoard = board;
			ArrayList<int[]> moves = possibleRedMoves(board);
			maxLoop:
			for(int[] m:moves) {
				int[][] newBoard = cloneBoard(board);
				for(int i=0;i<m.length;i+=4) {
					move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
				}
				int[][] lowerBoard = minimax(newBoard,depth-1,false,alpha,beta, pruneCount, pruneOn);
				int eval = boardScore(lowerBoard,depth);
				if(eval>maxEval) {
					maxEval = eval;
					bestBoard = lowerBoard;
				}
				alpha = Math.max(alpha, maxEval);
				if(beta<=alpha && pruneOn) {
					pruneCount[0]++;
					break maxLoop;
				}
			}
			return bestBoard;
		}
		else {
			int minEval = 1000000;
			int[][] bestBoard = board;
			ArrayList<int[]> moves = possibleBlackMoves(board);
			minLoop:
			for(int[] m:moves) {
				int[][] newBoard = cloneBoard(board);
				for(int i=0;i<m.length;i+=4) {
					move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
				}
				int[][] lowerBoard = minimax(newBoard,depth-1,true,alpha,beta, pruneCount, pruneOn);
				int eval = boardScore(lowerBoard,depth);
				if(eval<minEval) {
					minEval = eval;
					bestBoard = lowerBoard;
				}
				beta = Math.min(beta, minEval);
				if(beta<=alpha && pruneOn) {
					pruneCount[0]++;
					break minLoop;
				}
			}
			return bestBoard;
		}
	}
	
	// does not multi thread
	public static int[] findBestMove2(int[][] board, int maxDepth, int side, boolean pruneOn) {
		int[] bestMove = new int[4];
		int minBoardScore = 100000;
		int[] pruneCount = new int[2];
		int[][] bestBoardState = cloneBoard(board);
		ArrayList<int[]> moves = side==1? possibleRedMoves(board):possibleBlackMoves(board);
		for(int[] m:moves) { 
			int[][] newBoard = cloneBoard(board);
			for(int i=0;i<m.length;i+=4) {
				move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
			}
			int[][] lowerBoard = minimax(newBoard,maxDepth,true,-100000, 100000, pruneCount, pruneOn);
			int eval = boardScore(lowerBoard,maxDepth);
			if(eval<minBoardScore) {
				bestMove = m;
				minBoardScore = eval;
			}
			System.out.println("Thread Finished");
		}
		System.out.println("Leafs Visited: " + pruneCount[1]);
		return bestMove;
	}
	
	// multi thread the first set of possible moves
	public static int[] findBestMove(int[][] board, int maxDepth, int side, boolean pruneOn) {
		int[] totalPruneCount = new int[2];
	  ArrayList<int[][]> possibleBoards = new ArrayList<int[][]>();
	  ArrayList<int[]> possibleMoves = new ArrayList<int[]>();
		
		ArrayList<int[]> moves = side==1 ? possibleRedMoves(board):possibleBlackMoves(board);
		int totalTasks = moves.size();
		ExecutorService executor = Executors.newFixedThreadPool(totalTasks);
		CountDownLatch latch = new CountDownLatch(totalTasks);
		
		for(int[] m:moves) { 
			int[][] newBoard = cloneBoard(board);
			for(int i=0;i<m.length;i+=4) {
				move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
			}
			possibleBoards.add(new int[8][8]);
			possibleMoves.add(m);
			int index = possibleBoards.size()-1;
			executor.submit(() -> {
				int[] pruneCount = new int[2];
		    possibleBoards.set(index,minimax(newBoard,maxDepth,true,-1000000, 1000000, pruneCount, pruneOn));
		    totalPruneCount[0] += pruneCount[0];
		    totalPruneCount[1] += pruneCount[1];
		    latch.countDown();
			});		
		}
		
		System.out.println("Thread Pool Calculating");
		try {
			latch.await();
		} catch (InterruptedException e) {}
		System.out.println("Thread Pool Finished");
		
		int minBoardScore = 100000;
		int[] bestMove = new int[4];
		for(int i=0; i<possibleBoards.size(); i++) {
			int eval = boardScore(possibleBoards.get(i),1);
			if(eval<minBoardScore) {
				bestMove = possibleMoves.get(i);
				minBoardScore = eval;
			}
		}

		System.out.println("Alpha-Beta Prunes: " + totalPruneCount[0]);
		System.out.println("Leafs Visited: " + totalPruneCount[1]);
		
		executor.shutdownNow();
		return bestMove;
	}
	
	private static int[] pieceCount(int[][] board) {
		int[] count = new int[4];
		for(int i=0;i<8;i++) {
			for(int k=0; k<8; k++) {
				switch(board[i][k]) {
				case 1:
					count[0]++;
					break;
				case 2:
					count[1]++;
					break;
				case 11:
					count[2]++;
					break;
				case 12:
					count[3]++;
					break;
				}
			}
		}
		return count;
	}
	
	private static ArrayList<int[]> possibleRedMoves(int[][] board) {
		boolean clearedMoves = false;
		ArrayList<int[]> moves = new ArrayList<int[]>();
		ArrayList<int[]> jumps = new ArrayList<int[]>();
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==1) { // normal piece
					//moves
					if(jumps.isEmpty()) {
						if(y-1>=0) {
							if(x>0 && board[y-1][x-1]==0)
								moves.add(new int[] {x,y,x-1,y-1});
							if(x<7 && board[y-1][x+1]==0)
								moves.add(new int[] {x,y,x+1,y-1});
						}
					}
					//jumps
					if(y-2>=0) {
						if(x-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
							jumps.add(new int[] {x,y,x-2,y-2});
						else if(x+2<8 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
							jumps.add(new int[] {x,y,x+2,y-2});
					}
				}
				else if(board[y][x]==11) { // king
					//moves
					if(jumps.isEmpty()) {
						if(x>0 && y>0 && board[y-1][x-1]==0)
							moves.add(new int[] {x,y,x-1,y-1});
						if(x>0 && y<7 && board[y+1][x-1]==0)
							moves.add(new int[] {x,y,x-1,y+1});
						if(x<7 && y>0 && board[y-1][x+1]==0)
							moves.add(new int[] {x,y,x+1,y-1});
						if(x<7 && y<7 && board[y+1][x+1]==0)
							moves.add(new int[] {x,y,x+1,y+1});
					}
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y-2});
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y-2});
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 2 && board[y+2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y+2});
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 2 && board[y+2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y+2});
				}
				if(!jumps.isEmpty() && !clearedMoves) {
					moves.clear();
					clearedMoves = true;
				}
			}
		}
		if(!jumps.isEmpty()) {
			ArrayList<int[]> possibleJumpLines = new ArrayList<int[]>();
			for(int[] j:jumps) {
				findJumpLines(board,j,possibleJumpLines);
			}
			return possibleJumpLines;
		}
		return moves;
	}
	
	private static ArrayList<int[]> possibleRedJumps(int[][] board) {
		ArrayList<int[]> jumps = new ArrayList<int[]>();
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==1) { // normal piece
					//jumps
					if(y-2>=0) {
						if(x-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
							jumps.add(new int[] {x,y,x-2,y-2});
						else if(x+2<8 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
							jumps.add(new int[] {x,y,x+2,y-2});
					}
				}
				else if(board[y][x]==11) { // king
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y-2});
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y-2});
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 2 && board[y+2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y+2});
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 2 && board[y+2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y+2});
				}
			}
		}
		ArrayList<int[]> possibleJumpLines = new ArrayList<int[]>();
		for(int[] j:jumps) {
			findJumpLines(board,j,possibleJumpLines);
		}
		return possibleJumpLines;
	}
	
	private static ArrayList<int[]> possibleBlackMoves(int[][] board) {
		boolean clearedMoves = false;
		ArrayList<int[]> moves = new ArrayList<int[]>();
		ArrayList<int[]> jumps = new ArrayList<int[]>();
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==2) { // normal piece
					//moves
					if(jumps.isEmpty()) {
						if(y+1<8) {
							if(x>0 && board[y+1][x-1]==0)
								moves.add(new int[] {x,y,x-1,y+1});
							if(x<7 && board[y+1][x+1]==0)
								moves.add(new int[] {x,y,x+1,y+1});
						}
					}
					//jumps
					if(y+2<8) {
						if(x-2>=0 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
							jumps.add(new int[] {x,y,x-2,y+2});
						else if(x+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
							jumps.add(new int[] {x,y,x+2,y+2});
					}
				}
				else if(board[y][x]==12) { // king
					//moves
					if(jumps.isEmpty()) {
						if(x>0 && y>0 && board[y-1][x-1]==0)
							moves.add(new int[] {x,y,x-1,y-1});
						if(x>0 && y<7 && board[y+1][x-1]==0)
							moves.add(new int[] {x,y,x-1,y+1});
						if(x<7 && y>0 && board[y-1][x+1]==0)
							moves.add(new int[] {x,y,x+1,y-1});
						if(x<7 && y<7 && board[y+1][x+1]==0)
							moves.add(new int[] {x,y,x+1,y+1});
					}
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 1 && board[y-2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y-2});
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 1 && board[y-2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y-2});
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y+2});
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y+2});
				}
				if(!jumps.isEmpty() && !clearedMoves) {
					moves.clear();
					clearedMoves = true;
				}
			}
		}
		if(!jumps.isEmpty()) {
			ArrayList<int[]> possibleJumpLines = new ArrayList<int[]>();
			for(int[] j:jumps) {
				findJumpLines(board,j,possibleJumpLines);
			}
			return possibleJumpLines;
		}
		return moves;
	}
	
	private static ArrayList<int[]> possibleBlackJumps(int[][] board) {
		ArrayList<int[]> jumps = new ArrayList<int[]>();
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==2) { // normal piece
					//jumps
					if(y+2<8) {
						if(x-2>=0 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
							jumps.add(new int[] {x,y,x-2,y+2});
						else if(x+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
							jumps.add(new int[] {x,y,x+2,y+2});
					}
				}
				else if(board[y][x]==12) { // king
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 1 && board[y-2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y-2});
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 1 && board[y-2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y-2});
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
						jumps.add(new int[] {x,y,x-2,y+2});
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
						jumps.add(new int[] {x,y,x+2,y+2});
				}
			}
		}
		ArrayList<int[]> possibleJumpLines = new ArrayList<int[]>();
		for(int[] j:jumps) {
			findJumpLines(board,j,possibleJumpLines);
		}
		return possibleJumpLines;
	}
	
	private static void findJumpLines(int[][] board, int[] jump, ArrayList<int[]> possibilities) {
		int[][] newBoard = cloneBoard(board);
		move(newBoard,jump[jump.length-4],jump[jump.length-3],jump[jump.length-2],jump[jump.length-1]);
		
		ArrayList<int[]> possibleJumps = possibleJumps(newBoard, jump[jump.length-2], jump[jump.length-1]);
		if(possibleJumps.isEmpty()) {
			possibilities.add(jump);
		}
		else {
			for(int[] j:possibleJumps) {
				int[] multiJump = new int[jump.length+4];
				for(int i=0;i<jump.length;i++) {
					multiJump[i] = jump[i];
				}
				for(int i=0;i<4;i++) {
					multiJump[i+jump.length] = j[i];
				}
				int[][] newerBoard = cloneBoard(newBoard);
				findJumpLines(newerBoard,multiJump,possibilities);
			}
		}
	}
	
	private static ArrayList<int[]> possibleJumps(int[][] board, int x, int y) {
		ArrayList<int[]> jumps = new ArrayList<int[]>();
		switch(board[y][x]) {
		case 1:
			if(y-2>=0) {
				if(x-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
					jumps.add(new int[] {x,y,x-2,y-2});
				else if(x+2<8 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
					jumps.add(new int[] {x,y,x+2,y-2});
			}
			break;
		case 2:
			if(y+2<8) {
				if(x-2>=0 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
					jumps.add(new int[] {x,y,x-2,y+2});
				else if(x+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
					jumps.add(new int[] {x,y,x+2,y+2});
			}
			break;
		case 11:
			if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
				jumps.add(new int[] {x,y,x-2,y-2});
			else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
				jumps.add(new int[] {x,y,x+2,y-2});
			else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 2 && board[y+2][x-2] == 0)
				jumps.add(new int[] {x,y,x-2,y+2});
			else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 2 && board[y+2][x+2] == 0)
				jumps.add(new int[] {x,y,x+2,y+2});
			break;
		case 12:
			if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 1 && board[y-2][x-2] == 0)
				jumps.add(new int[] {x,y,x-2,y-2});
			else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 1 && board[y-2][x+2] == 0)
				jumps.add(new int[] {x,y,x+2,y-2});
			else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
				jumps.add(new int[] {x,y,x-2,y+2});
			else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
				jumps.add(new int[] {x,y,x+2,y+2});
			break;
		default:
			break;
		}
		return jumps;
	}
	
	private static void move(int[][] board, int pieceX, int pieceY, int targetX, int targetY) {
		if(Math.abs(pieceX-targetX)==1 && Math.abs(pieceY-targetY)==1) {
			board[targetY][targetX] = board[pieceY][pieceX];
			board[pieceY][pieceX] = 0;
		}
		else if(Math.abs(pieceX-targetX)==2 && Math.abs(pieceY-targetY)==2) {
			board[targetY][targetX] = board[pieceY][pieceX];
			board[pieceY][pieceX] = 0;
			board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)] = 0;
		}
		//update kings
		for(int i=0;i<8;i++) {
			if(board[0][i]==1) {
				board[0][i]=11;
			}
			if(board[7][i]==2) {
				board[7][i]=12;
			}
		}
	}
	
	/**
	 * Return best score based on board position
	 * @param board - current board
	 * @return score - higher score for better red position
	 */
	private static int boardScore(int[][] board, int depth) {
		int redScore = 0;
		int blackScore = 0;
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				switch(board[y][x]) {
				case 1:
					redScore+=3;
					if((y==3 || y==4) && x>=2 && x<=5) {
						redScore+=1;
					}
					break;
				case 2:
					blackScore+=3;
					if((y==3 || y==4) && x>=2 && x<=5) {
						blackScore+=1;
					}
					break;
				case 11:
					redScore+=5;
					if((y==3 || y==4) && x>=2 && x<=5) {
						redScore+=2;
					}
					break;
				case 12:
					blackScore+=5;
					if((y==3 || y==4) && x>=2 && x<=5) {
						blackScore+=2;
					}
					break;
				}
			}
		}
		if(redScore==0)
			return -1000*(depth);
		if(blackScore==0)
			return 1000*(depth);
		return redScore-blackScore;
	}
	
	private static int[][] cloneBoard(int[][] board) {
		int[][] newBoard = new int[8][8];
		for(int i=0;i<8;i++) {
			for(int k=0;k<8;k++) {
				newBoard[i][k] = board[i][k];
			}
		}
		return newBoard;
	}
}
