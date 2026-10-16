public class EnergyCard extends Card {
    private final int energyGain;

    public EnergyCard(int energyGain) {
        super("Energy", "เพิ่มพลังงาน", 0);
        this.energyGain = energyGain;
    }

    @Override
    public void use(Player user, Player target) {
        user.gainEnergy(energyGain);
    }
}