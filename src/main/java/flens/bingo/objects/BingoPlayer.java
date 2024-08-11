package flens.bingo.objects;

import flens.bingo.enums.PlayerState;
import org.bukkit.entity.Player;


public class BingoPlayer {

    private Player player;

    private PlayerState state;

    public BingoPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }


    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }
}
