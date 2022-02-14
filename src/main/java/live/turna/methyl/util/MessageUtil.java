package live.turna.methyl.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class MessageUtil {
    /**
     * A little helper for preparing message component
     *
     * @return New message component builder instance, with "[Methyl]" prefix
     */
    public static Component prepareNewComponent() {
        return Component.empty()
                .append(Component.text("[Methyl]", NamedTextColor.GRAY))
                .append(Component.text(" "));
    }

    public static Component prepareSimpleMessage(String msg, TextColor color) {
        return prepareNewComponent().append(Component.text(msg, color));
    }
}
