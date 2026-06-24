package ru.starashchuk.currency.exchange.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class DatabaseSource {
    private final HikariDataSource dataSource;

    public Connection getDbConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        return connection;
    }
}
