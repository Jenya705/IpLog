package com.github.jenya705.iplog;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.nio.file.Path;
import java.util.List;

@Plugin(
        id = "iplog",
        name = "IpLog",
        version = BuildConstants.VERSION,
        description = "Player ip logging plugin",
        authors = {"Jenya705"}
)
@Getter
public class IpLog {

    private static final TextColor primaryColor = NamedTextColor.GRAY;
    private static final TextColor secondaryColor = NamedTextColor.BLUE;

    private static final Component ipLog = Component
            .text("[IpLog] ")
            .color(primaryColor);

    private final ProxyServer server;
    private final IpLogConfig config;
    private final DatabaseConnection databaseConnection;

    @Inject
    public IpLog(@DataDirectory Path directoryPath, ProxyServer server) throws Exception {
        this.server = server;
        config = new IpLogConfig(directoryPath.toFile());
        databaseConnection = new DatabaseConnection(config);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getEventManager().register(this, new JoinListener(this));
        server.getCommandManager().register("iplog", new IpLogCommand(this));
    }

    public static Component buildMessage(String title, List<String> elements) {
        return Component.empty()
                .append(ipLog)
                .append(Component
                        .text(title)
                        .color(primaryColor)
                )
                .append(Component.newline())
                .append(ipLog)
                .append(Component
                        .text(elements.isEmpty() ?
                                "None":
                                String.join(" ", elements)
                        )
                        .color(secondaryColor)
                );
    }

}
