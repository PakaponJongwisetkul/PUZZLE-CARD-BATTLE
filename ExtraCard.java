public class ExtraCard extends Card {
    public ExtraCard() {
        super("Extra", "จับคู่เพิ่ม 1 ครั้ง", 1);
    }

    @Override
    public void use(Player user, Player target) {
        // ExtraCard grants +1 bonus board-match turn (managed by GameFlow.extraMatchBonusThisTurn)
        user.gainEnergy(1);
    }
}