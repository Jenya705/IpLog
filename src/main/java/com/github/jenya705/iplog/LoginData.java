package com.github.jenya705.iplog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Jenya705
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginData {

    private String nickname;
    private String ip;
    private long login;
    private long leave;

    public static LoginData from(ResultSet resultSet) throws SQLException {
        return new LoginData(
                resultSet.getString("nickname"),
                resultSet.getString("ip"),
                resultSet.getTimestamp("join_time").getTime(),
                resultSet.getTimestamp("leave_time").getTime()
        );
    }

}
