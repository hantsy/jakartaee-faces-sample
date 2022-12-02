package com.example.web;

import com.example.domain.Task;
import com.example.domain.TaskNotFoundException;
import com.example.domain.TaskRepository;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hantsy
 *
 */
@Named("editTaskAction")
@ViewScoped()
public class EditTaskAction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //@Inject
    private static final Logger LOGGER = Logger.getLogger(EditTaskAction.class.getName());
    
    @Inject
    FacesContext facesContext;

    @Inject
    private TaskRepository taskRepository;

    private Long taskId;

    private Task task;

    public Task getTask() {
        return task;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void init() {
        LOGGER.log(Level.INFO, " get task of id @{0}", taskId);

        if (taskId == null) {
            task = new Task();
        } else {
            task= taskRepository.findOptionalById(taskId)
                    .orElseThrow(()-> new TaskNotFoundException(taskId));
        }

    }

    public String save() {
        LOGGER.log(Level.INFO, "saving task@{0}", task);
        if (this.task.getId() == null) {
            this.task = taskRepository.save(task);
        } else {
            this.task = taskRepository.update(task);
        }
        FacesMessage info = new FacesMessage( "Task is saved successfully!");
        facesContext.addMessage(null, info);

        return "/tasks.xhtml?faces-redirect=true";
    }

}
