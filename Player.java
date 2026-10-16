import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Player {
    private final String name;
    private final IntegerProperty hp;
    private final IntegerProperty energy;
    private final IntegerProperty shield;

    public Player(String name, int hp) {
        this.name = name;
        this.hp = new SimpleIntegerProperty(hp);
        this.energy = new SimpleIntegerProperty(0);
        this.shield = new SimpleIntegerProperty(0);
    }

    public void attack(Player opponent, int damage) {
        int finalDamage = Math.max(0, damage);
        if (opponent.shield.get() > 0) {
            int remaining = opponent.shield.get() - finalDamage;
            if (remaining > 0) {
                opponent.shield.set(remaining);
            } else {
                opponent.shield.set(0);
                opponent.hp.set(Math.max(0, opponent.hp.get() + remaining));
            }
        } else {
            opponent.hp.set(Math.max(0, opponent.hp.get() - finalDamage));
        }
    }

    public void heal(int amount) {
        hp.set(Math.max(0, hp.get() + amount));
    }

    public void gainEnergy(int amount) {
        energy.set(Math.max(0, energy.get() + amount));
    }

    public void setShield(int amount) {
        shield.set(Math.max(0, amount));
    }

    public void setHp(int hpValue) {
        hp.set(Math.max(0, hpValue));
    }

    public void setEnergy(int energyValue) {
        energy.set(Math.max(0, energyValue));
    }

    public void removeShield() {
        shield.set(0);
    }

    public String getName() { return name; }
    public IntegerProperty hpProperty() { return hp; }
    public IntegerProperty energyProperty() { return energy; }
    public IntegerProperty shieldProperty() { return shield; }
    public int getHp() { return hp.get(); }
    public int getEnergy() { return energy.get(); }
    public int getShield() { return shield.get(); }
}