package com.example;

import com.example.domain.Task;
import com.example.domain.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author hantsy
 */
// see: https://github.com/jakartaee/transactions/issues/235
// Not a problem in the GlassFish 8.0.0/WildFly 39+
@ApplicationScoped
@Transactional
public class Bootstrap {

    @Inject
    Logger LOG;

    @Inject
    TaskRepository taskRepository;

    public void init(@Observes Startup startup) {
        LOG.log(Level.INFO, "bootstraping application...");

        Stream.of("first", "second")
                .map(s -> {
                    Task task = new Task();
                    task.setName("My " + s + " task");
                    task.setDescription("The description of my " + s + " task");
                    task.setStatus(Task.Status.TODO);
                    return task;
                })
                .map(data -> taskRepository.save(data))
                .forEach(task -> LOG.log(Level.INFO, " task saved: {0}", new Object[]{task}));
    }
}
