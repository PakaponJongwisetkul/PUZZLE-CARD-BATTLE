import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CoreGameLogicTest {

    @Test
    public void playerAttackDealsDamageWithoutShield() {
        Player attacker = new Player("A", 3000);
        Player defender = new Player("B", 3000);

        attacker.attack(defender, 500);

        assertEquals(2500, defender.getHp());
        assertEquals(0, defender.getShield());
    }

    @Test
    public void playerAttackUsesShieldBeforeReducingHp() {
        Player attacker = new Player("A", 3000);
        Player defender = new Player("B", 3000);
        defender.setShield(250);

        attacker.attack(defender, 500);

        assertEquals(0, defender.getShield());
        assertEquals(2750, defender.getHp());
    }

    @Test
    public void playerCanHealAndGainEnergy() {
        Player player = new Player("A", 3000);

        player.heal(250);
        player.gainEnergy(3);

        assertEquals(3250, player.getHp());
        assertEquals(3, player.getEnergy());
    }

    @Test
    public void handTracksCardLimitAndRemoveBehavior() {
        Hand hand = new Hand();

        for (int i = 0; i < 9; i++) {
            hand.addCard(new AttackCard(50));
        }

        assertEquals(8, hand.size());
        assertFalse(hand.isEmpty());
        assertNotNull(hand.removeCard(0));
        assertEquals(7, hand.size());
        assertNull(hand.removeCard(99));
    }

    @Test
    public void deckResetsWithExpectedCardCountAndDrawsAllCards() {
        Deck deck = new Deck();

        assertEquals(10, deck.size());
        assertFalse(deck.isEmpty());

        int count = 0;
        while (!deck.isEmpty()) {
            assertNotNull(deck.drawCard());
            count++;
        }

        assertEquals(10, count);
        assertTrue(deck.isEmpty());
    }

    @Test
    public void attackCardUsesDamageOnTarget() {
        Player attacker = new Player("A", 3000);
        Player defender = new Player("B", 3000);
        Card card = new AttackCard(700);

        card.use(attacker, defender);

        assertEquals(2300, defender.getHp());
    }

    @Test
    public void healCardRestoresHp() {
        Player player = new Player("A", 3000);
        Card card = new HealCard(400);

        card.use(player, new Player("B", 3000));

        assertEquals(3400, player.getHp());
    }

    @Test
    public void energyCardAddsEnergy() {
        Player player = new Player("A", 3000);
        Card card = new EnergyCard(2);

        card.use(player, new Player("B", 3000));

        assertEquals(2, player.getEnergy());
    }

    @Test
    public void shieldCardKeepsHighestShieldValue() {
        Player player = new Player("A", 3000);
        Card card = new ShieldCard(1000);
        player.setShield(400);

        card.use(player, new Player("B", 3000));

        assertEquals(1000, player.getShield());
    }

    @Test
    public void negateCardRemovesShieldAndReducesOpponentEnergy() {
        Player user = new Player("A", 3000);
        Player target = new Player("B", 3000);
        target.setShield(300);
        target.gainEnergy(3);
        Card card = new NegateCard();

        card.use(user, target);

        assertEquals(0, target.getShield());
        assertEquals(2, target.getEnergy());
    }

    @Test
    public void stealCardMovesOneEnergyFromTargetToUser() {
        Player user = new Player("A", 3000);
        Player target = new Player("B", 3000);
        user.gainEnergy(1);
        target.gainEnergy(3);
        Card card = new StealCard();

        card.use(user, target);

        assertEquals(2, user.getEnergy());
        assertEquals(2, target.getEnergy());
    }

    @Test
    public void extraCardGivesOneEnergyBonus() {
        Player user = new Player("A", 3000);
        Card card = new ExtraCard();

        card.use(user, new Player("B", 3000));

        assertEquals(1, user.getEnergy());
    }
}
