package application;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Program {
	
	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		ChessMatch match = new ChessMatch();

		while (true) {
			try {
				UI.clearScreen();
				UI.printBoard(match.getPieces());
				System.out.println();
				System.out.print("Source: ");
				ChessPosition origin = UI.readChessPosition(sc);

				boolean[][] possibleMoves = match.possibleMoves(origin);
				UI.clearScreen();
				UI.printBoard(match.getPieces(), possibleMoves);

				System.out.println();
				System.out.print("Target: ");
				ChessPosition target = UI.readChessPosition(sc);

				//ChessPiece capturedPiece = match.performMove(origin, target);
			} catch (ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}
	}
}
