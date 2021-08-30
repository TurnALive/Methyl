package live.turna.methyl.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class MessageUtil {
    /**
     * A little helper for preparing message component
     *
     * @return New message component builder instance, with "[Methyl]" prefix
     */
    public static ComponentBuilder prepareNewComponent() {
        ComponentBuilder builder = new ComponentBuilder();

        builder
                .append("[").color(ChatColor.GOLD)
                .append("Methyl").color(ChatColor.AQUA)
                .append("]").color(ChatColor.GOLD)
                .append(" ").reset();

        return builder;
    }
}
