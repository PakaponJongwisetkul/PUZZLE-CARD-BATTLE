public class ShieldCard extends Card {
    private final int shieldValue;

    public ShieldCard(int shieldValue) {
        super("Shield", "สร้างโล่", 3);
        this.shieldValue = shieldValue;
    }

    @Override
    public void use(Player user, Player target) {
        user.setShield(Math.max(user.getShield(), shieldValue));
    }
}