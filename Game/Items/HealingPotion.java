package byow.Game.Items;

import byow.Core.World;
import byow.Game.Game;
import byow.drawMethods.Point;

import java.awt.*;

public class HealingPotion extends Item {

    public static final int healingAmount = 3;

    public HealingPotion(World w, Point location) {
        super(w, '❤', Color.red, location, "Healing Potion");
    }

    @Override
    public boolean itemAbility(Game g) {
        playerHolding.healHealth(healingAmount);
        g.setMessage("Healed: " + healingAmount);
        return true;
    }
}
