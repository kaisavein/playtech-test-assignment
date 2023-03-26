package game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Main {

    private static final String INPUT_FILE = "src/resources/game_data.txt";
    private static final String OUTPUT_FILE = "analyzer_results.txt";

    /**
     * Main method, where game data is read in, analysed and faulty moves are written to file.
     */
    public static void main(String[] args) {
        try {
            Map<Integer, GameSession> gameSessions = readGameSessions(INPUT_FILE);
            List<String> faultyMoves = analyzeGameSessions(gameSessions);
            writeFaultyMoves(faultyMoves, OUTPUT_FILE);
        } catch (IOException e) {
            System.err.println("Error reading/writing file: " + e.getMessage());
        }
    }

    /**
     * Method that reads input and creates a GameTurn object out of each line.
     * GameTurns are added to a GameSession object.
     * Returns a map, where the key is session's ID and value is the GameSession object.
     */
    private static Map<Integer, GameSession> readGameSessions(String filename) throws IOException {
        Map<Integer, GameSession> sessionMap = new HashMap<>(); // key: session id, value: session object

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            GameSession currentGame = null;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");

                if (tokens.length != 6 || tokens[0].strip().length() < 1) { // to skip lines with errors
                    continue;
                }

                long timestamp = Long.parseLong(tokens[0]);
                int sessionId = Integer.parseInt(tokens[1]);
                int playerId = Integer.parseInt(tokens[2]);
                String action = tokens[3];
                String dealerHand = tokens[4];
                String playerHand = tokens[5];

                if (currentGame == null || currentGame.getSessionId() != sessionId) {
                    if (sessionMap.containsKey(sessionId)) { // if the session already exists
                        currentGame = sessionMap.get(sessionId);
                    } else {
                        currentGame = new GameSession(sessionId);
                        sessionMap.put(currentGame.getSessionId(), currentGame);
                    }
                }
                currentGame.addTurn(new GameTurn(timestamp, sessionId, playerId, action, dealerHand, playerHand));
            }
        }
        return sessionMap;
    }

    /**
     * Sort GameTurns by timestamp as the logs are not sequential.
     */
    private static void sortGameTurns(Map<Integer, GameSession> gameSessions) {
        for (Integer id : gameSessions.keySet()) {
            gameSessions.get(id).getTurns().sort(Comparator.comparingLong(GameTurn::getTimestamp));
        }
    }

    /**
     * Analyse all GameSessions and collect the first faulty move (if there is one) from each of them.
     */
    private static List<String> analyzeGameSessions(Map<Integer, GameSession> sessionMap) {
        sortGameTurns(sessionMap);

        List<String> faultyMoves = new ArrayList<>();
        List<GameSession> gameSessions = sessionMap.values().stream()
                .sorted(Comparator.comparingInt(GameSession::getSessionId)).toList(); // sort sessions by ID

        for (GameSession gameSession : gameSessions) {
            GameTurn firstFaultyTurn = gameSession.findFirstFaultyTurn();
            if (firstFaultyTurn != null) {
                faultyMoves.add(firstFaultyTurn.toString());
            }
        }
        return faultyMoves;
    }

    /**
     * Write all faulty moves to the given file.
     */
    private static void writeFaultyMoves(List<String> faultyMoves, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (String move : faultyMoves) {
                writer.write(move);
                writer.write('\n');
            }
        }
    }
}
