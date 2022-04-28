package com.github.jenya705.iplog;

import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jenya705
 */
public class DatabaseConnection {

    private final Connection connection;

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
        update("""
                CREATE TABLE IF NOT EXISTS logins (
                    nickname VARCHAR(16),
                    ip VARCHAR(255),
                    UNIQUE(nickname, ip)
                );""");
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
    public List<String> getIps(String name) {
        return resultSetToStringList(query("SELECT ip FROM logins WHERE nickname = ?", name));
    }

    @SneakyThrows
    public List<String> getNames(String ip) {
        return resultSetToStringList(query("SELECT nickname FROM logins WHERE ip = ?", ip));
    }

    @SneakyThrows
    public List<String> getAccounts(String name) {
        return resultSetToStringList(
                query("""
                        SELECT DISTINCT nickname
                        FROM logins
                        WHERE ip IN (SELECT ip FROM logins WHERE nickname = ?) AND nickname != ?;
                        """, name, name)
        );
    }

    @SneakyThrows
    public void insert(String nickname, String ip) {
        if (nickname.length() > 16 || ip.length() > 255) return;
        update("INSERT IGNORE INTO logins (nickname, ip) VALUES (?, ?);", nickname, ip);
    }

    private static List<String> resultSetToStringList(ResultSet resultSet) throws SQLException {
        List<String> result = new ArrayList<>();
        while (resultSet.next()) result.add(resultSet.getString(1));
        return result;
    }

}
