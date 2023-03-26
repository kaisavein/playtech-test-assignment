package game;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private final int sessionId;
    private final List<GameTurn> turns;

    /**
     * Class that contains all the turns during a game session.
     */
    public GameSession(int sessionId) {
        this.sessionId = sessionId;
        this.turns = new ArrayList<>();
    }

    public int getSessionId() {
        return sessionId;
    }

    public List<GameTurn> getTurns() {
        return turns;
    }

    public void addTurn(GameTurn turn) {
        turns.add(turn);
    }

    public GameTurn findFirstFaultyTurn() {
        for (GameTurn turn : turns) {
            if (!turn.isLegal()) {
                return turn;
            }
        }
        return null;
    }
}
