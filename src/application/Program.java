package application;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.*;

public class Program {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		ChessMatch match = new ChessMatch();

		List<ChessPiece> captured = new ArrayList<>();

		while (!match.getCheckMate()) {
			try {
				UI.clearScreen();
				UI.printMatch(match, captured);
				System.out.println();
				System.out.print("Source: ");
				ChessPosition origin = UI.readChessPosition(sc);

				boolean[][] possibleMoves = match.possibleMoves(origin);
				UI.clearScreen();
				UI.printBoard(match.getPieces(), possibleMoves);

				System.out.println();
				System.out.print("Target: ");
				ChessPosition target = UI.readChessPosition(sc);

				ChessPiece capturedPiece = match.performMove(origin, target);

				if (capturedPiece != null) {
					captured.add(capturedPiece);
				}

				if (match.getPromoted() != null) {
					System.out.print("Enter piece type for promotion (B/N/R/Q)");

					String type = sc.nextLine().toUpperCase();
					while (!type.equals("B") && !type.equals("Q") && !type.equals("N") && !type.equals("R")) {
						System.out.println("Invalid value");
						System.out.print("Enter piece type for promotion (B/N/R/Q)");

						type = sc.nextLine().toUpperCase();
					}
					match.replacePromotedPiece(type);
				}

			} catch (ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		} UI.clearScreen();
		UI.printMatch(match, captured);
	}
}
