package live.turna.methyl.command;

import com.google.common.collect.ImmutableList;
import live.turna.methyl.MethylLoader;
import live.turna.methyl.util.MessageUtil;
import live.turna.methyl.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
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
            ComponentBuilder msg = MessageUtil.prepareNewComponent();

            if (player.getInventory().firstEmpty() < 0) {
                msg.append("你的背包满了，不考虑整理下吗？").color(ChatColor.RED);
                player.spigot().sendMessage(msg.create());
                return true;
            }

            if (args.length < 1) {
                msg.append("用法: ").color(ChatColor.GOLD)
                        .append("/skull ").reset()
                        .append("<玩家名> ").color(ChatColor.RED)
                        .append("[强制外置登录 (true, false)]").color(ChatColor.GRAY);
                player.spigot().sendMessage(msg.create());

                msg = MessageUtil.prepareNewComponent()
                        .append("头颅默认将会从正版用户中获取，如有和正版用户撞名字的情况可选择强制从外置登录获取").color(ChatColor.GOLD);
                player.spigot().sendMessage(msg.create());

                return true;
            }

            final String skullOwner = args[0];
            final boolean forceYggdrasil = args.length > 1 && Boolean.parseBoolean(args[1]);

            if (!NAME_PATTERN.matcher(skullOwner).matches()) {
                msg.append("玩家名有误，玩家名仅有英文字母、数字和下划线，且不得短于 3 字符，不得长于 16 字符").color(ChatColor.RED);
                player.spigot().sendMessage(msg.create());
                return true;
            }

            msg.append(forceYggdrasil ? "正在从外置登录获取 " : "正在获取 ").color(ChatColor.GOLD)
                    .append(skullOwner).color(ChatColor.RED)
                    .append(" 的头颅，请稍候...").color(ChatColor.GOLD);
            player.spigot().sendMessage(msg.create());

            // Using scheduler to prevent interruption of game while fetching skull information
            Bukkit.getScheduler().runTaskAsynchronously(MethylLoader.getInstance(), () -> {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(skullOwner + (forceYggdrasil ? "@yggdrasil" : "")));
                head.setItemMeta(meta);
                player.getInventory().addItem(head);

                ComponentBuilder completeMsg = MessageUtil.prepareNewComponent()
                        .append("头颅已到，请查收！").color(ChatColor.GOLD);
                player.spigot().sendMessage(completeMsg.create());
            });
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return Util.getPlayerNames();
            case 2:
                return args[1].isEmpty() ? BOOLEANS : StringUtil.copyPartialMatches(args[1], BOOLEANS, new ArrayList<>(BOOLEANS.size()));
            default:
                return ImmutableList.of();
        }
    }
}
