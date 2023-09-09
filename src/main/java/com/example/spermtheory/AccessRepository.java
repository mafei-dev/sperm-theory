package com.example.spermtheory;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringJoiner;

@Component
public class AccessRepository {
    public int delete(Connection connection, String leaderAccessKey) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("DELETE FROM  ")
                .add("es_leader")
                .add("WHERE")
                .add("leader_access_key = ?")
        ;
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setString(1, leaderAccessKey);
            int i = statement.executeUpdate();
            System.out.println(Thread.currentThread() + ":deleted = " + i);
            return i;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int add(Connection connection, String leaderAccessKey) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("INSERT IGNORE INTO")
                .add("es_leader")
                .add("(")
                .add("leader_access_key")
                .add(")")
                .add("VALUES ")
                .add("(?)");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setString(1, leaderAccessKey);
            int i = statement.executeUpdate();
            System.out.println(Thread.currentThread() + ":i = " + i);
            return i;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Retryable(value = {RuntimeException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 3_000))
    public void getAccessKeyCount(Connection connection, String leaderAccessKey) {
        System.out.println("AccessRepository.getAccessKeyCount");
        StringJoiner sql = new StringJoiner(" ");
        sql.add("SELECT")
                .add("COUNT(leader_access_key) AS leader_access")
                .add("FROM")
                .add("es_leader")
                .add("WHERE")
                .add("leader_access_key = ?");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setString(1, leaderAccessKey);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getInt("leader_access") > 0) {
                    System.out.println("has key");
                    throw new RuntimeException();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int createTable(Connection connection, String tableName) {
        StringJoiner sql = new StringJoiner(" ");
        sql.add("CREATE TABLE IF NOT EXISTS")
                .add(tableName)
                .add("(id INT AUTO_INCREMENT PRIMARY KEY);");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int i = statement.executeUpdate();
            System.out.println(Thread.currentThread() + ":creating: = " + i);
            return i;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
