package edu.semitotal.commander.server.events;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("action_records")
public record ActionRecord(
    @Id Long id,
    String eventTypeName,
    ObjectNode payload,
    Instant timestamp
) {

    public ActionRecord(Class<?> eventType, ObjectNode payload, Instant timestamp) {
        this(null, eventType.getName(), payload, timestamp);
    }

}
