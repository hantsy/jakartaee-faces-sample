package com.example.it;

import com.example.domain.Task;
import com.example.domain.TaskRepository;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class TaskRepositoryTest {
    private static final Logger LOGGER = Logger.getLogger(TaskRepositoryTest.class.getName());

    @Deployment()
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addPackage(Task.class.getPackage())
                //Add JPA persistence configuration.
                //WARN: In a jar archive, persistence.xml should be put into /META-INF
                .addAsManifestResource("META-INF/persistence.xml", "persistence.xml");

        LOGGER.log(Level.INFO, "deployment unit: {0}", jar);

        return jar;
    }

    @Inject
    TaskRepository tasks;

    @PersistenceContext
    EntityManager em;

    Task saved;

    @BeforeEach
    public void setup() {
        saved = tasks.save(Task.of("test task", "desc of test task"));
    }

    @AfterEach
    public void teardown() {

    }

    @Test
    public void shouldCreated() {
        Task found = em.find(Task.class, saved.getId());
        assertEquals("test task", found.getName());
        assertEquals("desc of test task", found.getDescription());
    }
}
