package flens.bingo.objects;

import flens.bingo.Bingo;
import flens.bingo.enums.GameState;
import flens.bingo.enums.PlayerState;
import flens.bingo.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class Game {

    private final ArrayList<BingoPlayer> players = new ArrayList<>(); //List of BPlayer object for every player who are playing
    private final ArrayList<BingoPlayer> bPlayers = new ArrayList<>(); //List of BPlayer object for every player connected
    private final ArrayList<BingoPlayer> disconnectedPlayers = new ArrayList<>();

    private final ArrayList<Material> itemList = new ArrayList<>();
    private static final ArrayList<Material> all_items = new ArrayList<>(Arrays.stream(Material.values()).filter(
            Material::isItem).collect(Collectors.toList()));
    private static final ArrayList<Material> items_allowed = new ArrayList<>();
    private GameState state;

    private final HashMap<BingoPlayer, ArrayList<Boolean>> itemsGot = new HashMap<>();
    private final ArrayList<int[]> wins = new ArrayList<>();

    private int time = 0;

    public void init() {
        try {
            WorldUtils.PasteBarrier(Objects.requireNonNull(Bukkit.getWorld("lobby")), new Location(Bukkit.getWorld("lobby"), 0, 151, 0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.state = GameState.Open;
        initGrid();
    }

    public void initGrid(){
        items_allowed.clear();
        items_allowed.addAll(all_items);
        itemList.clear();
        for (int i = 0; i < 25; i++) {
            addItem(itemList, i);
        }
    }

    public void start() {
        for (BingoPlayer bingoPlayer : bPlayers) {
            ArrayList<Boolean> items = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                items.add(false);
            }
            itemsGot.put(bingoPlayer, items);
        }
        wins.add(new int[]{0, 1, 2, 3, 4});
        wins.add(new int[]{5, 6, 7, 8, 9});
        wins.add(new int[]{10, 11, 12, 13, 14});
        wins.add(new int[]{15, 16, 17, 18, 19});
        wins.add(new int[]{20, 21, 22, 23, 24});
        wins.add(new int[]{0, 5, 10, 15, 20});
        wins.add(new int[]{1, 6, 11, 16, 21});
        wins.add(new int[]{2, 7, 12, 17, 22});
        wins.add(new int[]{3, 8, 13, 18, 23});
        wins.add(new int[]{4, 9, 14, 19, 24});
        wins.add(new int[]{0, 6, 12, 18, 24});
        wins.add(new int[]{4, 8, 12, 16, 20});

        for (BingoPlayer bplayer : bPlayers) {
            Player player = bplayer.getPlayer();
            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
            broadcast("Téléportation de §7" + player.getName());
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(40);
            player.setExp(0);
            player.setLevel(0);
            player.setPlayerListName(player.getDisplayName());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 10000, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10 * 20, Integer.MAX_VALUE, false, false));
            players.add(bplayer);
            bplayer.setState(PlayerState.Playing);
        }
        Bukkit.getWorld("world").setTime(0);
        Bukkit.getWorld("world").setStorm(false);
        Bukkit.getWorld("world").setThundering(false);
        Bukkit.getWorld("world").setDifficulty(Difficulty.PEACEFUL);
        Bukkit.getWorld("world").setDifficulty(Difficulty.NORMAL);
        time = 0;

        Bukkit.getScheduler().runTaskLater(Bingo.getInstance(), () -> {
            this.state = GameState.Playing;
            Bukkit.getScheduler().runTaskTimer(Bingo.getInstance(), () -> {
                time++;
            }, 20, 20);
        }, 10 * 20);
    }

    public void addItem(ArrayList<Material> itemList, int i) {
        Material mat = items_allowed.get(Bingo.getInstance().getR().nextInt(items_allowed.size()));
        items_allowed.remove(mat);
        if (itemList.size() != i) {
            itemList.set(i, mat);
        } else {
            itemList.add(mat);
        }
    }

    public void getItem(BingoPlayer bingoPlayer, Material mat){
        if (itemsGot.get(bingoPlayer).get(getItemList().indexOf(mat))) return;

        itemsGot.get(bingoPlayer).set(getItemList().indexOf(mat), true);
        bingoPlayer.getPlayer().playSound(bingoPlayer.getPlayer(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);

        checkWin(bingoPlayer);
    }

    public void checkWin(BingoPlayer bingoPlayer) {
        ArrayList<Boolean> list = itemsGot.get(bingoPlayer);
        for (int[] a : wins) {
            boolean have_row = false;
            for (int i = 0; i < 5; i++) {
                if (list.get(a[i])) {
                    have_row = true;
                } else {
                    have_row = false;
                    break;
                }
            }
            if (have_row) {
                win(bingoPlayer);
                break;
            }
        }
    }

    public void win(BingoPlayer bingoPlayer) {
        broadcast(bingoPlayer.getPlayer().getDisplayName() + " a gagné la partie");
        this.state = GameState.Restarting;
        for (BingoPlayer bingoPlayer1 : bPlayers){
            bingoPlayer1.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    public void makePlayerJoin(Player player) {
        player.teleport(new Location(Bukkit.getWorld("lobby"), 0, 151, 0));
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.setHealth(20);
        player.setFoodLevel(40);
        broadcast(ChatColor.GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.GRAY + "] " + player.getDisplayName());
    }

    public void setSpectatorMode(Player player, String s) {
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(ChatColor.GREEN + s);
        BingoPlayer bPlayer = new BingoPlayer(player);
        bPlayer.setState(PlayerState.Spectator);
        bPlayers.add(bPlayer);
    }

    public void makePlayerLeave(BingoPlayer bPlayer) {
        broadcast(ChatColor.GRAY + "[" + ChatColor.RED + "-" + ChatColor.GRAY + "] " + bPlayer.getPlayer().getDisplayName());
        bPlayers.remove(bPlayer);
    }

    public BingoPlayer getBingoPlayer(Player player) {
        for (BingoPlayer bPlayer : bPlayers) {
            if (bPlayer.getPlayer().getDisplayName().equals(player.getDisplayName())) return bPlayer;
        }
        return null;
    }
    public void broadcast(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
    public ArrayList<Material> getItemList() {
        return itemList;
    }

    public GameState getState() {
        return state;
    }

    public ArrayList<BingoPlayer> getbPlayers() {
        return bPlayers;
    }

    public ArrayList<BingoPlayer> getPlayers() {
        return players;
    }

    public ArrayList<BingoPlayer> getDisconnectedPlayers() {
        return disconnectedPlayers;
    }
}
