package flens.bingo;

import flens.bingo.commands.StartCommand;
import flens.bingo.listeners.MainListeners;
import flens.bingo.objects.Game;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class Bingo extends JavaPlugin {

    private static Bingo instance;
    private Game game;
    private final Random r = new Random();


    @Override
    public void onEnable() {
        instance = this;
        game = new Game();

        getServer().getPluginManager().registerEvents(new MainListeners(), this);

        //Création du jeu
        WorldCreator wc = new WorldCreator("world_the_end");
        wc.environment(World.Environment.THE_END);
        wc.createWorld();
        wc = new WorldCreator("world_nether");
        wc.environment(World.Environment.NETHER);
        wc.createWorld();
        wc = new WorldCreator("world");
        wc.environment(World.Environment.NORMAL);
        wc.createWorld();
        wc = new WorldCreator("lobby");
        wc.environment(World.Environment.NORMAL);
        wc.createWorld();

        World lobby = Bukkit.getWorld("lobby");
        lobby.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        lobby.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        World world = Bukkit.getWorld("world");
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, true);

        game.init();
        getCommand("start").setExecutor(new StartCommand());
        /*  Code permettant de check la grille tant que les GUI ne sont pas dev
        Bukkit.getScheduler().runTaskLater(Bingo.getInstance(), () -> {
            for (Player p : getServer().getOnlinePlayers()){
                for (Material m : getGame().getItemList()){
                    p.getInventory().addItem(new ItemStack(m));
                }
            }
        }, 30 * 20);

         */
    }

    public Game getGame() {
        return game;
    }

    public Random getR() {
        return r;
    }

    public static Bingo getInstance() {
        return instance;
    }
}
