package live.turna.methyl.util;

import live.turna.methyl.MethylLoader;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<String> getPlayerNames() {
        Server server = MethylLoader.getInstance().getServer();
        List<String> result = new ArrayList<>();
        server.getOnlinePlayers().forEach(player -> result.add(player.getName()));
        return result;
    }
}
