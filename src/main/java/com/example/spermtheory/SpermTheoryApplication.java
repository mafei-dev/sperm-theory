package com.example.spermtheory;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@AllArgsConstructor
@EnableRetry
public class SpermTheoryApplication implements CommandLineRunner {
    private final AccessService accessService;

    public static void main(String[] args) {
        SpringApplication.run(SpermTheoryApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Connection connection = DBConnection.getConnection();
            connections.add(connection);
        }
        System.out.println("stated");
        long start = System.currentTimeMillis();

        for (int i = 0; i < connections.size(); i++) {
            int finalI = i;
//            accessService.check(connections.get(finalI));
            new Thread(() -> {
                try {
                    accessService.check(connections.get(finalI));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        long end = System.currentTimeMillis();
        Thread.sleep(1000);
        System.out.println("Time: " + (end - start));
    }
}

