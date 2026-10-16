public class StealCard extends Card {
    public StealCard() {
        super("Steal", "ขโมยการ์ด", 3);
    }

    @Override
    public void use(Player user, Player target) {
        if (target.getEnergy() > 0) {
            target.gainEnergy(-1);
            user.gainEnergy(1);
        }
    }
}