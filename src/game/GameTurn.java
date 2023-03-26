package game;

import java.util.Map;
import java.util.Objects;

public class GameTurn {
    private final long timestamp;
    private final int sessionId;
    private final int playerId;
    private final String action;
    private final String dealerHand;
    private final String playerHand;
    private final static int BUST_THRESHOLD = 21;
    private final static int DEALER_HIT_THRESHOLD = 17;

    /**
     * Class for saving and analysing one game turn.
     */
    public GameTurn(long timestamp, int sessionId, int playerId, String action, String dealerHand, String playerHand) {
        this.timestamp = timestamp;
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.action = action;
        this.dealerHand = dealerHand;
        this.playerHand = playerHand;
    }

    /**
     * Returns whether the turn is legal or not. Checks for faulty situations, otherwise assumes the turn is legal.
     */
    public boolean isLegal() {
        String[] dealerCards = dealerHand.split("-");
        String[] playerCards = playerHand.split("-");
        int dealerHandTotal = calculateHandTotal(dealerCards);
        int playerHandTotal = calculateHandTotal(playerCards);

        if (Objects.equals(action, "P Joined") || Objects.equals(action, "D Redeal")) {
            return isGameStartCorrect(dealerCards, playerCards);
        } else if (isSomeoneBust(dealerHandTotal, playerHandTotal)) { // someone is bust, but no win/loss
            return false;
        } else if (hasSomeoneWon(dealerHandTotal, playerHandTotal)) { // someone is closer to 21, but didn't win
            return false;
        } else if (playerHandTotal >= BUST_THRESHOLD - 1 && Objects.equals(action, "P Hit")) { // player will bust
            return false;
        } else if (hasGameEndedTooQuickly(dealerHandTotal)) { // game has ended too soon
            return false;
        } else if (isLastCardDuplicate(dealerCards, playerCards)) { // newest card is a duplicate
            return false;
        }
        return true;
    }

    /**
     * Calculates the total hand value.
     */
    private static int calculateHandTotal(String[] cards) {
        int total = 0;
        String numbers = "2345678910";
        Map<String, Integer> valueMap = Map.of("J", 10, "Q", 10, "K", 10, "A", 11);
        for (String card : cards) {
            if (Objects.equals(card, "?")) { // skips if dealer's card is hidden
                continue;
            }
            String rank = card.substring(0, 1);
            if (numbers.contains(rank)) { // for numbers 2-10
                total += Integer.parseInt(rank);
            } else {
                total += valueMap.get(rank.toUpperCase()); // for face cards and ace
            }
        }
        return total;
    }

    /**
     * Returns whether the game turn is correct when player has joined. Checks for mistakes, otherwise assumes it's correct.
     */
    private boolean isGameStartCorrect(String[] dealerCards, String[] playerCards) {
        String cardNames = "SHCD"; // in short format

        if (dealerCards.length != 2 || playerCards.length != 2) { // both must have exactly 2 cards
            return false;
        } else if (Objects.equals(dealerCards[0], "?") || !Objects.equals(dealerCards[1], "?")) { // dealer's second card must be hidden, first one mustn't
            return false;
        } else if (dealerCards[1].length() == 3 || playerCards[0].length() == 3 || playerCards[1].length() == 3) { // cards must be of correct length
            return false;
        } else if (!cardNames.contains(dealerCards[1].toUpperCase().substring(1))
                || !cardNames.contains(playerCards[0].toUpperCase().substring(1))
                || !cardNames.contains(playerCards[1].toUpperCase().substring(1))) { // card names must be valid
            return false;
        }
        return true;
    }

    /**
     * Check if someone is already bust, but the player didn't win/lose
     */
    private boolean isSomeoneBust(int dealerHandTotal, int playerHandTotal) {
        return (dealerHandTotal > BUST_THRESHOLD && playerHandTotal <= BUST_THRESHOLD && !Objects.equals(action, "P Win"))
                || (dealerHandTotal <= BUST_THRESHOLD && playerHandTotal > BUST_THRESHOLD && !Objects.equals(action, "P Lose"));
    }

    /**
     * Check who is closer to 21 after dealer is done hitting.
     */
    private boolean hasSomeoneWon(int dealerHandTotal, int playerHandTotal) {
        return (DEALER_HIT_THRESHOLD <= dealerHandTotal && dealerHandTotal <= playerHandTotal && !Objects.equals(action, "P Win"))
                || (DEALER_HIT_THRESHOLD <= dealerHandTotal && dealerHandTotal > playerHandTotal && !Objects.equals(action, "P Lose"));
    }

    /**
     * Check if the game has ended even when dealer's hand total is less than 17 (and player isn't bust).
     */
    private boolean hasGameEndedTooQuickly(int dealerHandTotal) {
        return dealerHandTotal < DEALER_HIT_THRESHOLD && (Objects.equals(action, "P Win") || Objects.equals(action, "P Lose"));
    }

    /**
     * Check if someone's last card is a duplicate (the game is played with one deck of cards).
     */
    private boolean isLastCardDuplicate(String[] dealerCards, String[] playerCards) {
        return playerCards.length != 0 && dealerCards.length != 0 && (playerHand.toUpperCase().contains(dealerCards[dealerCards.length - 1].toUpperCase())
                || dealerHand.toUpperCase().contains(playerCards[playerCards.length -1].toUpperCase()));
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp + "," + sessionId + "," + playerId + "," + action + "," + dealerHand + "," + playerHand;
    }
}
