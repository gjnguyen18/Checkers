package application;

import java.util.Stack;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class CheckerBoard extends Screen {

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
	
	checkersAI bot;
	int turn;
	
	Stack<int[]> moves;
	
	// Other UI
	Label redPieceCount;
	Label blackPieceCount;
	Label turnDisplay;
	
	boolean justJumped;
	boolean pieceLock;
	
	public CheckerBoard(Group root) {
		super(root);
		pieceSelectedX = -1;
		pieceSelectedY = -1;
		justJumped = false;
		pieceLock = false;
		moves = new Stack<int[]>();
		generateBoard();
		generateUI();
		updateBoardUI();
		bot = new checkersAI();
		turn = 1;
	}
	
	private void generateUI() {
		Pane UIBorder = new Pane();
		UIBorder.setLayoutX(640);
		UIBorder.setLayoutY(20);
		UIBorder.setPrefSize(200, 600);
		UIBorder.setId("UIBorder");
		addElement(UIBorder);
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
		updateBoardUI();
		if(!mustTakeAgain()) {
			selectedPiece = null;
			pieceSelectedX = -1;
			pieceSelectedY = -1;
			if(turn==1) {
				turn = 2;
				System.out.println("Black Move: "+numberOfBlackMoves());
			}
			else {
				turn = 1;
				System.out.println("Red Moves: "+numberOfRedMoves());
			}
			pieceLock = false;
		}
		else
			pieceLock = true;
		if(numberOfRed()==0 || numberOfRedMoves()==0) {
			System.out.println("Black Wins!");
		}
		else if(numberOfBlack()==0 || numberOfBlackMoves()==0) {
			System.out.println("Red Wins!");
		}
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
		int count = 0;
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==1) { // normal piece
					//moves
					if(y-1>=0) {
						if(x>0 && board[y-1][x-1]==0)
							count++;
						if(x<7 && board[y-1][x+1]==0)
							count++;
					}
					//jumps
					if(y-2>=0) {
						if(x-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
							count++;
						else if(x+2<8 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
							count++;
					}
				}
				else if(board[y][x]==11) { // king
					//jumps
					if(x>0 && y>0 && board[y-1][x-1]==0)
						count++;
					if(x>0 && y<7 && board[y+1][x-1]==0)
						count++;
					if(x<7 && y>0 && board[y-1][x+1]==0)
						count++;
					if(x<7 && y<7 && board[y+1][x+1]==0)
						count++;
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 2 && board[y-2][x-2] == 0)
						count++;
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 2 && board[y-2][x+2] == 0)
						count++;
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 2 && board[y+2][x-2] == 0)
						count++;
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 2 && board[y+2][x+2] == 0)
						count++;
				}
			}
		}
		return count;
	}
	
	private int numberOfBlackMoves() {
		int count = 0;
		for(int x=0;x<8;x++) {
			for(int y=0; y<8; y++) {
				if(board[y][x]==2) { // normal piece
					//moves
					if(y+1<8) {
						if(x>0 && board[y+1][x-1]==0)
							count++;
						if(x<7 && board[y+1][x+1]==0)
							count++;
					}
					//jumps
					if(y+2<8) {
						if(x-2>=0 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
							count++;
						else if(x+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
							count++;
					}
				}
				else if(board[y][x]==12) { // king
					//jumps
					if(x>0 && y>0 && board[y-1][x-1]==0)
						count++;
					if(x>0 && y<7 && board[y+1][x-1]==0)
						count++;
					if(x<7 && y>0 && board[y-1][x+1]==0)
						count++;
					if(x<7 && y<7 && board[y+1][x+1]==0)
						count++;
					//jumps
					if(x-2>=0 && y-2>=0 && board[y-1][x-1]%10 == 1 && board[y-2][x-2] == 0)
						count++;
					else if(x+2<8 && y-2>=0 && board[y-1][x+1]%10 == 1 && board[y-2][x+2] == 0)
						count++;
					else if(x-2>=0 && y+2<8 && board[y+1][x-1]%10 == 1 && board[y+2][x-2] == 0)
						count++;
					else if(x+2<8 && y+2<8 && board[y+1][x+1]%10 == 1 && board[y+2][x+2] == 0)
						count++;
				}
			}
		}
		return count;
	}
}
