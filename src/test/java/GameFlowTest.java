import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ==========================================
 * GameFlowTest — ครอบคลุมกติกาเกมทั้งหมด
 * ==========================================
 * ส่วนที่ 1  : การตั้งค่าเริ่มต้น
 * ส่วนที่ 2  : เริ่มเทิร์น (+2 Energy)
 * ส่วนที่ 3  : เปิดการ์ดกองกลาง (Board Reveal / Match)
 * ส่วนที่ 4  : ใช้งานการ์ดในมือ (Card Effects)
 * ส่วนที่ 5  : ระบบ Chain Effect
 * ส่วนที่ 6  : จบเทิร์น (End Turn)
 * ส่วนที่ 7  : เงื่อนไขการชนะ
 */
public class GameFlowTest {

    private GameFlow game;

    @BeforeEach
    void setUp() {
        game = new GameFlow();
        game.startGame();
    }

    // =========================================================
    // ส่วนที่ 1 : การตั้งค่าเริ่มต้น
    // =========================================================

    /** HP เริ่มต้น 3000 ต่อฝ่าย */
    @Test
    void initialHpIs3000PerPlayer() {
        assertEquals(3000, game.getPlayerA().getHp(), "Player A HP ต้องเริ่มต้น 3000");
        assertEquals(3000, game.getPlayerB().getHp(), "Player B HP ต้องเริ่มต้น 3000");
    }

    /** Energy เริ่มต้น 0 ก่อน startTurn (startGame เรียก startTurn ทำให้ Player A มี 2) */
    @Test
    void initialEnergyAfterStartGame() {
        // startGame → startTurn → Player A +2 Energy
        // Player B ยังไม่ได้เล่น Energy = 0
        assertEquals(2, game.getPlayerA().getEnergy(), "Player A ควรมี Energy 2 หลัง startGame");
        assertEquals(0, game.getPlayerB().getEnergy(), "Player B ควรมี Energy 0 ก่อนเทิร์นแรก");
    }

    /** มือผู้เล่นเริ่มต้นว่างเปล่า */
    @Test
    void initialHandsAreEmpty() {
        assertEquals(0, game.getHand(game.getPlayerA()).size(), "มือ Player A ต้องว่างเปล่า");
        assertEquals(0, game.getHand(game.getPlayerB()).size(), "มือ Player B ต้องว่างเปล่า");
    }

    /** Board มี 16 ใบ (4×4) ทุกใบคว่ำหน้า */
    @Test
    void initialBoardHas16FaceDownCards() {
        assertEquals(16, game.getBoardSize(), "Board ต้องมี 16 ช่อง");
        for (int i = 0; i < game.getBoardSize(); i++) {
            assertFalse(game.isBoardFaceUp(i), "การ์ดทุกใบต้องคว่ำหน้าตอนเริ่มเกม");
        }
    }

    /** เกมเริ่มต้นด้วย Player A เป็นผู้เล่นปัจจุบัน */
    @Test
    void gameStartsWithPlayerAAsCurrentPlayer() {
        assertEquals(game.getPlayerA(), game.getCurrentPlayer(), "Player A ต้องเล่นก่อน");
    }

    /** Phase เริ่มต้นคือ START_TURN */
    @Test
    void initialPhaseIsStartTurn() {
        assertEquals("START_TURN", game.getCurrentPhase(), "Phase แรกต้องเป็น START_TURN");
    }

    /** เทิร์นเริ่มต้นคือเทิร์น 1 */
    @Test
    void initialTurnNumberIsOne() {
        assertEquals(1, game.getTurnNumber(), "เทิร์นแรกต้องเป็นเทิร์นที่ 1");
    }

    /** Board ใช้ 7 ประเภทการ์ด (Attack, Heal, Energy, Shield, Negate, Steal, Extra) */
    @Test
    void boardContainsSevenCardTypes() {
        java.util.Set<String> types = new java.util.HashSet<>();
        for (int i = 0; i < game.getBoardSize(); i++) {
            types.add(game.getBoardCardType(i));
        }
        assertEquals(7, types.size(), "Board ต้องมี 7 ประเภทการ์ด");
    }

