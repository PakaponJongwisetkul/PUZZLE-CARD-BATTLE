public class HealCard extends Card {
    private final int healAmount;

    public HealCard(int healAmount) {
        super("Heal", "ฟื้นฟู HP", 2);
        this.healAmount = healAmount;
    }

    @Override
    public void use(Player user, Player target) {
        user.heal(healAmount);
    }
}