import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameFlow {
    private final Player playerA;
    private final Player playerB;
    private final Deck deckA;
    private final Deck deckB;
    private final Hand handA;
    private final Hand handB;
    private final ChainSystem chainSystem;

    private final List<String> boardCardTypes;
    private final List<Boolean> boardFaceUp;
    private final List<Boolean> boardMatched;
    private final List<String> specialZoneCards;
    private int pendingBoardIndex;
    private int boardRevealCount;

    private Player currentPlayer;
    private Player winner;
    private int turnNumber;
    private int drawsThisTurn;
    private int extraMatchBonusThisTurn;
    private String currentPhase;
    private String statusMessage;

    // Chain state
    private boolean chainWindowOpen;
    private Player chainTurnPlayer;   // whose turn it is to respond in the chain
    private Card pendingCard;         // card waiting to be negated/resolved
    private Player pendingCardUser;   // who played the pending card

    public GameFlow() {
        playerA = new Player("Player A", 3000);
        playerB = new Player("Player B", 3000);
        deckA = new Deck();
        deckB = new Deck();
        handA = new Hand();
        handB = new Hand();
        chainSystem = new ChainSystem();
        boardCardTypes = new ArrayList<>();
        boardFaceUp = new ArrayList<>();
        boardMatched = new ArrayList<>();
        specialZoneCards = new ArrayList<>();
        pendingBoardIndex = -1;
        boardRevealCount = 0;
        drawsThisTurn = 0;
        currentPlayer = playerA;
        winner = null;
        turnNumber = 1;
        currentPhase = "SETUP";
        statusMessage = "Game ready.";
        chainWindowOpen = false;
        chainTurnPlayer = null;
        pendingCard = null;
        pendingCardUser = null;
    }

    public void startGame() {
        playerA.setHp(3000);
        playerB.setHp(3000);
        playerA.setEnergy(0);
        playerB.setEnergy(0);
        playerA.setShield(0);
        playerB.setShield(0);
        deckA.reset();
        deckB.reset();
        handA.clear();
        handB.clear();
        specialZoneCards.clear();
        currentPlayer = playerA;
        winner = null;
        turnNumber = 1;
        currentPhase = "SETUP";
        pendingBoardIndex = -1;
        boardRevealCount = 0;
        extraMatchBonusThisTurn = 0;
        setupBoard();
        startTurn();

        statusMessage = "เริ่มเกม: เทิร์นของ " + currentPlayer.getName() + " (ได้รับ Energy +2) • กองกลางพร้อมให้เลือกเปิด";
    }

    public void startTurn() {
        if (winner != null) {
            return;
        }

        Player acting = currentPlayer;
        acting.gainEnergy(2);
        drawsThisTurn = 0;
        extraMatchBonusThisTurn = 0;
        currentPhase = "START_TURN";

        pendingBoardIndex = -1;
        boardRevealCount = 0;
        for (int i = 0; i < boardFaceUp.size(); i++) {
            if (!boardMatched.get(i)) {
                boardFaceUp.set(i, false);
            }
        }

        statusMessage = "เทิร์นของ " + acting.getName() + " (ได้รับ Energy +2) • กองกลางพร้อมให้เลือกเปิด";
    }

    public int getDrawsThisTurn() {
        return drawsThisTurn;
    }

    public int getSuccessfulMatchesThisTurn() {
        return drawsThisTurn;
    }

    public boolean canDrawCardForCurrentPlayer() {
        if (winner != null) {
            return false;
        }
        Player acting = currentPlayer;
        return drawsThisTurn < 2 || acting.getEnergy() >= 4;
    }

    public boolean canContinueMatchingBoardWithEnergy() {
        return currentPlayer != null && currentPlayer.getEnergy() >= 4 && drawsThisTurn >= 2;
    }

    public boolean payEnergyToContinueMatchingBoard() {
        if (winner != null) {
            return false;
        }

        Player acting = currentPlayer;
        if (acting.getEnergy() < 4) {
            statusMessage = acting.getName() + " ต้องใช้ Energy 4 หน่วยเพื่อเปิดการ์ดต่อ";
            return false;
        }

        acting.gainEnergy(-4);
        drawsThisTurn = 0;
        currentPhase = "DRAW";
        statusMessage = acting.getName() + " จ่าย Energy 4 หน่วย เพื่อเปิดการ์ดกองกลางต่อ";
        return true;
    }

    public boolean drawCardForCurrentPlayer() {
        if (winner != null) {
            return false;
        }

        Player acting = currentPlayer;
        if (drawsThisTurn >= 2) {
            statusMessage = acting.getName() + " already drew twice this turn. Pay 2 Energy to draw again.";
            return false;
        }

        Card drawn = drawCardFor(acting);
        if (drawn == null) {
            statusMessage = acting.getName() + " cannot draw from an empty deck.";
            return false;
        }

        getHand(acting).addCard(drawn);
        drawsThisTurn++;
        currentPhase = "DRAW";
        statusMessage = acting.getName() + " drew " + drawn.getName() + ".";
        return true;
    }

    public boolean payEnergyToDrawCard() {
        if (winner != null) {
            return false;
        }

        Player acting = currentPlayer;
        if (acting.getEnergy() < 2) {
            statusMessage = acting.getName() + " needs 2 Energy to pay for another draw.";
            return false;
        }

        acting.gainEnergy(-2);
        Card drawn = drawCardFor(acting);
        if (drawn == null) {
            acting.gainEnergy(2);
            statusMessage = acting.getName() + " cannot draw from an empty deck.";
            return false;
        }

        getHand(acting).addCard(drawn);
        drawsThisTurn++;
        currentPhase = "DRAW";
        statusMessage = acting.getName() + " paid 2 Energy and drew " + drawn.getName() + ".";
        return true;
    }

    public boolean playCardFromCurrentHand(int index) {
        if (winner != null || chainWindowOpen) {
            return false;
        }

        Player acting = currentPlayer;
        Hand hand = getHand(acting);
        if (index < 0 || index >= hand.size()) {
            statusMessage = "เลือกการ์ดไม่ถูกต้อง";
            return false;
        }

        Card card = hand.getCards().get(index);
        if (card.getCost() > acting.getEnergy()) {
            statusMessage = acting.getName() + " มี Energy ไม่พอ (ต้องการ " + card.getCost() + ")";
            return false;
        }

        Player target = getOpponent(acting);
        currentPhase = "ACTION";
        acting.gainEnergy(-card.getCost());
        hand.removeCard(index);

        // Check if opponent can chain
        if (canChainCounter(target)) {
            // Open chain window — card effect is pending
            chainWindowOpen = true;
            chainTurnPlayer = target;   // opponent gets to respond first
            pendingCard = card;
            pendingCardUser = acting;
            chainSystem.addCard(card);
            statusMessage = acting.getName() + " ใช้ " + card.getName() + " → " + target.getName() + " มีสิทธิ์เชน (Negate / Steal) หรือ Pass";
        } else {
            // No chain possible — resolve immediately
            card.use(acting, target);
            chainSystem.getChainStack(); // ensure empty
            statusMessage = acting.getName() + " ใช้ " + card.getName() + " → " + describeCardEffect(card, acting, target) + " (สำเร็จ)";
            checkWinner();
        }

        return true;
    }

    /**
     * Called when the current chain-turn player decides to chain a counter-card.
     * chainResponderIndex = index in the chain-turn player's hand.
     */
    public boolean chainWithCard(int handIndex) {
        if (!chainWindowOpen || chainTurnPlayer == null) {
            return false;
        }

        Hand hand = getHand(chainTurnPlayer);
        if (handIndex < 0 || handIndex >= hand.size()) {
            statusMessage = "เลือกการ์ดไม่ถูกต้อง";
            return false;
        }

        Card chainCard = hand.getCards().get(handIndex);
        String name = chainCard.getName();
        if (!"Negate".equals(name) && !"Steal".equals(name)) {
            statusMessage = "สามารถเชนได้เฉพาะ Negate หรือ Steal เท่านั้น";
            return false;
        }
        if (chainCard.getCost() > chainTurnPlayer.getEnergy()) {
            statusMessage = chainTurnPlayer.getName() + " มี Energy ไม่พอ";
            return false;
        }

        chainTurnPlayer.gainEnergy(-chainCard.getCost());
        hand.removeCard(handIndex);
        chainSystem.addCard(chainCard);

        // Flip chain turn to the other player
        Player prevChainPlayer = chainTurnPlayer;
        chainTurnPlayer = getOpponent(chainTurnPlayer);

        // Check if the new chain-turn player can also respond
        if (canChainCounter(chainTurnPlayer)) {
            statusMessage = prevChainPlayer.getName() + " เชนด้วย " + chainCard.getName()
                    + " → " + chainTurnPlayer.getName() + " มีสิทธิ์เชนต่อ หรือ Pass";
        } else {
            statusMessage = prevChainPlayer.getName() + " เชนด้วย " + chainCard.getName()
                    + " → ไม่มีการเชนต่อ กำลัง Resolve...";
            resolveChain();
        }
        return true;
    }

    /**
     * Called when the current chain-turn player decides NOT to chain (pass).
     */
    public boolean passChain() {
        if (!chainWindowOpen || chainTurnPlayer == null) {
            return false;
        }

        Player passing = chainTurnPlayer;
        // Resolve the chain stack (LIFO)
        resolveChain();
        statusMessage += " ← " + passing.getName() + " Pass → Chain Resolved";
        return true;
    }

    /** Resolve chain stack in LIFO order */
    private void resolveChain() {
        java.util.List<Card> stack = chainSystem.getChainStack();
        // stack[0] = original card, stack[last] = most recent chain card
        // Resolve last-in first-out
        for (int i = stack.size() - 1; i >= 0; i--) {
            Card c = stack.get(i);
            Player user  = (i % 2 == 0) ? pendingCardUser : getOpponent(pendingCardUser);
            Player tgt   = getOpponent(user);
            c.use(user, tgt);
        }
        // Build result description
        StringBuilder sb = new StringBuilder();
        for (int i = stack.size() - 1; i >= 0; i--) {
            Card c = stack.get(i);
            Player user = (i % 2 == 0) ? pendingCardUser : getOpponent(pendingCardUser);
            sb.append(c.getName()).append(" (").append(user.getName()).append(")");
            if (i > 0) sb.append(" → ");
        }
        chainWindowOpen = false;
        chainTurnPlayer = null;
        pendingCard = null;
        pendingCardUser = null;
        currentPhase = "ACTION";
        statusMessage = "Chain Resolved: " + sb.toString();
        chainSystem.clear();
        checkWinner();
    }

    public boolean isChainWindowOpen() { return chainWindowOpen; }
    public Player getChainTurnPlayer() { return chainTurnPlayer; }



    public void endTurn() {
        if (winner != null) {
            return;
        }

        currentPlayer = getOpponent(currentPlayer);
        pendingBoardIndex = -1;
        boardRevealCount = 0;
        drawsThisTurn = 0;
        extraMatchBonusThisTurn = 0;
        setupBoard();
        turnNumber += 1;
        startTurn();
        statusMessage = "เทิร์นของ " + currentPlayer.getName() + " (ได้รับ Energy +2) • กองกลางสับใหม่พร้อมเปิด";
    }

    public boolean checkWinner() {
        if (playerA.getHp() <= 0) {
            winner = playerB;
            statusMessage = playerB.getName() + " wins!";
            return true;
        }

        if (playerB.getHp() <= 0) {
            winner = playerA;
            statusMessage = playerA.getName() + " wins!";
            return true;
        }

        return false;
    }

    public void setCurrentPlayer(Player player) {
        if (player == playerA || player == playerB) {
            currentPlayer = player;
        }
    }

    public Player getPlayerA() { return playerA; }
    public Player getPlayerB() { return playerB; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public Player getOpponent() {
        return currentPlayer == playerA ? playerB : playerA;
    }

    public Player getOpponent(Player player) {
        return player == playerA ? playerB : playerA;
    }

    public Hand getHand(Player player) {
        return player == playerA ? handA : handB;
    }

    public Deck getDeck(Player player) {
        return player == playerA ? deckA : deckB;
    }

    public int getBoardSize() {
        return boardCardTypes.size();
    }

    public String getBoardCardType(int index) {
        return (index >= 0 && index < boardCardTypes.size()) ? boardCardTypes.get(index) : "";
    }

    public boolean isBoardFaceUp(int index) {
        return index >= 0 && index < boardFaceUp.size() && boardFaceUp.get(index);
    }

    public boolean isBoardMatched(int index) {
        return index >= 0 && index < boardMatched.size() && boardMatched.get(index);
    }

    public boolean revealBoardCard(int index) {
        if (winner != null || index < 0 || index >= boardCardTypes.size()) {
            return false;
        }
        if (boardMatched.get(index) || boardFaceUp.get(index)) {
            return false;
        }
        boolean usingExtraBonus = drawsThisTurn >= 2 && extraMatchBonusThisTurn > 0;
        if (drawsThisTurn >= 2 && currentPlayer.getEnergy() < 2 && !usingExtraBonus) {
            statusMessage = currentPlayer.getName() + " already matched 2 pairs this turn. Pay 2 Energy to continue.";
            return false;
        }
        if (drawsThisTurn >= 2 && currentPlayer.getEnergy() >= 2 && !usingExtraBonus) {
            statusMessage = currentPlayer.getName() + " can pay 2 Energy to continue matching board cards.";
            return false;
        }

        boardFaceUp.set(index, true);
        boardRevealCount++;

        currentPhase = "REVEAL";
        if (pendingBoardIndex == -1) {
            pendingBoardIndex = index;
            statusMessage = currentPlayer.getName() + " revealed " + boardCardTypes.get(index) + ". Select the matching card.";
            return true;
        }

        String firstType = boardCardTypes.get(pendingBoardIndex);
        String secondType = boardCardTypes.get(index);

        if (firstType.equals(secondType)) {
            currentPhase = "MATCH";
            boardMatched.set(pendingBoardIndex, true);
            boardMatched.set(index, true);
            boardFaceUp.set(pendingBoardIndex, false);
            boardFaceUp.set(index, false);

            boolean wasUsingExtraBonus = drawsThisTurn >= 2 && extraMatchBonusThisTurn > 0;
            Card reward = createCardFromType(firstType);
            getHand(currentPlayer).addCard(reward);
            int previousDrawsThisTurn = drawsThisTurn;
            drawsThisTurn++;
            if ("Extra".equals(firstType)) {
                extraMatchBonusThisTurn++;
            }
            if (wasUsingExtraBonus && previousDrawsThisTurn >= 2) {
                extraMatchBonusThisTurn = Math.max(0, extraMatchBonusThisTurn - 1);
            }
            specialZoneCards.add(firstType);
            refillMatchedBoardSlots();
            if ("Extra".equals(firstType)) {
                statusMessage = currentPlayer.getName() + " matched Extra and gained +1 extra pair chance for this turn. The board refills to 16 cards.";
            } else {
                statusMessage = currentPlayer.getName() + " matched " + firstType + " and gained 1 card to hand. The board refills to 16 cards.";
            }
            pendingBoardIndex = -1;
            boardRevealCount = 0;
        } else {
            currentPhase = "RESET";
            statusMessage = "No match: " + firstType + " vs " + secondType + ". Try again.";
            boardFaceUp.set(pendingBoardIndex, false);
            boardFaceUp.set(index, false);
            pendingBoardIndex = -1;
            boardRevealCount = 0;
        }

        return true;
    }

    public boolean isBoardCleared() {
        for (boolean matched : boardMatched) {
            if (!matched) {
                return false;
            }
        }
        return !boardCardTypes.isEmpty();
    }

    public List<String> getSpecialZoneCards() {
        return new ArrayList<>(specialZoneCards);
    }

    public boolean canChainCounter(Player opponent) {
        if (opponent == null) {
            return false;
        }
        for (Card card : getHand(opponent).getCards()) {
            String name = card.getName();
            if ("Negate".equals(name) || "Steal".equals(name)) {
                return true;
            }
        }
        return false;
    }

    public ChainSystem getChainSystem() {
        return chainSystem;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isGameOver() {
        return winner != null;
    }

    /**
     * สร้างชุดการ์ดสำหรับสนาม 4×4 (16 ใบ):
     *   - 7 ประเภท × 2 = 14 ใบ (ทุกประเภทมีคู่)
     *   - สุ่มอีก 2 ใบจาก 7 ประเภท (ทำให้ 1 ประเภทมี 4 ใบ หรือ 2 ประเภทมีอย่างละ 3 ใบ)
     */
    private List<String> buildBoardCardPool() {
        String[] types = {"Attack", "Heal", "Energy", "Shield", "Negate", "Steal", "Extra"};
        List<String> pool = new ArrayList<>();
        // 7 ประเภท × 2 ใบ = 14 ใบ
        for (String t : types) {
            pool.add(t);
            pool.add(t);
        }
        // อีก 2 ใบสุ่มจาก 7 ประเภท
        for (int i = 0; i < 2; i++) {
            pool.add(types[(int) (Math.random() * types.length)]);
        }
        Collections.shuffle(pool);
        return pool; // 16 ใบ
    }

    private void setupBoard() {
        boardCardTypes.clear();
        boardFaceUp.clear();
        boardMatched.clear();

        for (String template : buildBoardCardPool()) {
            boardCardTypes.add(template);
            boardFaceUp.add(false);
            boardMatched.add(false);
        }
    }

    private void refillMatchedBoardSlots() {
        // นับจำนวนช่องที่ matched (ต้องเติม)
        int matchedCount = 0;
        for (int i = 0; i < boardMatched.size(); i++) {
            if (boardMatched.get(i)) matchedCount++;
        }
        if (matchedCount == 0) return;

        // สร้าง pool สำหรับเติม: ขนาดเท่ากับจำนวนช่องที่ matched
        // ใช้ logic เดียวกับ buildBoardCardPool แต่ scale ลง
        // ง่ายสุด: สุ่มจาก 7 ประเภทเท่าๆ กัน (คู่ละ 1) + random extra
        String[] types = {"Attack", "Heal", "Energy", "Shield", "Negate", "Steal", "Extra"};
        List<String> refillPool = new ArrayList<>();
        // เติม pair เพื่อให้ matchable — ใช้ buildBoardCardPool() แล้ว trim
        List<String> full = buildBoardCardPool();
        // ใช้แค่ matchedCount ใบแรก (already shuffled)
        for (int i = 0; i < matchedCount && i < full.size(); i++) {
            refillPool.add(full.get(i));
        }

        int ri = 0;
        for (int i = 0; i < boardCardTypes.size(); i++) {
            if (boardMatched.get(i)) {
                boardCardTypes.set(i, ri < refillPool.size() ? refillPool.get(ri++) : types[0]);
                boardFaceUp.set(i, false);
                boardMatched.set(i, false);
            }
        }
    }

    private Card createCardFromType(String cardType) {
        return switch (cardType) {
            case "Attack" -> new AttackCard(10);
            case "Heal" -> new HealCard(15);
            case "Energy" -> new EnergyCard(2);
            case "Shield" -> new ShieldCard(10);
            case "Negate" -> new NegateCard();
            case "Steal" -> new StealCard();
            case "Extra" -> new ExtraCard();
            default -> new EnergyCard(1);
        };
    }

    private Card drawCardFor(Player player) {
        return getDeck(player).drawCard();
    }

    private String describeCardEffect(Card card, Player user, Player target) {
        String name = card.getName();
        return switch (name) {
            case "Attack" -> "500 ATK to " + target.getName();
            case "Heal" -> "+500 HP to " + user.getName();
            case "Energy" -> "+2 Energy";
            case "Shield" -> "+1000 Shield";
            case "Negate" -> "Negate + remove 1 Energy from " + target.getName();
            case "Steal" -> "Steal +1 Energy from " + target.getName();
            case "Extra" -> "+1 extra match chance";
            default -> "effect resolved";
        };
    }
}