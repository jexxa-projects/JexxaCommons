package io.jexxa.commons.component.persistence.repository.imdb;


import io.jexxa.commons.facade.TestConstants;
import io.jexxa.commons.facade.jdbc.JexxaEntity;
import io.jexxa.commons.facade.testapplication.JexxaValueObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Tag(TestConstants.UNIT_TEST)
class IMDBRepositoryTest
{
    private JexxaEntity aggregate;
    private IMDBRepository<JexxaEntity, JexxaValueObject> objectUnderTest;

    @BeforeEach
    void initTest()
    {
        //Arrange
        aggregate = JexxaEntity.create(new JexxaValueObject(42));
        objectUnderTest = new IMDBRepository<>(
                JexxaEntity.class,
                JexxaEntity::getKey,
                new Properties()
        );
        objectUnderTest.removeAll();
    }

    @Test
    void addAggregate()
    {
        //act
        objectUnderTest.add(aggregate);
        var result = objectUnderTest.get();

        //Assert
        assertEquals(aggregate, objectUnderTest.get(aggregate.getKey()).orElse(null));
        assertFalse(result.isEmpty());
    }

    @Test
    void addAggregateTwice()
    {
        //act
        objectUnderTest.add(aggregate);
        assertThrows(IllegalArgumentException.class, () ->objectUnderTest.add(aggregate));
    }


    @Test
    void removeAggregate()
    {
        //Arrange
        objectUnderTest.add(aggregate);

        //act
        objectUnderTest.remove( aggregate.getKey() );

        //Assert
        assertTrue(objectUnderTest.get().isEmpty());
    }

    @Test
    void differentConnections()
    {
        //Arrange
        objectUnderTest.add(aggregate);

        //act
        var newConnection = new IMDBRepository<>(
                JexxaEntity.class,
                JexxaEntity::getKey,
                new Properties()
        );

        //Assert that connections are different but refer to the same repository
        assertNotEquals(objectUnderTest, newConnection);
        assertFalse(objectUnderTest.get().isEmpty());
        assertFalse(newConnection.get().isEmpty());
    }

    @Test
    void differentRepositories()
    {
        //Arrange
        objectUnderTest.add(aggregate);

        //act
        var newConnection = new IMDBRepository<>(
                JexxaValueObject.class,
                JexxaValueObject::getValue,
                new Properties()
        );
        newConnection.removeAll();
        newConnection.add(new JexxaValueObject(42));

        //Assert that connections are different but refer to the same repository
        assertEquals(1, objectUnderTest.get().size());
        assertEquals(1, newConnection.get().size());

    }

    @Test
    void resetIMDBRepositories()
    {
        //Arrange
        objectUnderTest.add(aggregate);

        //act
        IMDBRepository.clear();

        //Assert that repository is empty
        assertTrue( objectUnderTest.get().isEmpty() );
    }

    @Test
    void reuseRepositoryAfterReset()
    {
        //Arrange
        objectUnderTest.add(aggregate);
        IMDBRepository.clear();

        //Act - Add entry and get new Connection
        objectUnderTest.add(aggregate);

        var newConnection = new IMDBRepository<>(
                JexxaEntity.class,
                JexxaEntity::getKey,
                new Properties()
        );

        //Assert
        assertEquals(1, objectUnderTest.get().size());
        assertEquals(1, newConnection.get().size());
    }


}
