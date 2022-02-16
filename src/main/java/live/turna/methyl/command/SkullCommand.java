package live.turna.methyl.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.ImmutableList;
import live.turna.methyl.MethylLoader;
import live.turna.methyl.util.*;
import live.turna.methyl.util.exceptions.HttpStatusException;
import live.turna.methyl.util.exceptions.RateLimitException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
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
                msg = msg.append(Component.text("用法：", NamedTextColor.DARK_AQUA))
                        .append(Component.text("/skull ", NamedTextColor.GRAY, TextDecoration.ITALIC))
                        .append(Component.text("<玩家名> [强制外置登录 (true, false)]", NamedTextColor.GRAY));
                player.sendMessage(msg);

                player.sendMessage(MessageUtil.prepareSimpleMessage(
                        "头颅默认将会从正版用户中获取，如有和正版用户撞名字的情况可选择强制从外置登录获取",
                        NamedTextColor.DARK_AQUA
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

            msg = msg.append(Component.text(forceYggdrasil ? "正在从外置登录获取 " : "正在获取 ", NamedTextColor.DARK_AQUA))
                    .append(Component.text(skullOwner, NamedTextColor.GRAY, TextDecoration.ITALIC))
                    .append(Component.text(" 的头颅，请稍候...", NamedTextColor.DARK_AQUA));
            player.sendMessage(msg);

            // Using scheduler to prevent interruption of game while fetching skull information
            Bukkit.getScheduler().runTaskAsynchronously(MethylLoader.getInstance(), () -> {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();

                PlayerProfile profile;
                if (forceYggdrasil) {
                    String skinServer = MethylLoader.getInstance().getConfig().getString("skull.externalSkinServer");
                    if (skinServer == null || skinServer.isBlank() || skinServer.isEmpty()) {
                        player.sendMessage(MessageUtil.prepareSimpleMessage(
                                "未设置外置登录服务器，请联系服务器管理员确认配置是否正确",
                                NamedTextColor.RED
                        ));
                        return;
                    }

                    try {
                        UUID uuid = YggdrasilUtil.usernameToUUID(skinServer, skullOwner);
                        if (uuid == null) {
                            player.sendMessage(MessageUtil.prepareSimpleMessage(
                                    "外置登录服务器上找不到玩家 " + skullOwner + "，头颅获取失败！",
                                    NamedTextColor.RED
                            ));
                            return;
                        }

                        profile = Bukkit.createProfile(uuid, skullOwner);

                        String texture = YggdrasilUtil.getTextureFromUsername(skinServer, skullOwner);
                        if (texture != null) {
                            Set<ProfileProperty> properties = profile.getProperties();
                            properties.add(YggdrasilUtil.generateTextureFromMineSkin(texture));
                            profile.setProperties(properties);
                        } else {
                            player.sendMessage(MessageUtil.prepareSimpleMessage(
                                    "注意: " + skullOwner + " 并没有设置皮肤！",
                                    NamedTextColor.GOLD
                            ));
                        }
                    } catch (Exception e) {
                        if (e instanceof RuntimeException) {
                            player.sendMessage(MessageUtil.prepareSimpleMessage(
                                    "无法从外置登录获取 " + skullOwner + " 的头颅，错误信息作为参考：" + e.getMessage(),
                                    NamedTextColor.RED
                            ));
                        } else if (e instanceof RateLimitException rle) {
                            player.sendMessage(MessageUtil.prepareSimpleMessage(
                                    "由于 API 限制，暂时无法从外置登录获取 " + skullOwner + " 的头颅，请等待 " + rle.getDelay() + " 秒后重试",
                                    NamedTextColor.RED
                            ));
                        } else {
                            if (!(e instanceof HttpStatusException))
                                MethylLoader.getInstance().getLogger().log(Level.SEVERE, "Unexpected exception while fetching skull information", e);

                            player.sendMessage(MessageUtil.prepareSimpleMessage(
                                    "无法从外置登录获取 " + skullOwner + " 的头颅，验证服务器挂了吗？",
                                    NamedTextColor.RED
                            ));
                        }
                        return;
                    }
                } else {
                    profile = Bukkit.createProfile(skullOwner);
                    profile.complete();
                }
                meta.setPlayerProfile(profile);

                head.setItemMeta(meta);
                player.getInventory().addItem(head);

                player.sendMessage(MessageUtil.prepareSimpleMessage("头颅已到，请查收！", NamedTextColor.DARK_AQUA));
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
