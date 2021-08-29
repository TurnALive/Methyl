package live.turna.methyl;

import live.turna.methyl.command.SkullCommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public final class MethylLoader extends JavaPlugin {
    private static MethylLoader INSTANCE;

    @Override
    public void onEnable() {
        // Plugin startup logic

        INSTANCE = this;

        this.registerCommand("skull", new SkullCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Register command to plugin
     *
     * @param cmd Command name, defined in plugin.yml
     * @param executor {@link TabExecutor} instance to executing plugin, with tab auto completion
     */
    private void registerCommand(String cmd, TabExecutor executor) {
        PluginCommand command = this.getCommand(cmd);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    /**
     * Get plugin instance for scheduler, logging, and etc.
     */
    public static MethylLoader getInstance() {
        return INSTANCE;
    }
}
