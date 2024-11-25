package team.cophee.huntedplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class HuntedGameMode {
    int minPlayers = 0;
    Player hunter;
    ArrayList<Player> hunted = new ArrayList<>();
    ArrayList<Player> alive = new ArrayList<>();

    final int hunterRegenerationAmplifier = 1;
    final double hunterBonusHealth = 10.0;

    boolean running = false;

    public int getTotal() {
        return hunted.size();
    }

    public int getAlive() {
        return alive.size();
    }

    private boolean startGame() {
        hunted = new ArrayList<>(Bukkit.getOnlinePlayers().stream().toList());
        hunted.remove(hunter);
        alive = hunted;
        running = true;

        setupWorldBorder();

        for(Player player: alive) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
        }
        hunter.setGameMode(GameMode.SURVIVAL);
        hunter.getInventory().clear();
        hunter.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
        return true;
    }

    public void setHunter(@NotNull Player player) {
        hunter = player;
        alive.remove(player);
        hunter.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, hunterRegenerationAmplifier, true, false));
        hunter.sendMessage(Component.text("Your are the hunter :)"));

        double baseHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(baseHealth + baseHealth);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue()); // Heal to max
    }

    public boolean isGameFinished() {
        return alive.isEmpty();
    }

    public void doGameEndMessage() {
        Bukkit.broadcast(Component.text(hunter + " won the game."));

        World world = Bukkit.getWorlds().getFirst();
        if (world == null) return;
        var spawnLocation = world.getSpawnLocation();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(spawnLocation);
        }
    }

    public boolean startGame(@NotNull CommandSender sender) {
        if (hunter == null) {
            sender.sendMessage("No hunter assigned. Use /hunter [PLAYER] to assign one.");
            return false;
        }
        if (hunted.size() < minPlayers) {
            sender.sendMessage("Not enough people to hunt.");
            return false;
        }

        return startGame();
    }
    public boolean assignHunter(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length <= 0) {
            sender.sendMessage("Missing argument Player");
            return false;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage("Player not found.");
            return false;
        }

        setHunter(player);
        sender.sendMessage("Assigned " + player.getName() + " to be the hunter.");
        return true;
    }

    private void setupWorldBorder() {
        World world = Bukkit.getWorlds().getFirst();
        if (world == null) return;
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(500);
        border.setWarningDistance(10);
    }
}
