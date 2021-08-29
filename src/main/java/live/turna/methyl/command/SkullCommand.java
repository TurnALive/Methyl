package live.turna.methyl.command;

import com.google.common.collect.ImmutableList;
import live.turna.methyl.MethylLoader;
import live.turna.methyl.util.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SkullCommand implements TabExecutor {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final List<String> BOOLEANS = ImmutableList.of("true", "false");

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;

            if (args.length < 1) {
                new MessageBuilder()
                        .append(ChatColor.GOLD, "用法: ")
                        .append(ChatColor.RESET, "/skull ")
                        .append(ChatColor.RED, "<玩家名> ")
                        .append(ChatColor.GRAY, "[强制外置登录 (true, false)]")
                        .send(player);
                new MessageBuilder()
                        .append(ChatColor.GOLD, "头颅默认将会从正版用户中获取，如有和正版用户撞名字的情况可选择强制从外置登录获取")
                        .send(player);
                return true;
            }

            final String skullOwner = args[0];
            final boolean forceYggdrasil = args.length > 1 && Boolean.parseBoolean(args[1]);

            if (!NAME_PATTERN.matcher(skullOwner).matches()) {
                new MessageBuilder()
                        .append(
                                ChatColor.RED,
                                "玩家名有误，玩家名仅有英文字母、数字和下划线，且不得短于 3 字符，不得长于 16 字符"
                        )
                        .send(player);
                return true;
            }

            new MessageBuilder()
                    .append(ChatColor.GOLD, forceYggdrasil ? "正在从外置登录获取 " : "正在获取 ")
                    .append(ChatColor.RED, skullOwner)
                    .append(ChatColor.GOLD, " 的头颅，请稍候...")
                    .send(player);

            // Using scheduler to prevent interruption of game while fetching skull information
            Bukkit.getScheduler().runTaskAsynchronously(MethylLoader.getInstance(), () -> {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(skullOwner + (forceYggdrasil ? "@yggdrasil" : "")));
                head.setItemMeta(meta);
                player.getInventory().addItem(head);

                new MessageBuilder().append(ChatColor.GOLD, "头颅已到，请查收！").send(player);
            });
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            if (args[1].isEmpty())
                return BOOLEANS;
            else
                return StringUtil.copyPartialMatches(args[1], BOOLEANS, new ArrayList<>(BOOLEANS.size()));
        } else if (args.length > 2) {
            return ImmutableList.of();
        }

        return null;
    }
}
