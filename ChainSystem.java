import java.util.ArrayList;
import java.util.List;

public class ChainSystem {
    private final List<Card> chainStack;

    public ChainSystem() {
        chainStack = new ArrayList<>();
    }

    public void addCard(Card card) {
        if (card != null) {
            chainStack.add(card);
        }
    }

    /** Clear the chain stack (called by GameFlow after resolving) */
    public void clear() {
        chainStack.clear();
    }

    /**
     * Legacy resolve — resolves all cards with the same user/target pair.
     * Prefer using GameFlow.resolveChain() for proper multi-owner resolution.
     */
    public void resolve(Player user, Player target) {
        for (int i = chainStack.size() - 1; i >= 0; i--) {
            Card card = chainStack.get(i);
            card.use(user, target);
        }
        chainStack.clear();
    }

    public boolean isEmpty() {
        return chainStack.isEmpty();
    }

    public int size() {
        return chainStack.size();
    }

    public List<Card> getChainStack() {
        return new ArrayList<>(chainStack);
    }
}