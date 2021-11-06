package com.github.jenya705.iplog;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "iplog",
        name = "IpLog",
        version = BuildConstants.VERSION,
        description = "Player ip logging plugin",
        authors = {"Jenya705"}
)
public class IpLog {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    @Inject
    @DataDirectory
    private Path directoryPath;

    @Getter
    private IpLogConfig config;

    @Getter
    private DatabaseConnection databaseConnection;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws Exception {
        config = new IpLogConfig(directoryPath.toFile());
        databaseConnection = new DatabaseConnection(config);
        server.getEventManager().register(
                this, new PlayerListener(databaseConnection, server));
        server.getCommandManager().register(
                "iplog", new IpLogCommand(databaseConnection));
    }

}
