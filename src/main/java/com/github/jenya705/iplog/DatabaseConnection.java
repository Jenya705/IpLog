package com.github.jenya705.iplog;

import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Jenya705
 */
public class DatabaseConnection {

    private static final int pageSize = 5;

    private final Connection connection;
    private final long delay;

    public DatabaseConnection(IpLogConfig config) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(
                String.format(
                        "jdbc:mysql://%s/%s",
                        config.get("host"),
                        config.get("database")
                ),
                config.getT("user"),
                config.getT("password")
        );
        update("create table if not exists logins (\n" +
                "    nickname text,\n" +
                "    ip text,\n" +
                "    join_time timestamp default NOW()\n," +
                "    leave_time timestamp default NOW()\n" +
                ");");
        delay = (int) config.get("delay") * 1000L;
    }

    @SneakyThrows
    public void update(String sql, Object... objects) {
        synchronized (connection) {
            if (objects.length == 0) {
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
            }
            else {
                PreparedStatement statement = connection.prepareStatement(sql);
                for (int i = 0; i < objects.length; ++i) {
                    statement.setObject(i + 1, objects[i]);
                }
                statement.executeUpdate();
            }
        }
    }

    @SneakyThrows
    public ResultSet query(String sql, Object... objects) {
        synchronized (connection) {
            if (objects.length == 0) {
                Statement statement = connection.createStatement();
                return statement.executeQuery(sql);
            }
            else {
                PreparedStatement statement = connection.prepareStatement(sql);
                for (int i = 0; i < objects.length; ++i) {
                    statement.setObject(i + 1, objects[i]);
                }
                return statement.executeQuery();
            }
        }
    }

    @SneakyThrows
    public List<LoginData> getLoginByName(String name, int page) {
        return getLogins(query(
                "select * from logins where nickname = ? order by join_time desc limit ?, ?;",
                name.toLowerCase(Locale.ROOT), pageSize * page, pageSize
        ));
    }

    @SneakyThrows
    public List<LoginData> getLoginByIp(String ip, int page) {
        return getLogins(query(
                "select * from logins where ip = ? order by join_time desc limit ?, ?;",
                ip, pageSize * page, pageSize
        ));
    }

    @SneakyThrows
    public LoginData getLastLoginByName(String name) {
        ResultSet resultSet = query(
                "select * from logins where nickname = ? order by join_time desc limit 1;",
                name.toLowerCase(Locale.ROOT)
        );
        if (!resultSet.next()) return null;
        return LoginData.from(resultSet);
    }

    @SneakyThrows
    public List<String> getPlayerIps(String name) {
        ResultSet resultSet = query(
                "select ip from logins where nickname = ? group by nickname;", name
        );
        List<String> array = new ArrayList<>();
        while (resultSet.next()) array.add(resultSet.getString(1));
        return array;
    }

    @SneakyThrows
    public List<String> getPlayerAccounts(String name) {
        List<String> ips = getPlayerIps(name);
        ResultSet resultSet = query(
                String.format(
                        "select nickname from logins where ip in (%s) group by nickname;",
                        ips
                                .stream()
                                .map(it -> "\"" + it + "\"")
                                .collect(Collectors.joining(","))
                )
        );
        List<String> nicknames = new ArrayList<>();
        while (resultSet.next()) nicknames.add(resultSet.getString(1));
        return nicknames;
    }

    @SneakyThrows
    public void login(String name, String ip) {
        LoginData lastLogin = getLastLoginByName(name);
        if (lastLogin != null &&
                lastLogin.getIp().equals(ip) &&
                System.currentTimeMillis() - lastLogin.getLeave() <= delay) {
            return;
        }
        update("insert into logins (nickname, ip) values (?, ?);", name.toLowerCase(Locale.ROOT), ip);
    }

    @SneakyThrows
    public void leave(String name) {
        LoginData lastLogin = getLastLoginByName(name);
        update(
                "update logins set leave_time = now() where nickname = ? and join_time = from_unixtime(?);",
                name.toLowerCase(Locale.ROOT), lastLogin.getLogin() / 1000
        );
    }

    private List<LoginData> getLogins(ResultSet resultSet) throws SQLException {
        List<LoginData> logins = new ArrayList<>();
        while (resultSet.next()) {
            logins.add(LoginData.from(resultSet));
        }
        return logins;
    }
}
