package application;

import java.util.Stack;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class CheckersVsBot extends Screen {

	/*
	 * 1 = red
	 * 2 = black
	 * 11 = red king
	 * 12 = black king
	 */
	
	int[][] board;
	GridPane boardUI;
	int boardX = 20;
	int boardY = 20;
	int squareLength = 600;
	Button selectedPiece;
	int pieceSelectedX;
	int pieceSelectedY;
	int turn;
	
	Stack<int[]> moves;
	
	// Other UI
	Label redPieceCount;
	Label blackPieceCount;
	Label turnDisplay;
	Label turnLabel;
	
	boolean justJumped;
	boolean pieceLock;
	
	public final static int numberOfMovesAhead = 12;
	
	public CheckersVsBot(Group root) {
		super(root);
		pieceSelectedX = -1;
		pieceSelectedY = -1;
		justJumped = false;
		pieceLock = false;
		moves = new Stack<int[]>();
		generateBoard();
		generateUI();
		updateBoardUI();
		turn = 1;
	}
	
	private void generateUI() {
		Pane UIBorder = new Pane();
		UIBorder.setLayoutX(640);
		UIBorder.setLayoutY(20);
		UIBorder.setPrefSize(200, 600);
		UIBorder.setId("UIBorder");
		addElement(UIBorder);
		
		turnLabel = new Label("Your Turn");
		BorderPane turnLabelBox = new BorderPane(turnLabel);
		turnLabelBox.setLayoutX(640);
		turnLabelBox.setLayoutY(30);
		turnLabelBox.setPrefSize(200, 100);
		addElement(turnLabelBox);
		
		redPieceCount = new Label("Red Pieces: " + 12);
		BorderPane redPieceCountBox = new BorderPane(redPieceCount);
		redPieceCountBox.setLayoutX(640);
		redPieceCountBox.setLayoutY(130);
		redPieceCountBox.setPrefSize(200, 30);
		addElement(redPieceCountBox);
		
		blackPieceCount = new Label("Black Pieces: " + 12);
		BorderPane blackPieceCountBox = new BorderPane(blackPieceCount);
		blackPieceCountBox.setLayoutX(640);
		blackPieceCountBox.setLayoutY(160);
		blackPieceCountBox.setPrefSize(200, 30);
		addElement(blackPieceCountBox);
	}
	
	private void updateUI() {
		redPieceCount.setText("Red Pieces: " + numberOfRed());
		blackPieceCount.setText("Black Pieces: " + numberOfBlack());
	}
	
	private void generateBoard() {
		board = new int[8][8];
		for(int i=0;i<8;i++) {
			for(int k=0;k<8;k++) {
				if(i<3) {
					if(i%2==0 && k%2==0)
							board[i][k] = 2;
					else if(i%2==1 && k%2==1)
							board[i][k] = 2;
				}
				if(i>4) {
					if(i%2==0 && k%2==0)
							board[i][k] = 1;
					else if(i%2==1 && k%2==1)
							board[i][k] = 1;
				}
			}
		}
	}
	
	private boolean undoMove() {
		if(moves.isEmpty())
			return false;
		else {
			
			try {
	  		Platform.runLater(new Runnable() {
		      @Override
		      public void run() {
		        updateBoardUI();
		      }
		    });
	  		Thread.sleep(500);
			} catch (InterruptedException e) {}
			return true;
		}
	}
	
	private boolean move(int pieceX, int pieceY, int targetX, int targetY) {
		if(pieceX<0 && pieceY<0) {
			return false;
		}
		if(mustTakeAgain()) {
			int x = moves.peek()[2];
			int y = moves.peek()[3];
			if(pieceX!=x || pieceY!=y) {
				System.out.println("Must Jump Again");
				return false;
			}
			if(!jumpPiece(pieceX,pieceY,targetX,targetY)) {
				System.out.println("Invalid Move");
				return false;
			}
		}
		else if(mustTake()) {
			if(attemptMovePiece(pieceX,pieceY,targetX,targetY)) {
				System.out.println("Must Take");
				return false;
			}
			if(!jumpPiece(pieceX,pieceY,targetX,targetY)) {
				System.out.println("Invalid Move");
				return false;
			}
		}
		else {
			if(!movePiece(pieceX,pieceY,targetX,targetY)) {
				System.out.println("Invalid Move");
				return false;
			}
		}
		System.out.println("Move: " + pieceX + " " + pieceY + " " + targetX + " " + targetY);
		moves.push(new int[] {pieceX, pieceY, targetX, targetY});
		updateKings();
		Platform.runLater(new Runnable() {
      @Override
      public void run() {
        updateBoardUI();
      }
    });
		if(numberOfRed()==0 || numberOfRedMoves()==0) {
			System.out.println("Black Wins!");
			Platform.runLater(new Runnable() {
	      @Override
	      public void run() {
	      	turnLabel.setText("Black Wins!");
	      }
	    });
			return true;
		}
		else if(numberOfBlack()==0 || numberOfBlackMoves()==0) {
			System.out.println("Red Wins!");
			Platform.runLater(new Runnable() {
	      @Override
	      public void run() {
	      	turnLabel.setText("Red Wins!");
	      }
	    });
			return true;
		}
		if(!mustTakeAgain()) {
			Platform.runLater(new Runnable() {
	      @Override
	      public void run() {
	        turnLabel.setText("CPU turn");
	      }
	    });     
			selectedPiece = null;
			pieceSelectedX = -1;
			pieceSelectedY = -1;
			if(turn==1) {
				turn = 2;
				System.out.println("Black Moves: "+numberOfBlackMoves());
			}
			else {
				turn = 1;
				System.out.println("Red Moves: "+numberOfRedMoves());
			}
			pieceLock = false;
			if(turn==2) {
				Task<Void> task = new Task<Void>() {
			    @Override 
			    public Void call() {				
			    	long timeStart = System.currentTimeMillis();
			    	//AI MOVE
			    	int[] bestMove = CheckersAI.findBestMove(board, numberOfMovesAhead, 2, true);
			    	for(int i=0;i<bestMove.length;i+=4) {
			    		try {
					  		Platform.runLater(new Runnable() {
						      @Override
						      public void run() {
						        updateBoardUI();
						      }
						    });
					  		Thread.sleep(500);
							} catch (InterruptedException e) {}
			    		move(bestMove[i+0],bestMove[i+1],bestMove[i+2],bestMove[i+3]);
			    	}
			    	System.out.println("Time to compute: " + (System.currentTimeMillis()-timeStart));
						return null;
			    }
				};
				new Thread(task).start();
	  		Platform.runLater(new Runnable() {
		      @Override
		      public void run() {
		        updateBoardUI();
		      }
		    });
			}
			else if(turn==1) {
				Platform.runLater(new Runnable() {
		      @Override
		      public void run() {
		        turnLabel.setText("Your turn");
		      }
		    });     
			}
		}
		else
			pieceLock = true;		
		return true;
	}
	
	private boolean mustTake() {
		for(int x=0;x<8;x++) {
			for(int y=0;y<8;y++) {
				if(board[y][x]%10==turn) {
					if(canTake(x,y)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean canTake(int x, int y) {
		switch(board[y][x]) {
		case 1:
			if(y-2>=0) {
				if(x-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
					return true;
				else if(x+2<8 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
					return true;
			}
			break;
		case 2:
			if(y+2<8) {
				if(x-2>=0 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
					return true;
				else if(x+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
					return true;
			}
			break;
		case 11:
			if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
				return true;
			else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
				return true;
			else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 2 && board[y+2][x-2] == 0)
				return true;
			else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 2 && board[y+2][x+2] == 0)
				return true;
			break;
		case 12:
			if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 1 && board[y-2][x-2] == 0)
				return true;
			else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 1 && board[y-2][x+2] == 0)
				return true;
			else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
				return true;
			else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
				return true;
			break;
		default:
			break;
		}
		return false;
	}
	
	private boolean mustTakeAgain() {
		if(!justJumped)
			return false;
		int x = 0;
		int y = 0;
		if(!moves.isEmpty()) {
			x = moves.peek()[2];
			y = moves.peek()[3];
		}
		else {
			return false;
		}
		switch(board[y][x]) {
		case 1:
			if(y-2>=0) {
				if(x-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
					return true;
				else if(x+2<8 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
					return true;
			}
			break;
		case 2:
			if(y+2<8) {
				if(x-2>=0 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
					return true;
				else if(x+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
					return true;
			}
			break;
		case 11:
			if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
				return true;
			else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
				return true;
			else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 2 && board[y+2][x-2] == 0)
				return true;
			else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 2 && board[y+2][x+2] == 0)
				return true;
			break;
		case 12:
			if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 1 && board[y-2][x-2] == 0)
				return true;
			else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 1 && board[y-2][x+2] == 0)
				return true;
			else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
				return true;
			else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
				return true;
			break;
		default:
			break;
		}
		return false;
	}
	
	private boolean movePiece(int pieceX, int pieceY, int targetX, int targetY) {
		boolean jumpStat = justJumped;
		justJumped = false;
		if(board[pieceY][pieceX] >=11) {
			if(Math.abs(pieceX-targetX)==1 && Math.abs(pieceY-targetY)==1) {
				board[targetY][targetX] = board[pieceY][pieceX];
				board[pieceY][pieceX] = 0;
				return true;
			}
		}
		if(board[pieceY][pieceX] == 1) {
			if(pieceY-targetY==1) {
				if(Math.abs(pieceX-targetX)==1) {
					board[targetY][targetX] = board[pieceY][pieceX];
					board[pieceY][pieceX] = 0;
					return true;
				}
			}
		}
		if(board[pieceY][pieceX] == 2) {
			if(pieceY-targetY==-1) {
				if(Math.abs(pieceX-targetX)==1) {
					board[targetY][targetX] = board[pieceY][pieceX];
					board[pieceY][pieceX] = 0;
					return true;
				}
			}
		}
		justJumped = jumpStat;
		return false;
	}
	
	private boolean attemptMovePiece(int pieceX, int pieceY, int targetX, int targetY) {
		if(board[pieceY][pieceX] >=11) {
			if(board[pieceY][pieceX]%10 == turn) {
				if(Math.abs(pieceX-targetX)==1 && Math.abs(pieceY-targetY)==1) {
					return true;
				}
			}
		}
		if(board[pieceY][pieceX] == 1 && turn == 1) {
			if(pieceY-targetY==1) {
				if(Math.abs(pieceX-targetX)==1) {
					return true;
				}
			}
		}
		if(board[pieceY][pieceX] == 2 && turn == 2) {
			if(pieceY-targetY==-1) {
				if(Math.abs(pieceX-targetX)==1) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean jumpPiece(int pieceX, int pieceY, int targetX, int targetY) {
		justJumped = true;
		if(board[pieceY][pieceX] == 11) {
			if(Math.abs(pieceX-targetX)==2 && Math.abs(pieceY-targetY)==2) {
				if(board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)]%10 == 2) {
					board[pieceY][pieceX] = 0;
					board[targetY][targetX] = 11;
					board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)] = 0;
					return true;
				}
			}
		}
		else if(board[pieceY][pieceX] == 12) {
			if(Math.abs(pieceX-targetX)==2 && Math.abs(pieceY-targetY)==2) {
				if(board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)]%10 == 1) {
					board[pieceY][pieceX] = 0;
					board[targetY][targetX] = 12;
					board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)] = 0;
					return true;
				}
			}
		}
		else if(board[pieceY][pieceX] == 1) {
			if(pieceY-targetY==2) {
				if(Math.abs(pieceX-targetX)==2) {
					if(board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)]%10 == 2) {
						board[pieceY][pieceX] = 0;
						board[targetY][targetX] = 1;
						board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)] = 0;
						return true;
					}
				}
			}
		}
		else if(board[pieceY][pieceX] == 2) {
			if(pieceY-targetY==-2) {
				if(Math.abs(pieceX-targetX)==2) {
					if(board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)]%10 == 1) {
						board[pieceY][pieceX] = 0;
						board[targetY][targetX] = 2;
						board[pieceY+((targetY-pieceY)/2)][pieceX+((targetX-pieceX)/2)] = 0;
						return true;
					}
				}
			}
		}
		justJumped = false;
		System.out.println("jump failed");
		return false;
	}
	
	private void updateKings() {
		for(int i=0;i<8;i++) {
			if(board[0][i]==1) {
				board[0][i]=11;
				System.out.println("promotion");
			}
			if(board[7][i]==2) {
				board[7][i]=12;
				System.out.println("promotion");
			}
		}
	}
	
	private void updateBoardUI() {
		removeElement(boardUI);
		boardUI = new GridPane();
		boardUI.setLayoutX(boardX);
		boardUI.setLayoutY(boardY);
		for(int i=0;i<8;i++) {
			for(int k=0;k<8;k++) {
				int y = k;
				int x = i;
				Pane squareGroup = new Pane();
				Button square = new Button();
				squareGroup.getChildren().add(square);
				square.setOnAction(
						e -> {
							move(pieceSelectedX, pieceSelectedY, x, y);
						}
				);
				square.setPrefSize(squareLength/8, squareLength/8);
				if((i+k)%2==0) {
					square.setId("square1");
				}
				else {
					square.setId("square2");
				}
				
				int pieceSide = board[y][x]%10;
				Button piece = new Button();
				piece.setShape(new Circle(5));
				piece.setPrefSize(squareLength/10, squareLength/10);
				BorderPane centerPiece = new BorderPane(piece);
				centerPiece.setPrefSize(squareLength/8, squareLength/8);
				if(board[y][x] == 1) {
					piece.setId("redPiece");
					squareGroup.getChildren().add(centerPiece);
				}
				else if(board[y][x] == 2) {
					piece.setId("blackPiece");
					squareGroup.getChildren().add(centerPiece);
				}
				else if(board[y][x] == 11) {
					piece.setId("redKing");
					squareGroup.getChildren().add(centerPiece);
				}
				else if(board[y][x] == 12) {
					piece.setId("blackKing");
					squareGroup.getChildren().add(centerPiece);
				}
				piece.setOnAction(
						e -> {
							if(pieceLock)
								return;
							if(turn!=pieceSide)
								return;
							if(pieceSide!=1)
								return;
							
							if(selectedPiece==piece) {
								selectedPiece.setId(selectedPiece.getId().replaceAll("Selected", ""));
								selectedPiece = null;
								pieceSelectedX = -1;
								pieceSelectedY = -1;
							}
							else {
								if(selectedPiece!=null) {
									selectedPiece.setId(selectedPiece.getId().replaceAll("Selected", ""));
								}
								selectedPiece = piece;
								pieceSelectedX = x;
								pieceSelectedY = y;
								selectedPiece.setId(selectedPiece.getId()+"Selected");
							}
						}
				);
				if(mustTakeAgain()) {
					if(moves.peek()[2] == x && moves.peek()[3] == y) {
						selectedPiece = piece;
						pieceSelectedX = x;
						pieceSelectedY = y;
						selectedPiece.setId(selectedPiece.getId()+"Selected");
					}
				}
				boardUI.add(squareGroup, i, k);
			}
		}
		addElement(boardUI);
		updateUI();
	}
	
	private int numberOfRed() {
		int count = 0;
		for(int i=0;i<8;i++) {
			for(int k=0; k<8; k++) {
				if(board[i][k]%10==1)
					count++;
			}
		}
		return count;
	}
	
	private int numberOfBlack() {
		int count = 0;
		for(int i=0;i<8;i++) {
			for(int k=0; k<8; k++) {
				if(board[i][k]%10==2)
					count++;
			}
		}
		return count;
	}
	
	private int numberOfRedMoves() {
		int moveCount = 0;
		int jumpCount = 0;
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==1) { // normal piece
					//moves
					if(jumpCount==0) {
						if(y-1>=0) {
							if(x>0 && board[y-1][x-1]==0)
								moveCount++;
							if(x<7 && board[y-1][x+1]==0)
								moveCount++;
						}
					}
					//jumps
					if(y-2>=0) {
						if(x-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
							jumpCount++;
						else if(x+2<8 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
							jumpCount++;
					}
				}
				else if(board[y][x]==11) { // king
					//jumps
					if(jumpCount==0) {
						if(x>0 && y>0 && board[y-1][x-1]==0)
							moveCount++;
						if(x>0 && y<7 && board[y+1][x-1]==0)
							moveCount++;
						if(x<7 && y>0 && board[y-1][x+1]==0)
							moveCount++;
						if(x<7 && y<7 && board[y+1][x+1]==0)
							moveCount++;
					}
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
						jumpCount++;
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
						jumpCount++;
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 2 && board[y+2][x-2] == 0)
						jumpCount++;
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 2 && board[y+2][x+2] == 0)
						jumpCount++;
				}
			}
		}
		return jumpCount>0 ? jumpCount:moveCount;
	}
	
	private int numberOfBlackMoves() {
		int moveCount = 0;
		int jumpCount = 0;
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==2) { // normal piece
					//moves
					if(jumpCount==0) {
						if(y+1<8) {
							if(x>0 && board[y+1][x-1]==0)
								moveCount++;
							if(x<7 && board[y+1][x+1]==0)
								moveCount++;
						}
					}
					//jumps
					if(y+2<8) {
						if(x-2>=0 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
							jumpCount++;
						else if(x+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
							jumpCount++;
					}
				}
				else if(board[y][x]==12) { // king
					//jumps
					if(jumpCount==0) {
						if(x>0 && y>0 && board[y-1][x-1]==0)
							moveCount++;
						if(x>0 && y<7 && board[y+1][x-1]==0)
							moveCount++;
						if(x<7 && y>0 && board[y-1][x+1]==0)
							moveCount++;
						if(x<7 && y<7 && board[y+1][x+1]==0)
							moveCount++;
					}
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 1 && board[y-2][x-2] == 0)
						jumpCount++;
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 1 && board[y-2][x+2] == 0)
						jumpCount++;
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
						jumpCount++;
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
						jumpCount++;
				}
			}
		}
		return jumpCount>0 ? jumpCount:moveCount;
	}
	
	public int getTurn() {
		return turn;
	}
	
	public int[][] getBoard() {
		return board;
	}
}
