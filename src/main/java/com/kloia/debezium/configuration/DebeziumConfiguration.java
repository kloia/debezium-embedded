package com.kloia.debezium.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebeziumConfiguration {

    @Value("${author.datasource.host}")
    private String host;

    @Value("${author.datasource.port}")
    private String port;

    @Value("${author.datasource.database}")
    private String database;

    @Value("${author.datasource.username}")
    private String username;

    @Value("${author.datasource.password}")
    private String password;



    @Bean
    public io.debezium.config.Configuration customerConnector() {
        return io.debezium.config.Configuration.create()
                .with("name", "customer-connector")
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "/tmp/offsets.dat")
                .with("offset.flush.interval.ms", "60000")
                .with("database.hostname", host)
                .with("database.port", port)
                .with("database.user", username)
                .with("database.password", password)
                .with("database.dbname", database)
                .with("database.include.list", database)
                .with("include.schema.changes", "false")
                .with("database.server.name", "author-server")
                .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename", "/tmp/dbhistory.dat")
                .build();
    }

}
