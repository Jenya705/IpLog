package com.github.jenya705.iplog;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * @author Jenya705
 */
@AllArgsConstructor
public class PlayerListener {

    private final DatabaseConnection databaseConnection;

    private final ProxyServer server;

    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        databaseConnection.login(
                player.getUsername(),
                player.getRemoteAddress().getHostString()
        );
        List<String> accounts = databaseConnection.getPlayerAccounts(player.getUsername());
        if (accounts.size() != 1) {
            Component message = IpLogCommand.buildAccountsComponent(accounts);
            server
                    .getAllPlayers()
                    .stream()
                    .filter(p -> p.hasPermission("iplog.accounts"))
                    .forEach(p -> p.sendMessage(message));
        }
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        Player player = event.getPlayer();
        databaseConnection.leave(player.getUsername());
    }

}
