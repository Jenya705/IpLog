package com.github.jenya705.iplog;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Jenya705
 */
@RequiredArgsConstructor
public class JoinListener {

    @NotNull
    private final IpLog ipLog;

    @Subscribe
    public void onPlayerJoin(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        ipLog.getDatabaseConnection().insert(
                player.getUsername(),
                player.getRemoteAddress().getHostString()
        );
        List<String> accounts = ipLog.getDatabaseConnection().getAccounts(player.getUsername());
        if (!accounts.isEmpty()) {
            Component message = IpLog.buildMessage("%s accounts".formatted(player.getUsername()), accounts);
            ipLog.getServer().getAllPlayers()
                    .stream()
                    .filter(it -> it.hasPermission("iplog"))
                    .forEach(it -> it.sendMessage(message));
        }
    }

}
