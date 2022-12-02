package com.example.web;

import com.example.domain.Task;
import com.example.domain.TaskNotFoundException;
import com.example.domain.TaskRepository;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author hantsy
 *
 */
@Named("viewTaskAction")
@ViewScoped()
public class ViewTaskDetailsAction implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ViewTaskDetailsAction.class.getName());

    @Inject
    private TaskRepository taskRepository;

    @NotNull
    private Long taskId;

    private Task task;

    public void init() {

        LOGGER.log(Level.INFO, "get task details of id @{0}", taskId);

        task = taskRepository.findOptionalById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Task getTask() {
        return task;
    }

}
