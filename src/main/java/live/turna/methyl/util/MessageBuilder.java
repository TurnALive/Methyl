package live.turna.methyl.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Simple and stupid builder to send message more easier
 *
 * @author SpectreLake
 */
public class MessageBuilder {
    private final StringBuilder builder;

    /**
     * Constructs a new message builder instance
     */
    public MessageBuilder() {
        this.builder = new StringBuilder();

        this.appendPrefix();
    }

    /**
     * Appending "[Methyl]" prefix to message
     */
    private void appendPrefix() {
        this.builder.append(ChatColor.GOLD);
        this.builder.append("[");
        this.builder.append(ChatColor.AQUA);
        this.builder.append("Methyl");
        this.builder.append(ChatColor.GOLD);
        this.builder.append("] ");
    }

    /**
     * Append message text with no style
     *
     * @param message Message text
     */
    public MessageBuilder append(String message) {
        return this.append(ChatColor.RESET, message);
    }

    /**
     * Append message text with style
     *
     * @param style Text style
     * @param message Message text
     */
    public MessageBuilder append(ChatColor style, String message) {
        this.builder.append(style);
        this.builder.append(message);
        return this;
    }

    /**
     * Send message to player
     *
     * @param player Player that receives the message
     */
    public void send(Player player) {
        player.sendMessage(this.toString());
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }
}
