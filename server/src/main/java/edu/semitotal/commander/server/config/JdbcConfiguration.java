package edu.semitotal.commander.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.util.Arrays;

@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    private final ObjectMapper objectMapper;

    public JdbcConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    @NonNull
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
            new ObjectNodeToJsonConverter(objectMapper),
            new JsonToObjectNodeConverter(objectMapper)
        ));
    }

    @WritingConverter
    static class ObjectNodeToJsonConverter implements Converter<ObjectNode, PGobject> {
        private final ObjectMapper objectMapper;

        ObjectNodeToJsonConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        @SneakyThrows
        public PGobject convert(@NonNull ObjectNode source) {
            var pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(objectMapper.writeValueAsString(source));
            return pgObject;
        }
    }

    @ReadingConverter
    static class JsonToObjectNodeConverter implements Converter<PGobject, ObjectNode> {
        private final ObjectMapper objectMapper;

        JsonToObjectNodeConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        @SneakyThrows
        public ObjectNode convert(PGobject source) {
            return (ObjectNode) objectMapper.readTree(source.getValue());
        }
    }
}