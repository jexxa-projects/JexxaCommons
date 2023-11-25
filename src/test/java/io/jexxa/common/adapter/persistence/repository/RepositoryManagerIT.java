package io.jexxa.common.adapter.persistence.repository;


import io.jexxa.common.adapter.persistence.RepositoryConfig;
import io.jexxa.common.adapter.persistence.RepositoryManager;
import io.jexxa.common.adapter.persistence.repository.imdb.IMDBRepository;
import io.jexxa.common.adapter.persistence.repository.jdbc.JDBCKeyValueRepository;
import io.jexxa.common.facade.jdbc.JexxaEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;

import static io.jexxa.common.facade.TestConstants.INTEGRATION_TEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(INTEGRATION_TEST)
class RepositoryManagerIT
{

    @AfterEach
    void cleanup()
    {
        RepositoryManager.defaultSettings();
    }

    @Test
    void validateIMDBFallbackStrategy( )
    {
        //Arrange
        RepositoryManager.setDefaultStrategy(null);

        // Act
        var result =  RepositoryManager.getRepository(JexxaEntity.class,
                JexxaEntity::getKey,
                new Properties());

        //Assert
        assertNotNull(result);
        assertEquals(IMDBRepository.class.getName(), result.getClass().getName() );
    }

    @Test
    void validatePropertiesStrategy( )
    {
        //Arrange
        RepositoryManager.setDefaultStrategy(null);

        var postgresProperties = RepositoryConfig.postgresRepositoryConfig("jexxa");

        //Act
        var result = RepositoryManager.getRepository(JexxaEntity.class,
                JexxaEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(JDBCKeyValueRepository.class.getName(), result.getClass().getName() );
    }


    @Test
    void validateDefaultStrategyOverProperties()
    {
        //Arrange: Define a JDBC connection in properties but also set a default strategy
        var postgresProperties = RepositoryConfig.postgresRepositoryConfig("jexxa");

        //Act
        RepositoryManager.setDefaultStrategy(IMDBRepository.class);

        var result =  RepositoryManager.getRepository(JexxaEntity.class,
                JexxaEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(IMDBRepository.class.getName(), result.getClass().getName() );
    }

    @Test
    void validateSpecificStrategyOverDefaultStrategy()
    {
        //Arrange: Define a JDBC connection in properties but also set a default strategy
        var postgresProperties = RepositoryConfig.postgresRepositoryConfig("jexxa");

        RepositoryManager.setDefaultStrategy(IMDBRepository.class);  // Set a default strategy which is used in case no specific strategy is defines

        //Act
        RepositoryManager.setStrategy(JDBCKeyValueRepository.class, JexxaEntity.class );  // Set a specific strategy

        var result =  RepositoryManager.getRepository(JexxaEntity.class,
                JexxaEntity::getKey,
                postgresProperties);

        //Assert
        assertNotNull(result);
        assertEquals(JDBCKeyValueRepository.class.getName(), result.getClass().getName() );
    }
}