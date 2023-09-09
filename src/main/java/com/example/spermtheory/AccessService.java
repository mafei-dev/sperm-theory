package com.example.spermtheory;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
@AllArgsConstructor
public class AccessService {

    private final AccessRepository accessRepository;

    public void check(Connection connection) throws SQLException {
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        connection.setAutoCommit(false);
        connection.setReadOnly(false);
        String key = "order_service:1.0.5";
        int add = accessRepository.add(connection, key);
        connection.commit();
        if (add != 0) {
            System.out.println("Got leader.");
            try {
                Thread.sleep(10_000);
                System.out.println("deleting leader.");
                int delete1 = accessRepository.delete(connection, key);
                System.out.println("leader completed.");
                connection.commit();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("becoming save");
            accessRepository.getAccessKeyCount(connection, key);
            System.out.println("salve done");
            connection.commit();
            connection.close();
        }
    }
}
