package com.github.jenya705.iplog;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Jenya705
 */
@RequiredArgsConstructor
public class IpLogCommand implements SimpleCommand {

    private static final List<String> subCommands = List.of("nick", "ip", "accounts");
    private static final Component error = Component
            .empty()
            .color(NamedTextColor.RED)
            .append(Component.text("Arguments:"))
            .append(Component.newline())
            .append(Component.text("<nick | ip | accounts> <nick | ip>"));

    private final IpLog ipLog;

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("iplog");
    }

    @Override
    public void execute(Invocation invocation) {
        String[] arguments = invocation.arguments();
        if (arguments.length < 2) {
            invocation.source().sendMessage(error);
            return;
        }
        String identifier = arguments[1];
        invocation.source().sendMessage(
                switch (arguments[0].toLowerCase(Locale.ROOT)) {
                    case "nick" -> IpLog.buildMessage(
                            "%s ips".formatted(identifier),
                            ipLog.getDatabaseConnection().getIps(identifier)
                    );
                    case "ip" -> IpLog.buildMessage(
                            "Players with %s ip".formatted(identifier),
                            ipLog.getDatabaseConnection().getNames(identifier)
                    );
                    case "accounts" -> IpLog.buildMessage(
                            "Accounts of %s".formatted(identifier),
                            ipLog.getDatabaseConnection().getAccounts(identifier)
                    );
                    default -> error;
                }
        );
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] arguments = invocation.arguments();
        if (arguments.length <= 1) return subCommands;
        if (arguments.length == 2) {
            String startsWith = arguments[1].toLowerCase(Locale.ROOT);
            return ipLog
                    .getServer()
                    .getAllPlayers()
                    .stream()
                    .map(Player::getUsername)
                    .filter(it -> it.startsWith(startsWith))
                    .limit(50)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
