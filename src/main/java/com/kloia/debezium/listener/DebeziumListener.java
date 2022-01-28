package com.kloia.debezium.listener;

import com.kloia.debezium.service.AuthorService;
import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.debezium.data.Envelope.*;
import static io.debezium.data.Envelope.FieldName.AFTER;
import static io.debezium.data.Envelope.FieldName.BEFORE;
import static io.debezium.data.Envelope.FieldName.OPERATION;
import static java.util.stream.Collectors.toMap;

@Component
public class DebeziumListener {
    private final Executor executor = Executors.newSingleThreadExecutor();

    private AuthorService authorService;
    private DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    public DebeziumListener(Configuration authorConnectorConfiguration, AuthorService authorService) {

        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(authorConnectorConfiguration.asProperties())
                .notifying(this::handleChangeEvent)
                .build();

        this.authorService = authorService;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordChangeEvent.record();
        Struct sourceRecordChangeValue = (Struct) sourceRecord.value();

        if (sourceRecordChangeValue != null) {
            Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));

            if (operation != Operation.READ) {
                String record = operation == Operation.DELETE ? BEFORE : AFTER;
                Struct struct = (Struct) sourceRecordChangeValue.get(record);
                Map<String, String> payload = struct.schema().fields().stream()
                        .map(Field::name)
                        .filter(fieldName -> struct.get(fieldName) != null)
                        .map(fieldName -> Pair.of(fieldName, String.valueOf(struct.get(fieldName))))
                        .collect(toMap(Pair::getKey, Pair::getValue));

                this.authorService.replicateData(payload, operation);
            }
        }
    }

    @PostConstruct
    private void start() {
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }
    }
}
