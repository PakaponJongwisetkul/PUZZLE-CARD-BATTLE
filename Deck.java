import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Deck {
    private final Queue<Card> cards;

    public Deck() {
        cards = new LinkedList<>();
        reset();
    }

    public void reset() {
        cards.clear();
        List<Card> list = new ArrayList<>();
        list.add(new AttackCard(500));
        list.add(new AttackCard(300));
        list.add(new HealCard(250));
        list.add(new HealCard(300));
        list.add(new EnergyCard(2));
        list.add(new EnergyCard(3));
        list.add(new ShieldCard(1000));
        list.add(new NegateCard());
        list.add(new StealCard());
        list.add(new ExtraCard());
        Collections.shuffle(list);
        cards.addAll(list);
    }

    public Card drawCard() {
        return cards.poll();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }
}