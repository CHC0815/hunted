package team.cophee.huntedplugin;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Util {
    static public void resetPlayerState(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20.0);
        player.setHealth(20.0);

        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        Objects.requireNonNull(player.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(1.0);
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
    }
}
