package application;

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
	
	// Other UI
	Label redPieceCount;
	Label blackPieceCount;
	Label turnDisplay;
	
	public CheckerBoard(Group root) {
		super(root);
		pieceSelectedX = -1;
		pieceSelectedY = -1;
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
		if(!movePiece(pieceX,pieceY,targetX,targetY)) {
			if(!jumpPiece(pieceX,pieceY,targetX,targetY)) {
				System.out.println("Invalid Move");
				return false;
			}
		}
		System.out.println("Move: " + pieceX + " " + pieceY + " " + targetX + " " + targetY);
		selectedPiece = null;
		pieceSelectedX = -1;
		pieceSelectedY = -1;
		updateKings();
		if(turn==1)
			turn = 2;
		else
			turn = 1;
		updateBoardUI();
		return true;
	}
	
	private boolean movePiece(int pieceX, int pieceY, int targetX, int targetY) {
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
		return false;
	}
	
	private boolean jumpPiece(int pieceX, int pieceY, int targetX, int targetY) {
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
				
				int pieceSide = board[y][x];
				Button piece = new Button();
				piece.setShape(new Circle(5));
				piece.setPrefSize(squareLength/11, squareLength/11);
				piece.setOnAction(
						e -> {
							if(turn!=pieceSide)
								return;
							
							if(selectedPiece==piece) {
								if(selectedPiece.getId().equals("redPieceSelected")) 
									selectedPiece.setId("redPiece");
								else if(selectedPiece.getId().equals("blackPieceSelected")) 
									selectedPiece.setId("blackPiece");
								else if(selectedPiece.getId().equals("redKingSelected")) 
									selectedPiece.setId("redKing");
								else if(selectedPiece.getId().equals("blackKingSelected")) 
									selectedPiece.setId("blackKing");
								selectedPiece = null;
								pieceSelectedX = -1;
								pieceSelectedY = -1;
							}
							else {
								if(selectedPiece!=null) {
									if(selectedPiece.getId().equals("redPieceSelected")) 
										selectedPiece.setId("redPiece");
									else if(selectedPiece.getId().equals("blackPieceSelected")) 
										selectedPiece.setId("blackPiece");
									else if(selectedPiece.getId().equals("redKingSelected")) 
										selectedPiece.setId("redKing");
									else if(selectedPiece.getId().equals("blackKingSelected")) 
										selectedPiece.setId("blackKing");
								}
								selectedPiece = piece;
								pieceSelectedX = x;
								pieceSelectedY = y;
								if(selectedPiece.getId().equals("redPiece")) 
									selectedPiece.setId("redPieceSelected");
								else if(selectedPiece.getId().equals("blackPiece")) 
									selectedPiece.setId("blackPieceSelected");
								else if(selectedPiece.getId().equals("redKing")) 
									selectedPiece.setId("redKingSelected");
								else if(selectedPiece.getId().equals("blackKing")) 
									selectedPiece.setId("blackKingSelected");
							}
						}
				);
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
}
