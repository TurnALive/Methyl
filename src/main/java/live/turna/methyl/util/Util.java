package live.turna.methyl.util;

import live.turna.methyl.MethylLoader;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {
    public static List<String> getPlayerNames() {
        Server server = MethylLoader.getInstance().getServer();
        List<String> result = new ArrayList<>();
        server.getOnlinePlayers().forEach(player -> result.add(player.getName()));
        return result;
    }

    // Reference: https://stackoverflow.com/a/19399768
    public static UUID stringNoDashesToUUID(String uuid) {
        return UUID.fromString(uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        ));
    }
}
