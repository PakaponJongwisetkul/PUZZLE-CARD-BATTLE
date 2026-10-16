import java.util.ArrayList;
import java.util.List;

public class Hand {
    private final List<Card> handCards;
    private final int maxSize = 8;

    public Hand() {
        handCards = new ArrayList<>();
    }

    public void addCard(Card card) {
        if (card != null && handCards.size() < maxSize) {
            handCards.add(card);
        }
    }

    public Card removeCard(int index) {
        if (index < 0 || index >= handCards.size()) {
            return null;
        }
        return handCards.remove(index);
    }

    public void clear() {
        handCards.clear();
    }

    public int size() {
        return handCards.size();
    }

    public List<Card> getCards() {
        return new ArrayList<>(handCards);
    }

    public boolean isEmpty() {
        return handCards.isEmpty();
    }
}