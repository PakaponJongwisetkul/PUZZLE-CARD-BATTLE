import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GameFlowTest {

    @Test
    public void startGameStartsWithEmptyHands() {
        GameFlow game = new GameFlow();

        game.startGame();

        assertEquals(0, game.getHand(game.getPlayerA()).size());
        assertEquals(0, game.getHand(game.getPlayerB()).size());
        assertEquals(3000, game.getPlayerA().getHp());
        assertEquals(3000, game.getPlayerB().getHp());
    }

    @Test
    public void matchingBoardCardsAddsRewardToCurrentPlayersHand() throws Exception {
        GameFlow game = new GameFlow();
        game.startGame();

        var boardCardTypesField = GameFlow.class.getDeclaredField("boardCardTypes");
        boardCardTypesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<String> cardTypes = (java.util.List<String>) boardCardTypesField.get(game);

        int firstIndex = -1;
        int secondIndex = -1;
        for (int i = 0; i < cardTypes.size(); i++) {
            for (int j = i + 1; j < cardTypes.size(); j++) {
                if (cardTypes.get(i).equals(cardTypes.get(j))) {
                    firstIndex = i;
                    secondIndex = j;
                    break;
                }
            }
            if (firstIndex >= 0) {
                break;
            }
        }

        assertTrue(firstIndex >= 0 && secondIndex >= 0, "Expected at least one duplicate card pair on the board");

        int beforeSize = game.getHand(game.getCurrentPlayer()).size();
        assertTrue(game.revealBoardCard(firstIndex));
        assertTrue(game.revealBoardCard(secondIndex));
        assertEquals(beforeSize + 1, game.getHand(game.getCurrentPlayer()).size());
    }

    @Test
    public void startTurnAddsEnergyAndSwitchesPlayer() {
        GameFlow game = new GameFlow();
        game.startGame();

        Player before = game.getCurrentPlayer();
        game.startTurn();

        assertEquals(2, before.getEnergy());
        assertNotSame(before, game.getCurrentPlayer());
    }
}
