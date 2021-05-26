package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;

	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();

	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		check = false;
		initialSetup();
	}

	public int getTurn() {
		return this.turn;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean getCheck() {
		return check;
	}

	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}

		return mat;
	}

	public boolean[][] possibleMoves(ChessPosition positionCP) {
		Position position = positionCP.toPosition();
		validateOriginPosition(position);

		return board.piece(position).possibleMoves();
	}

	public ChessPiece performMove(ChessPosition originCP, ChessPosition targetCP) {
		Position origin = originCP.toPosition();
		Position target = targetCP.toPosition();

		validateOriginPosition(origin);
		validateTargetPosition(origin, target);
		Piece capturedPiece = makeMove(origin, target);

		if (testCheck(currentPlayer)) {
			undoMove(origin, target, capturedPiece);
			throw new ChessException("You can't check yourself");
		}

		check = (testCheck(opponent(currentPlayer))) ? true : false;

		nextTurn();

		return (ChessPiece) capturedPiece;
	}

	private Piece makeMove(Position origin, Position target) {
		Piece p = board.removePiece(origin);
		Piece captured = board.removePiece(target);

		board.placePiece(p, target);

		if (captured != null) {
			piecesOnTheBoard.remove(captured);
			capturedPieces.add(captured);
		}

		return captured;
	}

	private void undoMove(Position origin, Position target, Piece capturedPiece) {

		Piece p = board.removePiece(target);
		board.placePiece(p, origin);

		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
	}

	private void validateOriginPosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece in such position");
		}

		if (((ChessPiece)board.piece(position)).getColor() != currentPlayer) throw new ChessException("Such piece is not yours");

		if (!board.piece(position).isThereAnyPossibleMove()) throw new ChessException("There is no possible move for this piece");
	}

	private void validateTargetPosition(Position origin ,Position target) {
		if (!board.piece(origin).possibleMove(target)) throw new ChessException("Chosen piece can't move to such position");
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}

	private void nextTurn() {
		turn ++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	private Color opponent(Color color) {
		return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());

		for (Piece p : list) {
			if (p instanceof King) {
				return (ChessPiece) p;
			}
		} throw new IllegalStateException("There is no " + color + "king in the game");
	}

	private boolean testCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition();
		List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());
		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()] == true) {
				return true;
			}
		}
		return  false;
	}

	private void initialSetup() {
		placeNewPiece('c', 1, new Rook(board, Color.WHITE));
		placeNewPiece('c', 2, new Rook(board, Color.WHITE));
		placeNewPiece('d', 2, new Rook(board, Color.WHITE));
		placeNewPiece('e', 2, new Rook(board, Color.WHITE));
		placeNewPiece('e', 1, new Rook(board, Color.WHITE));
		placeNewPiece('d', 1, new King(board, Color.WHITE));

		placeNewPiece('c', 7, new Rook(board, Color.BLACK));
		placeNewPiece('c', 8, new Rook(board, Color.BLACK));
		placeNewPiece('d', 7, new Rook(board, Color.BLACK));
		placeNewPiece('e', 7, new Rook(board, Color.BLACK));
		placeNewPiece('e', 8, new Rook(board, Color.BLACK));
		placeNewPiece('d', 8, new King(board, Color.BLACK));
	}
}
