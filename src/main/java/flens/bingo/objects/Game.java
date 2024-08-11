package flens.bingo.objects;

import flens.bingo.Bingo;
import flens.bingo.enums.GameState;
import flens.bingo.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

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

    public void addItem(ArrayList<Material> itemList, int i) {
        Material mat = items_allowed.get(Bingo.getInstance().getR().nextInt(items_allowed.size()));
        items_allowed.remove(mat);
        if (itemList.size() != i) {
            itemList.set(i, mat);
        } else {
            itemList.add(mat);
        }
    }

    public ArrayList<Material> getItemList() {
        return itemList;
    }
}
