package live.turna.methyl.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.ImmutableList;
import live.turna.methyl.MethylLoader;
import live.turna.methyl.util.MessageUtil;
import live.turna.methyl.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SkullCommand implements TabExecutor {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final List<String> BOOLEANS = ImmutableList.of("true", "false");

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (sender instanceof final Player player) {
            if (player.getInventory().firstEmpty() < 0) {
                player.sendMessage(MessageUtil.prepareSimpleMessage("你的背包满了，不考虑整理下吗？", NamedTextColor.RED));
                return true;
            }

            Component msg = MessageUtil.prepareNewComponent();

            if (args.length < 1) {
                msg = msg.append(Component.text("用法：", NamedTextColor.GOLD))
                        .append(Component.text("/skull "))
                        .append(Component.text("<玩家名> ", NamedTextColor.RED))
                        .append(Component.text("[强制外置登录 (true, false)]", NamedTextColor.GRAY));
                player.sendMessage(msg);

                player.sendMessage(MessageUtil.prepareSimpleMessage(
                        "头颅默认将会从正版用户中获取，如有和正版用户撞名字的情况可选择强制从外置登录获取",
                        NamedTextColor.GOLD
                ));

                return true;
            }

            final String skullOwner = args[0];
            final boolean forceYggdrasil = args.length > 1 && Boolean.parseBoolean(args[1]);

            if (!NAME_PATTERN.matcher(skullOwner).matches()) {
                player.sendMessage(MessageUtil.prepareSimpleMessage(
                        "玩家名有误，玩家名仅有英文字母、数字和下划线，且不得短于 3 字符，不得长于 16 字符",
                        NamedTextColor.RED
                ));
                return true;
            }

            msg = msg.append(Component.text(forceYggdrasil ? "正在从外置登录获取 " : "正在获取 ", NamedTextColor.GOLD))
                    .append(Component.text(skullOwner, NamedTextColor.RED))
                    .append(Component.text(" 的头颅，请稍候...", NamedTextColor.GOLD));
            player.sendMessage(msg);

            // Using scheduler to prevent interruption of game while fetching skull information
            Bukkit.getScheduler().runTaskAsynchronously(MethylLoader.getInstance(), () -> {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();

                PlayerProfile profile = Bukkit.createProfile(skullOwner + (forceYggdrasil ? "@yggdrasil" : ""));
                profile.complete();
                meta.setPlayerProfile(profile);

                head.setItemMeta(meta);
                player.getInventory().addItem(head);

                player.sendMessage(MessageUtil.prepareSimpleMessage("头颅已到，请查收！", NamedTextColor.GOLD));
            });
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String[] args) {
        return switch (args.length) {
            case 1 -> Util.getPlayerNames();
            case 2 -> args[1].isEmpty() ? BOOLEANS : StringUtil.copyPartialMatches(args[1], BOOLEANS, new ArrayList<>(BOOLEANS.size()));
            default -> ImmutableList.of();
        };
    }
}
