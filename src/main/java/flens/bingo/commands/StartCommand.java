package flens.bingo.commands;

import flens.bingo.Bingo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (!sender.hasPermission("bingo.admin")) return false;
        Bingo.getInstance().getGame().start();
        return false;
    }
}