    // =========================================================
    // ส่วนที่ 2 : เริ่มเทิร์น
    // =========================================================

    /** เริ่มเทิร์นให้ +2 Energy กับผู้เล่นที่ active */
    @Test
    void startTurnGivesPlusTwoEnergy() {
        Player current = game.getCurrentPlayer();
        int before = current.getEnergy();
        game.startTurn();
        assertEquals(before + 2, current.getEnergy(), "startTurn ต้อง +2 Energy");
    }

    /** startTurn ไม่เปลี่ยน currentPlayer */
    @Test
    void startTurnDoesNotChangeCurrentPlayer() {
        Player before = game.getCurrentPlayer();
        game.startTurn();
        assertEquals(before, game.getCurrentPlayer(), "startTurn ต้องไม่เปลี่ยน active player");
    }

    /** startTurn รีเซ็ต match count เป็น 0 */
    @Test
    void startTurnResetsMatchCount() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0);
        game.revealBoardCard(1); // match 1
        assertEquals(1, game.getSuccessfulMatchesThisTurn());

        game.startTurn();
        assertEquals(0, game.getSuccessfulMatchesThisTurn(), "startTurn ต้อง reset match count เป็น 0");
    }

    /** startTurn คว่ำการ์ดทั้งหมดที่ยังไม่ matched */
    @Test
    void startTurnFlipsAllUnmatchedCardsFaceDown() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Heal", "Energy", "Shield",
                "Negate", "Steal", "Extra", "Attack",
                "Heal", "Energy", "Shield", "Negate",
                "Steal", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0); // flip one card face-up
        assertTrue(game.isBoardFaceUp(0), "ก่อน startTurn: การ์ดต้องหงายหน้า");

        game.startTurn();
        assertFalse(game.isBoardFaceUp(0), "หลัง startTurn: การ์ดที่ไม่ matched ต้องคว่ำหน้า");
    }

    /** ไม่ให้ +2 Energy หลัง game over */
    @Test
    void startTurnDoesNothingAfterGameOver() {
        game.getPlayerB().setHp(0);
        game.checkWinner();
        int energyBefore = game.getCurrentPlayer().getEnergy();
        game.startTurn();
        assertEquals(energyBefore, game.getCurrentPlayer().getEnergy(),
                "startTurn ต้องไม่ให้ Energy หลัง game over");
    }

    // =========================================================
    // ส่วนที่ 3 : เปิดการ์ดกองกลาง
    // =========================================================

    /** เปิดการ์ดใบแรกได้ฟรี และการ์ดหงายหน้า */
    @Test
    void firstRevealFlipsCardFaceUp() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        assertTrue(game.revealBoardCard(0), "เปิดใบแรกต้องสำเร็จ");
        assertTrue(game.isBoardFaceUp(0), "การ์ดต้องหงายหน้าหลังเปิด");
    }

    /** จับคู่สำเร็จ → ได้การ์ดเข้ามือ 1 ใบ */
    @Test
    void successfulMatchAddsOneCardToHand() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        int handBefore = game.getHand(game.getCurrentPlayer()).size();
        game.revealBoardCard(0);
        game.revealBoardCard(1); // Attack ↔ Attack
        assertEquals(handBefore + 1, game.getHand(game.getCurrentPlayer()).size(),
                "จับคู่สำเร็จต้องได้การ์ด 1 ใบ");
    }

    /** ไม่จับคู่ → การ์ดทั้งสองคว่ำกลับ ไม่ได้การ์ด */
    @Test
    void mismatchFlipsBothCardsFaceDown() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Heal", "Energy", "Shield",
                "Negate", "Steal", "Extra", "Attack",
                "Heal", "Energy", "Shield", "Negate",
                "Steal", "Extra", "Attack", "Heal"));
        int handBefore = game.getHand(game.getCurrentPlayer()).size();
        game.revealBoardCard(0); // Attack
        game.revealBoardCard(1); // Heal — ไม่คู่กัน
        assertFalse(game.isBoardFaceUp(0), "เมื่อไม่คู่: การ์ดใบแรกต้องคว่ำกลับ");
        assertFalse(game.isBoardFaceUp(1), "เมื่อไม่คู่: การ์ดใบสองต้องคว่ำกลับ");
        assertEquals(handBefore, game.getHand(game.getCurrentPlayer()).size(),
                "เมื่อไม่คู่: ต้องไม่ได้การ์ด");
    }

    /** เปิดฟรี 2 ครั้ง (match), ครั้งที่ 3 ต้องจ่าย Energy 4 */
    @Test
    void thirdRevealRequiresFourEnergy() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0);
        game.revealBoardCard(1); // match 1
        game.revealBoardCard(2);
        game.revealBoardCard(3); // match 2

        // หลังเปิดฟรี 2 ครั้ง: ต้อง block ถ้ายังไม่จ่าย Energy
        assertFalse(game.revealBoardCard(4), "ต้อง block ถ้ายังไม่จ่าย Energy");
    }

    /** จ่าย Energy 4 เพื่อเปิดต่อ — รีเซ็ต match count */
    @Test
    void payFourEnergyToContinueMatchingResetsCount() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0);
        game.revealBoardCard(1);
        game.revealBoardCard(2);
        game.revealBoardCard(3); // 2 matches ใช้สิทธิ์ฟรีหมด

        game.getCurrentPlayer().setEnergy(4);
        assertTrue(game.payEnergyToContinueMatchingBoard(), "ต้องสามารถจ่าย Energy 4 ได้");
        assertEquals(0, game.getCurrentPlayer().getEnergy(), "Energy ต้องหัก 4 ออก");
        assertEquals(0, game.getSuccessfulMatchesThisTurn(), "Match count ต้อง reset เป็น 0");
    }

    /** จ่าย Energy น้อยกว่า 4 — ล้มเหลว */
    @Test
    void payLessThanFourEnergyFails() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0);
        game.revealBoardCard(1);
        game.revealBoardCard(2);
        game.revealBoardCard(3);

        game.getCurrentPlayer().setEnergy(3);
        assertFalse(game.payEnergyToContinueMatchingBoard(), "Energy 3 ไม่พอ ต้องล้มเหลว");
        assertEquals(3, game.getCurrentPlayer().getEnergy(), "Energy ต้องไม่เปลี่ยน");
    }

    /** Board refill หลัง match — ขนาดยังคง 16 */
    @Test
    void boardRefillsTo16AfterMatch() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0);
        game.revealBoardCard(1);
        assertEquals(16, game.getBoardSize(), "Board ต้องยังมี 16 ช่องหลัง refill");
    }

    /** การ์ดที่ matched ต้องไม่อยู่ในสถานะ faceUp หรือ matched (เพราะ refill ใหม่) */
    @Test
    void matchedSlotsAreRefilledAndFaceDown() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0);
        game.revealBoardCard(1);
        assertFalse(game.isBoardFaceUp(0), "Slot ที่ refill ต้องคว่ำหน้า");
        assertFalse(game.isBoardMatched(0), "Slot ที่ refill ต้องไม่ถูก mark matched");
    }

    /** Extra match: จับคู่ Extra ใน 2 ครั้งแรก → ได้โอกาสเปิดเพิ่ม */
    @Test
    void extraMatchGrantsBonusMatchOpportunity() throws Exception {
        setBoardState(game, List.of(
                "Extra", "Extra", "Attack", "Attack",
                "Heal", "Heal", "Energy", "Energy",
                "Shield", "Shield", "Negate", "Negate",
                "Steal", "Steal", "Attack", "Heal"));

        // Match Extra (ครั้งที่ 1)
        game.revealBoardCard(0);
        game.revealBoardCard(1);
        assertEquals(1, game.getSuccessfulMatchesThisTurn());

        // Match Attack (ครั้งที่ 2)
        game.getCurrentPlayer().setEnergy(0); // ไม่มี Energy — จ่ายไม่ได้
        game.revealBoardCard(2);
        game.revealBoardCard(3);
        assertEquals(2, game.getSuccessfulMatchesThisTurn());

        // Extra bonus: เปิดได้ครั้งที่ 3 โดยไม่ต้องจ่าย Energy
        assertTrue(game.revealBoardCard(4), "Extra bonus ต้องให้เปิดครั้งที่ 3 ฟรี");
    }

    /** ไม่สามารถเปิดการ์ดเดิมที่หงายอยู่แล้ว */
    @Test
    void cannotRevealAlreadyFaceUpCard() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Heal", "Energy", "Shield",
                "Negate", "Steal", "Extra", "Attack",
                "Heal", "Energy", "Shield", "Negate",
                "Steal", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0); // หงาย
        assertFalse(game.revealBoardCard(0), "ต้อง block การเปิดการ์ดที่หงายอยู่แล้ว");
    }

    /** ไม่สามารถเปิดการ์ดเมื่อ game over */
    @Test
    void cannotRevealBoardCardAfterGameOver() throws Exception {
        game.getPlayerB().setHp(0);
        game.checkWinner();
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        assertFalse(game.revealBoardCard(0), "ต้อง block การเปิดการ์ดหลัง game over");
    }

    // =========================================================
    // ส่วนที่ 4 : ใช้งานการ์ดในมือ (Card Effects)
    // =========================================================

    /** Attack Card ลด HP ฝ่ายตรงข้าม */
    @Test
    void attackCardReducesOpponentHp() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        int damage = 500;
        int hpBefore = opponent.getHp();
        game.getHand(acting).addCard(new AttackCard(damage));

        game.playCardFromCurrentHand(0);
        // ถ้า opponent ไม่มี Negate/Steal chain window จะไม่เปิด
        if (!game.isChainWindowOpen()) {
            assertEquals(hpBefore - damage, opponent.getHp(),
                    "Attack Card ต้องลด HP ฝ่ายตรงข้าม");
        }
    }

    /** Attack Card หัก Energy ของผู้ใช้ (cost = 2) */
    @Test
    void attackCardCostsEnergy() {
        Player acting = game.getCurrentPlayer();
        acting.setEnergy(5);
        game.getHand(acting).addCard(new AttackCard(100));

        game.playCardFromCurrentHand(0);
        // cost = 2, energy หลัง = 5 - 2 = 3
        assertEquals(3, acting.getEnergy(), "Attack Card ต้องหัก 2 Energy");
    }

    /** Heal Card เพิ่ม HP ผู้ใช้ */
    @Test
    void healCardIncreasesUserHp() {
        Player acting = game.getCurrentPlayer();
        acting.setEnergy(5);
        acting.setHp(2000);
        game.getHand(acting).addCard(new HealCard(500));

        game.playCardFromCurrentHand(0);
        if (!game.isChainWindowOpen()) {
            assertEquals(2500, acting.getHp(), "Heal Card ต้องเพิ่ม HP ผู้ใช้");
        }
    }

    /** Energy Card เพิ่ม Energy ผู้ใช้ */
    @Test
    void energyCardIncreasesUserEnergy() {
        Player acting = game.getCurrentPlayer();
        acting.setEnergy(2);
        game.getHand(acting).addCard(new EnergyCard(3));

        game.playCardFromCurrentHand(0);
        if (!game.isChainWindowOpen()) {
            // cost = 0, energy = 2 - 0 + 3 = 5
            assertEquals(5, acting.getEnergy(), "Energy Card ต้องเพิ่ม Energy");
        }
    }

    /** Shield Card เพิ่ม Shield ผู้ใช้ */
    @Test
    void shieldCardGivesUserShield() {
        Player acting = game.getCurrentPlayer();
        acting.setEnergy(5);
        game.getHand(acting).addCard(new ShieldCard(1000));

        game.playCardFromCurrentHand(0);
        if (!game.isChainWindowOpen()) {
            assertEquals(1000, acting.getShield(), "Shield Card ต้องให้ Shield 1000");
        }
    }

    /** Shield ดูดซับดาเมจก่อน HP */
    @Test
    void shieldAbsorbsDamageBeforeHp() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        opponent.setShield(500);
        opponent.setHp(3000);

        acting.attack(opponent, 300); // ดาเมจน้อยกว่า Shield
        assertEquals(200, opponent.getShield(), "Shield ต้องดูดซับดาเมจก่อน");
        assertEquals(3000, opponent.getHp(), "HP ต้องไม่ลดถ้า Shield ยังดูดซับได้");
    }

    /** Shield ทะลุเกิน → ส่วนที่เกินลด HP */
    @Test
    void damageExceedingShieldReducesHp() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        opponent.setShield(200);
        opponent.setHp(3000);

        acting.attack(opponent, 500);
        assertEquals(0, opponent.getShield(), "Shield ต้องหมดเมื่อดาเมจเกิน");
        assertEquals(2700, opponent.getHp(), "HP ต้องลดในส่วนที่ Shield ดูดซับไม่ได้");
    }

    /** Negate Card ลบ Shield และ -1 Energy ของ target */
    @Test
    void negateCardRemovesShieldAndEnergy() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setShield(1000);
        opponent.setEnergy(3);
        game.getHand(acting).addCard(new NegateCard());

        game.playCardFromCurrentHand(0);
        if (!game.isChainWindowOpen()) {
            assertEquals(0, opponent.getShield(), "Negate ต้องลบ Shield ฝ่ายตรงข้าม");
            assertEquals(2, opponent.getEnergy(), "Negate ต้องลด Energy ฝ่ายตรงข้าม 1");
        }
    }

    /** Steal Card โอน Energy 1 จาก target ให้ user */
    @Test
    void stealCardTransfersEnergyFromTargetToUser() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setEnergy(4);
        game.getHand(acting).addCard(new StealCard());

        game.playCardFromCurrentHand(0);
        if (!game.isChainWindowOpen()) {
            // cost = 3, user: 5 - 3 + 1 = 3
            assertEquals(3, acting.getEnergy(), "Steal: user ต้องได้ Energy +1");
            assertEquals(3, opponent.getEnergy(), "Steal: target ต้องเสีย Energy -1");
        }
    }

    /** ไม่สามารถใช้การ์ดถ้า Energy ไม่พอ */
    @Test
    void cannotPlayCardWithInsufficientEnergy() {
        Player acting = game.getCurrentPlayer();
        acting.setEnergy(1); // Attack cost = 2
        game.getHand(acting).addCard(new AttackCard(100));

        assertFalse(game.playCardFromCurrentHand(0), "ต้อง block ถ้า Energy ไม่พอ");
        assertEquals(1, acting.getEnergy(), "Energy ต้องไม่เปลี่ยน");
        assertEquals(1, game.getHand(acting).size(), "การ์ดต้องยังอยู่ในมือ");
    }

    /** ไม่สามารถใช้การ์ดด้วย index ที่ invalid */
    @Test
    void playCardWithInvalidIndexReturnsFalse() {
        game.getCurrentPlayer().setEnergy(5);
        assertFalse(game.playCardFromCurrentHand(-1), "index -1 ต้องคืน false");
        assertFalse(game.playCardFromCurrentHand(99), "index เกินต้องคืน false");
    }

    /** ไม่สามารถใช้การ์ดขณะ chain window เปิดอยู่ */
    @Test
    void cannotPlayCardWhileChainWindowOpen() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(10);
        game.getHand(acting).addCard(new AttackCard(100));
        game.getHand(acting).addCard(new HealCard(100));
        game.getHand(opponent).addCard(new NegateCard());

        game.playCardFromCurrentHand(0); // เปิด chain window
        assertTrue(game.isChainWindowOpen());

        assertFalse(game.playCardFromCurrentHand(0),
                "ต้อง block การเล่นการ์ดขณะ chain window เปิด");
    }

    // =========================================================
    // ส่วนที่ 5 : Chain Effect
    // =========================================================

    /** เมื่อ opponent มี Negate → chain window เปิด */
    @Test
    void chainWindowOpensWhenOpponentHasNegate() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        game.getHand(acting).addCard(new AttackCard(100));
        game.getHand(opponent).addCard(new NegateCard());

        game.playCardFromCurrentHand(0);
        assertTrue(game.isChainWindowOpen(), "Chain window ต้องเปิดเมื่อ opponent มี Negate");
        assertEquals(opponent, game.getChainTurnPlayer(),
                "Chain turn ต้องเป็น opponent ก่อน");
    }

    /** เมื่อ opponent มี Steal → chain window เปิด */
    @Test
    void chainWindowOpensWhenOpponentHasSteal() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(10);
        game.getHand(acting).addCard(new AttackCard(100));
        game.getHand(opponent).addCard(new StealCard());

        game.playCardFromCurrentHand(0);
        assertTrue(game.isChainWindowOpen(), "Chain window ต้องเปิดเมื่อ opponent มี Steal");
    }

    /** เมื่อ opponent ไม่มีการ์ดตอบโต้ → resolve ทันที ไม่เปิด chain window */
    @Test
    void noChainWindowWhenOpponentCannotCounter() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        game.getHand(acting).addCard(new AttackCard(100));
        // opponent ไม่มีการ์ด

        int hpBefore = opponent.getHp();
        game.playCardFromCurrentHand(0);
        assertFalse(game.isChainWindowOpen(), "ต้องไม่เปิด chain window ถ้า opponent ไม่มีตอบโต้");
        assertEquals(hpBefore - 100, opponent.getHp(), "Attack ต้อง resolve ทันที");
    }

    /** chainWithCard ด้วย Negate → resolve chain LIFO → attack ถูก Negate */
    @Test
    void chainWithNegateResolvesCorrectly() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setEnergy(3);
        int hpBefore = opponent.getHp();
        game.getHand(acting).addCard(new AttackCard(500));
        game.getHand(opponent).addCard(new NegateCard());

        game.playCardFromCurrentHand(0); // Attack → chain window
        assertTrue(game.isChainWindowOpen());

        game.chainWithCard(0); // Negate → resolve
        assertFalse(game.isChainWindowOpen(), "Chain window ต้องปิดหลัง resolve");
        // Negate (cost 2) resolves first: removes shield, -1 energy from acting
        // Then Attack resolves: deals damage to opponent
        // Note: order is LIFO — Negate(1) resolves before Attack(0)
        // Negate targets acting (attacker = i%2==0 → pendingCardUser=acting, so Negate user=opponent, target=acting)
        assertEquals(2, acting.getEnergy(), "Negate ต้องลด Energy ผู้ใช้ Attack");
    }

    /** passChain → resolve chain ทันที */
    @Test
    void passChainResolvesImmediately() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setEnergy(3);
        int hpBefore = opponent.getHp();
        game.getHand(acting).addCard(new AttackCard(500));
        game.getHand(opponent).addCard(new NegateCard());

        game.playCardFromCurrentHand(0);
        assertTrue(game.isChainWindowOpen());

        game.passChain();
        assertFalse(game.isChainWindowOpen(), "passChain ต้องปิด chain window");
    }

    /** passChain โดยไม่มี chain window → คืน false */
    @Test
    void passChainReturnsFalseWhenNoWindowOpen() {
        assertFalse(game.passChain(), "passChain ต้องคืน false ถ้าไม่มี chain window");
    }

    /** chainWithCard ด้วยการ์ดที่ไม่ใช่ Negate/Steal → ล้มเหลว */
    @Test
    void chainWithNonCounterCardFails() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setEnergy(5);
        game.getHand(acting).addCard(new AttackCard(100));
        game.getHand(opponent).addCard(new NegateCard());
        game.getHand(opponent).addCard(new HealCard(100)); // ใบที่ 1

        game.playCardFromCurrentHand(0); // เปิด chain window
        assertTrue(game.isChainWindowOpen());

        assertFalse(game.chainWithCard(1),
                "ต้อง reject chain ด้วยการ์ดที่ไม่ใช่ Negate/Steal");
    }

    /** chain window ใช้ stack size ถูกต้อง */
    @Test
    void chainSystemStackSizeIsCorrectAfterPlay() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setEnergy(5);
        game.getHand(acting).addCard(new AttackCard(100));
        game.getHand(opponent).addCard(new NegateCard());

        game.playCardFromCurrentHand(0);
        assertEquals(1, game.getChainSystem().size(),
                "chain stack ต้องมี 1 การ์ด (Attack) หลัง play");
    }

    /** หลัง resolve chain → chainSystem.clear() */
    @Test
    void chainSystemClearedAfterResolve() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setEnergy(5);
        game.getHand(acting).addCard(new AttackCard(100));
        game.getHand(opponent).addCard(new NegateCard());

        game.playCardFromCurrentHand(0);
        game.chainWithCard(0); // resolve
        assertEquals(0, game.getChainSystem().size(),
                "chain stack ต้องว่างหลัง resolve");
    }

    // =========================================================
    // ส่วนที่ 6 : จบเทิร์น
    // =========================================================

    /** endTurn → สลับ currentPlayer */
    @Test
    void endTurnSwitchesCurrentPlayer() {
        Player before = game.getCurrentPlayer();
        game.endTurn();
        assertNotEquals(before, game.getCurrentPlayer(),
                "endTurn ต้องสลับ currentPlayer");
    }

    /** endTurn → Player B ได้ +2 Energy */
    @Test
    void endTurnGivesPlusTwoEnergyToNextPlayer() {
        Player playerB = game.getPlayerB();
        int before = playerB.getEnergy();
        game.endTurn(); // สลับไป B, startTurn → +2
        assertEquals(before + 2, playerB.getEnergy(),
                "Player B ต้องได้ +2 Energy หลัง endTurn");
    }

    /** endTurn → turnNumber เพิ่มขึ้น */
    @Test
    void endTurnIncreasesTurnNumber() {
        int before = game.getTurnNumber();
        game.endTurn();
        assertEquals(before + 1, game.getTurnNumber(),
                "endTurn ต้อง increment turnNumber");
    }

    /** endTurn → Board สับใหม่ ยังมี 16 ใบ */
    @Test
    void endTurnReshufflesBoardTo16Cards() {
        game.endTurn();
        assertEquals(16, game.getBoardSize(),
                "Board ต้องมี 16 ช่องหลัง endTurn");
    }

    /** endTurn → match count รีเซ็ต */
    @Test
    void endTurnResetsMatchCount() throws Exception {
        setBoardState(game, List.of(
                "Attack", "Attack", "Heal", "Heal",
                "Energy", "Energy", "Shield", "Shield",
                "Negate", "Negate", "Steal", "Steal",
                "Extra", "Extra", "Attack", "Heal"));
        game.revealBoardCard(0);
        game.revealBoardCard(1); // match 1
        assertEquals(1, game.getSuccessfulMatchesThisTurn());

        game.endTurn();
        assertEquals(0, game.getSuccessfulMatchesThisTurn(),
                "endTurn ต้อง reset match count");
    }

    /** endTurn → phase เปลี่ยนเป็น START_TURN */
    @Test
    void endTurnChangesPhaseToStartTurn() {
        game.endTurn();
        assertEquals("START_TURN", game.getCurrentPhase(),
                "endTurn ต้องเปลี่ยน phase เป็น START_TURN");
    }

    /** endTurn ไม่ทำงานหลัง game over */
    @Test
    void endTurnDoesNothingAfterGameOver() {
        game.getPlayerB().setHp(0);
        game.checkWinner();
        Player before = game.getCurrentPlayer();
        game.endTurn();
        assertEquals(before, game.getCurrentPlayer(),
                "endTurn ต้องไม่ทำงานหลัง game over");
    }

    // =========================================================
    // ส่วนที่ 7 : เงื่อนไขการชนะ
    // =========================================================

    /** HP ลดเหลือ 0 → แพ้ ฝ่ายอื่นชนะ */
    @Test
    void playerBLosesWhenHpReachesZero() {
        game.getPlayerB().setHp(0);
        assertTrue(game.checkWinner(), "checkWinner ต้องคืน true เมื่อ HP = 0");
        assertTrue(game.isGameOver(), "isGameOver ต้องเป็น true");
    }

    /** Player A HP = 0 → Player B ชนะ */
    @Test
    void playerBWinsWhenPlayerAHpIsZero() {
        game.getPlayerA().setHp(0);
        game.checkWinner();
        assertTrue(game.isGameOver(), "เกมต้องจบเมื่อ Player A HP = 0");
    }

    /** Player B HP = 0 → Player A ชนะ */
    @Test
    void playerAWinsWhenPlayerBHpIsZero() {
        game.getPlayerB().setHp(0);
        game.checkWinner();
        assertTrue(game.isGameOver(), "เกมต้องจบเมื่อ Player B HP = 0");
    }

    /** Attack ทำให้ HP = 0 → checkWinner คืน true */
    @Test
    void attackReducingHpToZeroTriggersWin() {
        Player acting = game.getCurrentPlayer();
        Player opponent = game.getOpponent();
        acting.setEnergy(5);
        opponent.setHp(100);
        game.getHand(acting).addCard(new AttackCard(100));

        game.playCardFromCurrentHand(0);
        // ถ้าไม่มี chain window (opponent ไม่มีตอบโต้)
        if (!game.isChainWindowOpen()) {
            assertTrue(game.isGameOver(), "เมื่อ HP = 0 จาก attack เกมต้องจบ");
        }
    }

    /** HP ไม่ลดต่ำกว่า 0 */
    @Test
    void hpCannotGoBelowZero() {
        Player opponent = game.getOpponent();
        opponent.setHp(50);
        game.getCurrentPlayer().attack(opponent, 9999);
        assertEquals(0, opponent.getHp(), "HP ต้องไม่ต่ำกว่า 0");
    }

    /** ถ้าเกมยังไม่จบ checkWinner คืน false */
    @Test
    void checkWinnerReturnsFalseWhenBothPlayersAlive() {
        assertFalse(game.checkWinner(), "checkWinner ต้องคืน false ถ้าทั้งสอง HP > 0");
        assertFalse(game.isGameOver());
    }

    /** หลัง game over → statusMessage ระบุผู้ชนะ */
    @Test
    void winnerStatusMessageIsSetCorrectly() {
        game.getPlayerB().setHp(0);
        game.checkWinner();
        assertTrue(game.getStatusMessage().contains(game.getPlayerA().getName()),
                "statusMessage ต้องระบุชื่อผู้ชนะ");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void setBoardState(GameFlow g, List<String> cardTypes) throws Exception {
        setFieldList(g, "boardCardTypes", cardTypes);
        setFieldList(g, "boardFaceUp", buildFalseList(cardTypes.size()));
        setFieldList(g, "boardMatched", buildFalseList(cardTypes.size()));
    }

    private List<Boolean> buildFalseList(int size) {
        List<Boolean> list = new ArrayList<>();
        for (int i = 0; i < size; i++) list.add(false);
        return list;
    }

    @SuppressWarnings("unchecked")
    private void setFieldList(GameFlow g, String fieldName, List<?> values) throws Exception {
        Field field = GameFlow.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        List<Object> list = (List<Object>) field.get(g);
        list.clear();
        list.addAll(values);
    }
}
