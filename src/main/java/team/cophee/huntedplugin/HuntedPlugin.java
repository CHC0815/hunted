package team.cophee.huntedplugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.entity.PlayerDeathEvent;


public class HuntedPlugin extends JavaPlugin implements  Listener {

    HuntedGameMode hunted;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        hunted = new HuntedGameMode();
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(Util::resetPlayerState);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (hunted.running) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("The game mode Hunted is running at the moment. Please try again later :)"));
        }
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (hunted.running) {
            event.motd(Component.text("Server is running Hunted at the moment... " +  hunted.getAlive() + "/" + hunted.getTotal() + " alive"));
        } else {
            event.motd(Component.text("Hunted - Join :)"));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // if mode is not running disable pvp damage
        if (hunted.running) return;

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();

        player.setGameMode(GameMode.SPECTATOR);
        Util.resetPlayerState(player);
        hunted.alive.remove(player);

        if (killer == null || player == killer) {
            // if no killer or suicide return
            return;
        }

        if (killer == hunted.hunter) {
            // TODO: add points to hunter score
            killer.sendMessage(Component.text("You killed " + player.getName()));
        }

        if (player == hunted.hunter) {
            hunted.setHunter(killer);
            Bukkit.broadcast(Component.text(hunted.hunter + " is the new Hunter"));
        }

        if (hunted.isGameFinished()) {
            hunted.doGameEndMessage();
            hunted = new HuntedGameMode();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return switch (command.getName()) {
            case "start" -> hunted.startGame(sender);
            case "hunter" -> hunted.assignHunter(sender, args);
            default ->  {
                sender.sendMessage("Command " + command.getName() + " not found");
                yield false;
            }
        };
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Util.resetPlayerState(player);
    }


}
