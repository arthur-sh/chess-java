package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enPassantVulnerable;
	private ChessPiece promoted;

	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();

	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		check = false;
		checkMate = false;
		initialSetup();
	}

	public int getTurn() {
		return this.turn;
	}

	public Color getCurrentPlayer() { return currentPlayer; }

	public boolean getCheck() { return check; }

	public boolean getCheckMate() { return checkMate; }

	public ChessPiece getEnPassantVulnerable() { return enPassantVulnerable; }

	public ChessPiece getPromoted() { return promoted; }

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

		ChessPiece movedPiece = (ChessPiece) board.piece(target);

		//promotion
		promoted = null;
		if (movedPiece instanceof Pawn) {
			if ((movedPiece.getColor() == Color.WHITE && target.getRow() == 0) || (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)) {
				promoted = (ChessPiece) board.piece(target);
				promoted = replacePromotedPiece("Q");
			}
		}

		if (testCheck(currentPlayer)) {
			undoMove(origin, target, capturedPiece);
			throw new ChessException("You can't check yourself");
		}

		check = (testCheck(opponent(currentPlayer))) ? true : false;

		if (testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		} else nextTurn();;

		//en passant

		if (movedPiece instanceof Pawn && (target.getRow() == origin.getRow() + 2 || target.getRow() == origin.getRow() - 2)) {
			enPassantVulnerable = movedPiece;
		} else enPassantVulnerable = null;

		return (ChessPiece) capturedPiece;
	}

	private Piece makeMove(Position origin, Position target) {
		ChessPiece p = (ChessPiece) board.removePiece(origin);
		p.increaseMoveCount();
		Piece captured = board.removePiece(target);

		board.placePiece(p, target);

		if (captured != null) {
			piecesOnTheBoard.remove(captured);
			capturedPieces.add(captured);
		}

		//castling kingside rook

		if (p instanceof King && target.getColumn() == origin.getColumn() + 2) {
			Position originT = new Position(origin.getRow(), origin.getColumn() + 3);
			Position targetT = new Position(origin.getRow(), origin.getColumn() + 1);

			ChessPiece rook = (ChessPiece) board.removePiece(originT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}

		//castling queenside rook

		if (p instanceof King && target.getColumn() == origin.getColumn() - 2) {
			Position originT = new Position(origin.getRow(), origin.getColumn() - 4);
			Position targetT = new Position(origin.getRow(), origin.getColumn() - 1);

			ChessPiece rook = (ChessPiece) board.removePiece(originT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}

		//en passant

		if (p instanceof Pawn) {
			if (origin.getColumn() != target.getColumn() && captured == null) {
				Position pawnPosition;
				if (p.getColor() == Color.WHITE) { pawnPosition = new Position(target.getRow() + 1, target.getColumn()); } else {
					pawnPosition = new Position(target.getRow() - 1, target.getColumn());
				}
				captured = board.removePiece(pawnPosition);
				capturedPieces.add(captured);
				piecesOnTheBoard.remove(captured);
			}
		}

		return captured;
	}

	public ChessPiece replacePromotedPiece(String type) {
		if (promoted == null) throw new IllegalStateException("There is no piece to be promoted");

		if (!type.equals("B") && !type.equals("Q") && !type.equals("N") && !type.equals("R")) return promoted;

		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.removePiece(pos);
		piecesOnTheBoard.remove(p);

		ChessPiece newPiece = newPiece(type, promoted.getColor());
		board.placePiece(newPiece, pos);
		piecesOnTheBoard.add(newPiece);

		return newPiece;
	}

	private ChessPiece newPiece (String type, Color color) {
		if (type.equals("B")) return new Bishop(board, color);
		if (type.equals("Q")) return new Queen(board, color);
		if (type.equals("N")) return new Knight(board, color);
		return new Rook(board, color);
	}

	private void undoMove(Position origin, Position target, Piece capturedPiece) {

		ChessPiece p = (ChessPiece) board.removePiece(target);
		p.decreaseMoveCount();
		board.placePiece(p, origin);

		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}

		//castling kingside rook

		if (p instanceof King && target.getColumn() == origin.getColumn() + 2) {
			Position originT = new Position(origin.getRow(), origin.getColumn() + 3);
			Position targetT = new Position(origin.getRow(), origin.getColumn() + 1);

			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			board.placePiece(rook, originT);
			rook.decreaseMoveCount();
		}

		//castling queenside rook

		if (p instanceof King && target.getColumn() == origin.getColumn() - 2) {
			Position originT = new Position(origin.getRow(), origin.getColumn() - 4);
			Position targetT = new Position(origin.getRow(), origin.getColumn() - 1);

			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			board.placePiece(rook, originT);
			rook.decreaseMoveCount();
		}

		//en passant

		if (p instanceof Pawn) {
			if (origin.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {

				ChessPiece pawn = (ChessPiece) board.removePiece(target);

				Position pawnPosition;
				if (p.getColor() == Color.WHITE) { pawnPosition = new Position(3, target.getColumn()); } else {
					pawnPosition = new Position(4, target.getColumn());
				}
				board.placePiece(pawn, pawnPosition);
			}
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

	private boolean testCheckMate(Color color) {
		if (!testCheck(color)) return false;

		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).collect(Collectors.toList());

		for (Piece p : list) {
			boolean[][] mat = p.possibleMoves();

			for (int i = 0; i < board.getRows(); i++) {
				for (int j = 0; j < board.getColumns(); j++) {
					if (mat[i][j]) {
						Position origin = ((ChessPiece) p).getChessPosition().toPosition();;
						Position target = new Position(i, j);

						Piece capturedPiece = makeMove(origin, target);

						boolean testCheck = this.testCheck(color);
						undoMove(origin, target, capturedPiece);

						if (!testCheck) {
							return  false;
						}
					}
				}
			}
		}

		return true;
	}

	private void initialSetup() {
		placeNewPiece('a', 1, new Rook(board, Color.WHITE));
		placeNewPiece('b', 1, new Knight(board, Color.WHITE));
		placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('d', 1, new Queen(board, Color.WHITE));
		placeNewPiece('e', 1, new King(board, Color.WHITE, this));
		placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('g', 1, new Knight(board, Color.WHITE));
		placeNewPiece('h', 1, new Rook(board, Color.WHITE));
		placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

		placeNewPiece('a', 8, new Rook(board, Color.BLACK));
		placeNewPiece('b', 8, new Knight(board, Color.BLACK));
		placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('d', 8, new Queen(board, Color.BLACK));
		placeNewPiece('e', 8, new King(board, Color.BLACK, this));
		placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('g', 8, new Knight(board, Color.BLACK));
		placeNewPiece('h', 8, new Rook(board, Color.BLACK));
		placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
	}
}
