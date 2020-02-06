package com.example.web;

import com.example.domain.Task;
import com.example.domain.TaskNotFoundException;
import com.example.domain.TaskRepository;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;

/**
 *
 * @author hantsy
 *
 */
@Named("viewTaskAction")
@ViewScoped()
public class ViewTaskDetailsAction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //@Inject
    private static final Logger log = Logger.getLogger(ViewTaskDetailsAction.class.getName());

    @Inject
    private TaskRepository taskRepository;

    @NotNull
    private Long taskId;

    private Task task;

    public void init() {

        log.log(Level.INFO, "get task details of id @{0}", taskId);

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
