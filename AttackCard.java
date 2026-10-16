public class AttackCard extends Card {
    private final int damage;

    public AttackCard(int damage) {
        super("Attack", "โจมตีศัตรู", 2);
        this.damage = damage;
    }

    @Override
    public void use(Player user, Player target) {
        user.attack(target, damage);
    }
}