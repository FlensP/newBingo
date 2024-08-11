package flens.bingo.listeners;

import flens.bingo.Bingo;
import flens.bingo.enums.GameState;
import flens.bingo.objects.BingoPlayer;
import flens.bingo.objects.Game;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class MainListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        event.setJoinMessage(null);
        Game game = Bingo.getInstance().getGame();

        if (game.getState().equals(GameState.Open)) {
            game.getbPlayers().add(new BingoPlayer(player));
            game.makePlayerJoin(player);
        } else {

            boolean remove = false;
            for (BingoPlayer bPlayer : game.getDisconnectedPlayers()) {

                if (bPlayer.getPlayer().getName().equals(player.getName())) {
                    bPlayer.setPlayer(player);
                    game.getDisconnectedPlayers().remove(bPlayer);
                    bPlayer.getPlayer().setDisplayName(bPlayer.getPlayer().getName());
                    bPlayer.getPlayer().setPlayerListName(player.getDisplayName());
                    remove = true;
                    break;
                }
            }

            if (remove) {
                game.broadcast(player.getName() + "vient de se reconnecter.");
            } else {
                game.setSpectatorMode(player, "La partie a déjà commencé. Vous êtes spectateur");
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        event.setQuitMessage(null);
        Player player = event.getPlayer();
        Game game = Bingo.getInstance().getGame();
        if (game.getBingoPlayer(player) == null) return;
        BingoPlayer bPlayer = game.getBingoPlayer(player);

        if (!game.getState().equals(GameState.Playing)) {
            game.makePlayerLeave(bPlayer);
        } else {
            if (game.getPlayers().contains(game.getBingoPlayer(player))) {
                game.broadcast(ChatColor.GRAY + "[" + ChatColor.RED + "-" + ChatColor.GRAY + "] " + player.getDisplayName() + ChatColor.YELLOW + "a quitté la partie.");
                game.getDisconnectedPlayers().add(bPlayer);
            } else {
                game.makePlayerLeave(bPlayer);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (Bingo.getInstance().getGame().getState().equals(GameState.Playing)) return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (Bingo.getInstance().getGame().getState().equals(GameState.Playing)) return;
        event.setCancelled(true);
    }


    @EventHandler
    public void onDamagebyEntity(EntityDamageByEntityEvent event) {
        if (!Bingo.getInstance().getGame().getState().equals(GameState.Playing)) {
            event.setCancelled(true);
        } else {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                event.setCancelled(true);
            } else if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
                if (((Arrow) event.getDamager()).getShooter() instanceof Player) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (Bingo.getInstance().getGame().getState().equals(GameState.Playing)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        if (Bingo.getInstance().getGame().getState().equals(GameState.Playing)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (Bingo.getInstance().getGame().getState().equals(GameState.Playing)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }


    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Game game = Bingo.getInstance().getGame();
        if (!game.getState().equals(GameState.Playing)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Material mat = event.getItem().getItemStack().getType();
        if (!game.getItemList().contains(mat)) return;
        BingoPlayer bPlayer = game.getBingoPlayer(player);
        if (!game.getPlayers().contains(bPlayer)) return;

        game.getItem(bPlayer, mat);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Game game = Bingo.getInstance().getGame();
        if (!game.getState().equals(GameState.Playing)) return;
        Material mat = Objects.requireNonNull(event.getInventory().getResult()).getType();
        if (!game.getItemList().contains(mat)) return;
        Player player = (Player) event.getInventory().getHolder();
        BingoPlayer bPlayer = game.getBingoPlayer(player);
        if (!game.getPlayers().contains(bPlayer)) return;

        game.getItem(bPlayer, mat);
    }

    @EventHandler
    public void onBucketChange(PlayerBucketFillEvent event) {
        Game game = Bingo.getInstance().getGame();
        if (!game.getState().equals(GameState.Playing)) return;
        Material mat = event.getBucket();
        if (!game.getItemList().contains(mat)) return;
        Player player = event.getPlayer();
        BingoPlayer bPlayer = game.getBingoPlayer(player);
        if (!game.getPlayers().contains(bPlayer)) return;

        game.getItem(bPlayer, mat);
    }

    @EventHandler
    public void onInventoryChange(InventoryMoveItemEvent event) {
        Game game = Bingo.getInstance().getGame();
        if (!game.getState().equals(GameState.Playing)) return;
        if (!event.getDestination().getType().equals(InventoryType.PLAYER)) return;

        Material mat = event.getItem().getType();
        if (!game.getItemList().contains(mat)) return;
        Player player = (Player) event.getDestination().getViewers().get(0);
        BingoPlayer bPlayer = game.getBingoPlayer(player);
        if (!game.getPlayers().contains(bPlayer)) return;

        game.getItem(bPlayer, mat);
    }
}
