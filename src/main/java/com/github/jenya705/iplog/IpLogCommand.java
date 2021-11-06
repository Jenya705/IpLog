package com.github.jenya705.iplog;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jenya705
 */
@AllArgsConstructor
public class IpLogCommand implements SimpleCommand {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final TextColor color = TextColor.color(127, 255, 212);

    private final DatabaseConnection databaseConnection;

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("iplog.command");
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 0) {
            source.sendMessage(buildComponent(
                    Component
                            .text("Command is not given")
                            .color(NamedTextColor.RED)
            ));
            return;
        }
        String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("nick")) {
            if (args.length <= 2) {
                source.sendMessage(buildComponent(
                        Component
                                .text("<nick> <page>")
                                .color(NamedTextColor.RED)
                ));
                return;
            }
            String nick = args[1];
            int page;
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                source.sendMessage(buildComponent(
                        Component
                                .text("Page is not integer")
                                .color(NamedTextColor.RED)
                ));
                return;
            }
            List<LoginData> logins = databaseConnection.getLoginByName(nick, page);
            Component buildComponent = buildComponent(Component
                    .text("Logins\n")
                    .color(NamedTextColor.AQUA)
            );
            for (LoginData loginData : logins) {
                buildComponent = buildComponent.append(buildLoginData(loginData));
            }
            source.sendMessage(buildComponent);
        }
        else if (subCommand.equalsIgnoreCase("ip")) {
            if (args.length <= 2) {
                source.sendMessage(buildComponent(
                        Component
                                .text("<ip> <page>")
                                .color(NamedTextColor.RED)
                ));
                return;
            }
            String ip = args[1];
            int page;
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                source.sendMessage(buildComponent(
                        Component
                                .text("Page is not integer")
                                .color(NamedTextColor.RED)
                ));
                return;
            }
            List<LoginData> logins = databaseConnection.getLoginByIp(ip, page);
            Component buildComponent = buildComponent(Component
                    .text("Logins\n")
                    .color(color)
            );
            for (LoginData loginData : logins) {
                buildComponent = buildComponent.append(buildLoginData(loginData));
            }
            source.sendMessage(buildComponent);
        }
        else if (subCommand.equalsIgnoreCase("accounts")) {
            if (args.length <= 1) {
                source.sendMessage(buildComponent(
                        Component
                                .text("<nickname>")
                                .color(NamedTextColor.RED)
                ));
                return;
            }
            String nickname = args[1];
            List<String> playerAccounts = databaseConnection.getPlayerAccounts(nickname);
            source.sendMessage(buildAccountsComponent(playerAccounts));
        }
    }

    private static Component buildComponent(Component component) {
        return Component
                .text("[IpLog] ")
                .color(color)
                .append(component);
    }

    private static Component buildLoginData(LoginData loginData) {
        return Component
                .text(loginData.getNickname() + " " + loginData.getIp() + " ")
                .color(NamedTextColor.YELLOW)
                .append(Component
                        .text(
                                unixTimeToString(loginData.getLogin()) + " - " +
                                        unixTimeToString(loginData.getLeave()) + "\n"
                        )
                        .color(NamedTextColor.AQUA)
                );
    }

    private static String unixTimeToString(long unix) {
        return dateFormat.format(new Date(unix));
    }

    public static Component buildAccountsComponent(List<String> playerAccounts) {
        return buildComponent(
                Component
                        .text("[")
                        .color(NamedTextColor.GRAY)
                        .append(Component
                                .text(String.join(", ", playerAccounts))
                                .color(color)
                                .append(Component
                                        .text("]")
                                        .color(NamedTextColor.GRAY)
                                )
                        )
        );
    }

}
