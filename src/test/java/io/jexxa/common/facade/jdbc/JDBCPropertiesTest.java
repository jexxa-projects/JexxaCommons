package io.jexxa.common.facade.jdbc;


import io.jexxa.common.facade.TestConstants;
import io.jexxa.common.adapter.persistence.repository.jdbc.JDBCKeyValueRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;

import static io.jexxa.common.facade.jdbc.JDBCProperties.JDBC_DRIVER;
import static io.jexxa.common.facade.jdbc.JDBCProperties.JDBC_URL;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag(TestConstants.UNIT_TEST)
@Execution(ExecutionMode.CONCURRENT)
class JDBCPropertiesTest
{
    @Test
    void invalidProperties()
    {
        //1.Assert missing properties
        var emptyProperties = new Properties();
        assertThrows(IllegalArgumentException.class, () -> new JDBCKeyValueRepository<>(
                JexxaEntity.class,
                JexxaEntity::getKey,
                emptyProperties
        ));

        //2.Arrange invalid properties: Invalid Driver
        Properties propertiesInvalidDriver = new Properties();
        propertiesInvalidDriver.put(JDBC_DRIVER, "org.unknown.Driver");
        propertiesInvalidDriver.put(JDBC_URL, "jdbc:postgresql://localhost:5432/jexxa");

        //2.Assert invalid properties: Invalid Driver
        assertThrows(IllegalArgumentException.class, () -> new JDBCKeyValueRepository<>(
                JexxaEntity.class,
                JexxaEntity::getKey,
                propertiesInvalidDriver
        ));

        //3. Arrange invalid properties: Invalid URL
        Properties propertiesInvalidURL = new Properties();
        propertiesInvalidURL.put(JDBC_DRIVER, "org.postgresql.Driver");
        propertiesInvalidURL.put(JDBC_URL, "jdbc:unknown://localhost:5432/jexxa");

        //3.Assert invalid properties: Invalid URL
        assertThrows(IllegalArgumentException.class, () -> new JDBCKeyValueRepository<>(
                JexxaEntity.class,
                JexxaEntity::getKey,
                propertiesInvalidURL
        ));
    }
}