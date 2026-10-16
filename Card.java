public abstract class Card {
    protected String name;
    protected String description;
    protected int cost;

    public Card(String name, String description, int cost) {
        this.name = name;
        this.description = description;
        this.cost = cost;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCost() { return cost; }

    public abstract void use(Player user, Player target);
}