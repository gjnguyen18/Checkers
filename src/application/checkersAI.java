package application;

import java.util.ArrayList;

public class checkersAI {

	// side: 1 = red, 2 = black
	public int[] makeMove(int[][] board, int side) {
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
	
	private ArrayList<int[]> possibleRedMoves(int[][] board) {
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
					//jumps
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
	
	private ArrayList<int[]> possibleBlackMoves(int[][] board) {
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
					//jumps
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
	
	private void findJumpLines(int[][] board, int[] jump, ArrayList<int[]> possibilities) {
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
	
	private ArrayList<int[]> possibleJumps(int[][] board, int x, int y) {
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
	
	private void move(int[][] board, int pieceX, int pieceY, int targetX, int targetY) {
		if(Math.abs(pieceX-targetX)==1 && Math.abs(pieceY-targetY)==1) {
			board[targetY][targetX] = board[pieceY][pieceX];
			board[pieceY][pieceX] = 0;
		}
		else if(Math.abs(pieceX-targetX)==2 && Math.abs(pieceY-targetY)==2) {
			board[targetY][targetX] = board[pieceY][pieceX];
			board[pieceY][pieceX] = 0;
			board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)] = 0;
		}
	}
	
//	public int[] findBestMove(int[][] board, int maxDepth, int side) {
//		int[] bestMove = new int[4];
//		int[][] bestBoardState = cloneBoard(board);
//		int totalPossibleBoards = 0;
//		ArrayList<int[]> moves = side==1? possibleRedMoves(board):possibleBlackMoves(board);
//		int turn = side==1? 1:-1;
//		for(int[] m:moves) { 
//			int[][] newBoard = cloneBoard(board);
//			for(int i=0;i<m.length;i+=4) {
//				move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
//			}
//			ArrayList<int[][]> possibleBoards = new ArrayList<int[][]>();
//			findAllPossibleMoves(newBoard, 0, maxDepth, turn ,possibleBoards);
//			for(int[][] b:possibleBoards) {
//				if(compareBoards(b,bestBoardState,side)>0) {
//					bestBoardState = b;
//					bestMove = m;
//				}
//			}
//			totalPossibleBoards+=possibleBoards.size();
//		}
//		System.out.println("Total Possible States of Depth " + maxDepth + ": " + totalPossibleBoards);
//		return bestMove;
//	}
	
	private int compareBoards(int[][] board1, int[][] board2, int side) {

		int b1Value = numberOfRed(board1)-numberOfBlack(board1);
		int b2Value = numberOfRed(board2)-numberOfBlack(board2);
		
		if(side==1) {
			return b1Value-b2Value;
		}
		else {
			b1Value = -1*b1Value;
			b2Value = -1*b2Value;
			return b1Value-b2Value;
		}
	}
	
	private int boardScore(int[][] board) {
		return numberOfRed(board)-numberOfBlack(board);
	}
	
//	private void findAllPossibleMoves(int[][] board, int depth, int maxDepth, int turn, ArrayList<int[][]> possibilities) {
//		if(depth>=maxDepth) {
//			possibilities.add(board);
//			return;
//		}
//		ArrayList<int[]> moves = turn==1? possibleRedMoves(board):possibleBlackMoves(board);
//		for(int[] m:moves) { 
//			int[][] newBoard = cloneBoard(board);
//			for(int i=0;i<m.length;i+=4) {
//				move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
//			}
//			findAllPossibleMoves(newBoard, depth+1, maxDepth, turn*-1,possibilities);
//		}
//	} 
	
	private void findAllPossibleMoves(int[][] board, int depth, int maxDepth, int turn, Node curr) {
		if(depth>=maxDepth) {
			curr.board = board;
			return;
		}
		ArrayList<int[]> moves = turn==1? possibleRedMoves(board):possibleBlackMoves(board);
		for(int[] m:moves) { 
			Node n = new Node();
			curr.children.add(n);
			int[][] newBoard = cloneBoard(board);
			for(int i=0;i<m.length;i+=4) {
				move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
			}
			findAllPossibleMoves(newBoard, depth+1, maxDepth, turn*-1,n);
		}
	} 
	
//	private int[][] minimax(int[][] board, int depth, boolean maximizing) {
//		if(depth==0 || numberOfRed(board)==0 || numberOfBlack(board)==0) {
//			return board;
//		}
//		else if(maximizing) {
//			int maxEval = -100000;
//			int[][] bestBoard = new int[8][8];
//			ArrayList<int[]> moves = possibleRedMoves(board);
//			for(int[] m:moves) {
//				int[][] newBoard = cloneBoard(board);
//				for(int i=0;i<m.length;i+=4) {
//					move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
//				}
//				int[][] lowerBoard = minimax(newBoard,depth-1,!maximizing);
//				if(boardScore(lowerBoard)>maxEval) {
//					maxEval = boardScore(lowerBoard);
//					bestBoard = lowerBoard;
//				}
//			}
//			return bestBoard;
//		}
//		else {
//			int minEval = 100000;
//			int[][] bestBoard = new int[8][8];
//			ArrayList<int[]> moves = possibleBlackMoves(board);
//			for(int[] m:moves) {
//				int[][] newBoard = cloneBoard(board);
//				for(int i=0;i<m.length;i+=4) {
//					move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
//				}
//				int[][] lowerBoard = minimax(newBoard,depth-1,!maximizing);
//				if(boardScore(lowerBoard)<minEval) {
//					minEval = boardScore(lowerBoard);
//					bestBoard = lowerBoard;
//				}
//			}
//			return bestBoard;
//		}
//	}
//	
//	public int[] findBestMove(int[][] board, int maxDepth, int side) {
//		int[] bestMove = new int[4];
//		int minBoardScore = 100000;
//		int[][] bestBoardState = cloneBoard(board);
//		ArrayList<int[]> moves = side==1? possibleRedMoves(board):possibleBlackMoves(board);
//		int turn = side==1? 1:-1;
//		for(int[] m:moves) { 
//			int[][] newBoard = cloneBoard(board);
//			for(int i=0;i<m.length;i+=4) {
//				move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
//			}
//			int[][] possibleBoard = minimax(board,maxDepth,false);
//			if(boardScore(possibleBoard)<minBoardScore) {
//				bestMove = m;
//				minBoardScore = boardScore(possibleBoard);
//			}
//		}
//		return bestMove;
//	}
	
	private int[][] minimax(int[][] board, int depth, boolean maximizing, int alpha, int beta) {
		if(depth==0 || numberOfRed(board)==0 || numberOfBlack(board)==0) {
			return board;
		}
		if(maximizing) {
			int maxEval = -100000;
			int[][] bestBoard = board;
			ArrayList<int[]> moves = possibleRedMoves(board);
			maxLoop:
			for(int[] m:moves) {
				int[][] newBoard = cloneBoard(board);
				for(int i=0;i<m.length;i+=4) {
					move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
				}
				int[][] lowerBoard = minimax(newBoard,depth-1,false,alpha,beta);
				int eval = boardScore(lowerBoard);
				if(eval>maxEval) {
					maxEval = eval;
					bestBoard = lowerBoard;
				}
				alpha = Math.max(alpha, maxEval);
				if(beta<=alpha) {
//					System.out.println("prune");
					break maxLoop;
				}
			}
			return bestBoard;
		}
		else {
			int minEval = 100000;
			int[][] bestBoard = board;
			ArrayList<int[]> moves = possibleBlackMoves(board);
			minLoop:
			for(int[] m:moves) {
				int[][] newBoard = cloneBoard(board);
				for(int i=0;i<m.length;i+=4) {
					move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
				}
				int[][] lowerBoard = minimax(newBoard,depth-1,true,alpha,beta);
				int eval = boardScore(lowerBoard);
				if(eval<minEval) {
					minEval = eval;
					bestBoard = lowerBoard;
				}
				beta = Math.min(beta, minEval);
				if(beta<=alpha) {
//					System.out.println("prune");
					break minLoop;
				}
			}
			return bestBoard;
		}
	}
	
	public int[] findBestMove(int[][] board, int maxDepth, int side) {
		int[] bestMove = new int[4];
		int minBoardScore = 100000;
		int[][] bestBoardState = cloneBoard(board);
		ArrayList<int[]> moves = side==1? possibleRedMoves(board):possibleBlackMoves(board);
		for(int[] m:moves) { 
			int[][] newBoard = cloneBoard(board);
			for(int i=0;i<m.length;i+=4) {
				move(newBoard,m[i],m[i+1],m[i+2],m[i+3]);
			}
			int[][] lowerBoard = minimax(newBoard,maxDepth,true,-100000, 100000);
			int eval = boardScore(lowerBoard);
			if(eval<minBoardScore) {
				bestMove = m;
				minBoardScore = eval;
			}
		}
		return bestMove;
	}
	
	
	private int[][] cloneBoard(int[][] board) {
		int[][] newBoard = new int[8][8];
		for(int i=0;i<8;i++) {
			for(int k=0;k<8;k++) {
				newBoard[i][k] = board[i][k];
			}
		}
		return newBoard;
	}
	
	private class Node {
		
		private Node(int[][] board) {
			this.board = board;
		}
		
		private Node() {}
		
		int[][] board;
		ArrayList<Node> children = new ArrayList<Node>();
	}
}
