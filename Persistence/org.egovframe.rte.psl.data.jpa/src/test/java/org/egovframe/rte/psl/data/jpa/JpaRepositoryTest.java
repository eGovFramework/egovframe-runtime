package org.egovframe.rte.psl.data.jpa;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaConfiguration.class)
public class JpaRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaRepositoryTest.class);

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "sampleRepository")
    private SampleRepository repository;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public Sample makeSample() {
        Sample sample = new Sample();
        sample.setId(1);
        sample.setSampleId("SAMPLE-00001");
        sample.setName("Runtime");
        sample.setDescription("Runtime Tool");
        sample.setUseYn("Y");
        sample.setRegUser("eGov");
        return sample;
    }

    @Test
    public void crudTest() {
        Sample sample = makeSample();
        repository.save(sample);

        boolean exists = repository.existsById(1);
        assertTrue(exists);

        Optional<Sample> newValue = repository.findById(1);
        LOGGER.debug("### JpaRepositoryTest crudTest() id >>> {}", newValue.get().getId());
        LOGGER.debug("### JpaRepositoryTest crudTest() sample id >>> {}", newValue.get().getSampleId());
        LOGGER.debug("### JpaRepositoryTest crudTest() name >>> {}", newValue.get().getName());
        LOGGER.debug("### JpaRepositoryTest crudTest() description >>> {}", newValue.get().getDescription());
        LOGGER.debug("### JpaRepositoryTest crudTest() useYn >>> {}", newValue.get().getUseYn());
        LOGGER.debug("### JpaRepositoryTest crudTest() regUser >>> {}", newValue.get().getRegUser());

        assertNotNull(newValue.get().getId());
        assertEquals("SAMPLE-00001", newValue.get().getSampleId());
        assertEquals("Runtime", newValue.get().getName());
        assertEquals("Runtime Tool", newValue.get().getDescription());
        assertEquals("Y", newValue.get().getUseYn());
        assertEquals("eGov", newValue.get().getRegUser());
    }

}
