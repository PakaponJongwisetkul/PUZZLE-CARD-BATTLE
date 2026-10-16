public class NegateCard extends Card {
    public NegateCard() {
        super("Negate", "ยกเลิกการ์ดฝ่ายตรงข้าม", 2);
    }

    @Override
    public void use(Player user, Player target) {
        target.removeShield();
        if (target.getEnergy() > 0) {
            target.gainEnergy(-1);
        }
    }
}