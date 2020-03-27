# Building a Jakarta Server Faces application

I have created a [Jakarta EE 8 starter](https://github.com/hantsy/jakartaee8-starter) to help you to start a simple Jakarta EE 8 application in seconds. In this post, I will reuse the codebase of  [Jakarta EE starter](https://github.com/hantsy/jakartaee8-starter), and create a simple Kanban board like web application with JSF , EJB,  CDI, JPA. etc.

I assume you have read  my former posts for [Jakarta EE 8 starter](https://github.com/hantsy/jakartaee8-starter).

* [Kickstart a Jakarta EE 8 Application](https://medium.com/@hantsy/kickstart-a-jakarta-ee-8-application-d1b6ff32213b)
* [Testing Jakarta EE 8 Applications](https://medium.com/swlh/testing-jakarta-ee-8-applications-9ca250da20e3)
* [Put your Jakarta EE 8 applications to production](https://medium.com/@hantsy/put-your-jakarta-ee-8-applications-to-production-77756d1967bf)

In this application, we use a  real database database to store data instead of dummy codes in [Jakarta EE 8 starter](https://github.com/hantsy/jakartaee8-starter) , JPA is used to persist data, EJB is responsible for handling transaction, and JSF is selected to present the Web UI pages. 

Firstly let's setup Jakarta Persistence.

## Setup Jakarta Persistence

Create a */src/main/resources/META-INF/persistence.xml* configuration.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2" 
             xmlns="http://xmlns.jcp.org/xml/ns/persistence" 
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
  <persistence-unit name="defaultPU" transaction-type="JTA">
    <jta-data-source>java:comp/DefaultDataSource</jta-data-source>
    <exclude-unlisted-classes>false</exclude-unlisted-classes>
    <properties>
      <property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
    </properties>
  </persistence-unit>
</persistence>
```

Since Java EE 7, a default `DataSource` should be available at runtime in application servers, this improves the portability of your application source codes between different servers.

* Set `transaction-type` to JTA and use JTA transaction.
* You have to set a `jta-data-source` when using a JTA transaction.
*  Set property `javax.persistence.schema-generation.database.action` to `drop-and-create` to drop and create database at the application startup.

This application only includes a simple `Task` domain object. Create a simple JPA `@Entity` to present the task entity persisted in the database.

```java
@Entity
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    public static enum Status {
        TODO, DOING, DONE;
    }

    public Task() {
    }

    public static Task of(String name, String description) {
        final Task task = new Task();
        task.setName(name);
        task.setDescription(description);

        return task;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = TODO;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    ...
}
```

In the above code, 

* A JPA Entity must be annotated with a `@Entity` annotation.
* It should includes an identity field annotated with `@Id` (or `@IdClass`).
* The entity class should has a default none-arguments constructor.

Next, we will use EJB  `@Stateless` bean to perform CRUD operations.

## Create a TaskRepository bean

Create a class named `TaskRepository` and annotated it with `@Stateless` like following.

```java
@Stateless
public class TaskRepository {

    @PersistenceContext
    EntityManager em;

    public Task findById(Long id) {
        Task task = em.find(Task.class, id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }

        return task;
    }

    public Optional<Task> findOptionalById(Long id) {
        Task task = em.find(Task.class, id);
        return Optional.ofNullable(task);
    }

    public List<Task> findByStatus(Task.Status status) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();

        CriteriaQuery<Task> q = cb.createQuery(Task.class);
        Root<Task> c = q.from(Task.class);
        
        if (null != status) {
            q.where(cb.equal(c.get(Task_.status), status));
        }

        TypedQuery<Task> query = em.createQuery(q);

        return query.getResultList();
    }

    public Task save(Task task) {
        em.persist(task);

        return task;
    }

    public Task update(Task task) {
        return em.merge(task);
    }

    public void delete(Task task) {
        task = em.merge(task);
        em.remove(task);
    }

    public void deleteById(Long id) {
        Task task = this.findById(id);
        em.remove(task);
    }

}
```

In the above codes, 

* `@Stateless` means it is a EJB stateless bean, and it gets Transaction support automaticially.
* A `EntityManager` can be injected by `@PersistenceContext`. 
* In the `findByStatus` method, it uses JPA Criteria API  to perform type-safe queries instead of literal queries.

The entity metadata classes generation is dependent on the background Persistence provider, Hibernate and EclipseLinks provide APT tooling to generate them at compile time.

Add the following dependency in *pom.xml*.

```xml
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>org.eclipse.persistence.jpa.modelgen.processor</artifactId>
    <version>${eclipselink.version}</version>
    <scope>provided</scope>
</dependency>
```

When compiling the source codes by Maven command line, it will be picked up by maven-compiler-plugin and generate the metadata classes for you.

For most of IDEs, such as Eclipse and IDEA, you should enable APT  manually in the settings of IDEs. Then it will generate the entity metadata classes automatically.

## Initializing Sample Data

EJB provides a `Singlton` bean which can be used for initializing sample data as expected.

```java
@Startup
@Singleton
public class Bootstrap {

    @Inject
    Logger LOG;

    @Inject
    TaskRepository taskRepository;

    @PostConstruct
    public void init() {
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
                .collect(Collectors.toList())
                .forEach(task -> LOG.log(Level.INFO, " task saved: {0}", new Object[]{task}));
    }
}
```

The above `Bootstrap` bean is marked as `@Startup`, which means this bean will be initialized as soon as possible when EJB container is ready.  And `@Startup` must be used with EJB `@Singletone`.

Next, let's move to the UI work. Firstly, let's enable JSF 2.3 in your Jakarta EE 8 applications.

## Enabling JSF 2.3

Unlike former JSF, JSF 2.3 brought a new `FacesConfiguration` annotation to enable the features in JSF 2.3.

Create a class annotated with `@FacesConfig`,  set attribute `version` to `JSF_2_3`.

```java
@FacesConfig(
	// Activates CDI build-in beans
	version = JSF_2_3 
)
public class FacesConfigurationBean {

}
```

In the home page of the application, it displays tasks by status groups, including TODO, DOING, DONE.  

When JSF is enabled, it supports Facelets template engine to render Web UI.

## Facelets layout and templates

For common use cases, we splits a page into several parts, such as  header, content, footer etc.

In the *src/main/webapp/WEB-INF/views*, create a  *template.xhtml*.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:h="http://xmlns.jcp.org/jsf/html">
    <f:view contentType="text/html" encoding="UTF-8">
        <ui:insert name="metadata"></ui:insert>
        <h:head>
            <title>Taskboard - A JakartaEE Faces Sample</title>
            <!-- Required meta tags -->
            <meta charset="utf-8"></meta>
            <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"></meta>
            <!-- styles -->
            <link rel="stylesheet"
                  href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"
                  integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh"
                  crossorigin="anonymous"/>
            <link href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"
                  rel="stylesheet"
                  integrity="sha384-wvfXpqpZZVQGK6TAh5PVlGOfQNHSoD2xbE+QkPxCAFlNEevoEH3Sl0sibVcOQVnN"
                  crossorigin="anonymous"/>
            <link href="#{request.contextPath}/resources/css/main.css" rel="stylesheet"/>
            <!--            <h:outputStylesheet library="css" name="main.css"></h:outputStylesheet>-->
            <ui:insert name="headIncludes"></ui:insert>
        </h:head>

        <h:body styleClass="d-flex flex-column h-100">
            <h:panelGroup layout="block" styleClass="header">
                <ui:include src="/WEB-INF/layout/header.xhtml"/>
            </h:panelGroup>

            <main role="main" class="flex-shrink-0">
                <div class="container">
                    <ui:include src="/WEB-INF/layout/alert.xhtml"/>
                    <h:panelGroup layout="block" styleClass="page-header">
                        <h1>
                            <ui:insert name="pageTitle"></ui:insert>
                        </h1>
                    </h:panelGroup>
                    <ui:insert name="content"/>
                </div>
            </main>

            <ui:include src="/WEB-INF/layout/footer.xhtml"/>
            <!-- Optional JavaScript -->
            <!-- jQuery first, then Popper.js, then Bootstrap JS -->
            <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js" integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous">
                /** stop autoclosing **/
            </script>
            <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous">
                /** stop autoclosing **/
            </script>
            <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous">
                /** stop autoclosing **/
            </script>
            <ui:insert name="bodyIncludes"></ui:insert>
        </h:body>
    </f:view>
</html>
```

In above codes,

* We use [Bootstrap](https://getbootstrap.com/) and [Font Awesome](https://fontawesome.com/) to beautify the pages.

* We use `ui:include` to include the predefined facelets `composition` view. 

* There are some `ui:insert` tags where live some room to the certain facelets templates to specify the content.

Facelets follow the `Composition` pattern, it is easy to replace the specified content with Facelets composition view.

Check the file contents of *header.xhtml*, *footer.xhtml* and *alert.xhtml* yourself from the [source codes](https://github.com/hantsy/jakartaee-faces-sample). 



## Displaying Tasks

Let's have a look at the home page which displays the tasks in different status swimming lanes.

```xml
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
                xmlns:f="http://xmlns.jcp.org/jsf/core"
                xmlns:h="http://xmlns.jcp.org/jsf/html"
                xmlns:c="http://java.sun.com/jsp/jstl/core"
                template="/WEB-INF/layout/template.xhtml">
    <ui:define name="metadata">
        <f:metadata>
            <f:viewAction action="#{taskHome.init()}" />
        </f:metadata>
    </ui:define>
    <ui:define name="pageTitle"> TASK LIST</ui:define>
    <ui:define name="content">
        <h:form>
            <div class="row">
                <div class="col-md-4 col-xs-12">
                    <div class="card" >
                        <!-- Default panel contents -->
                        <div class="card-header">
                            <span class="fa fa-list-alt" aria-hidden="true"></span>
                            TODO
                        </div>
                        <div class="card-body">
                            <p>Tasks newly added in the backlog.</p>
                        </div>

                        <!-- List group -->
                        <ui:fragment rendered="#{not empty taskHome.todotasks}">

                            <ul id="todotasks" class="list-group">
                                <ui:repeat var="task" value="#{taskHome.todotasks}">
                                    <li class="list-group-item">
                                        <h4>
                                            <span>##{task.id} #{task.name}</span> 
                                            <span class="pull-right">
                                                <h:link outcome="/details.xhtml">
                                                    <f:param name="id" value="#{task.id}"></f:param>
                                                    <span class="fa fa-file-text-o" aria-hidden="true"></span>
                                                </h:link> 
                                                <h:link outcome="/form.xhtml">
                                                    <f:param name="id" value="#{task.id}"></f:param>
                                                    <span class="fa fa-pencil" aria-hidden="true"></span>
                                                </h:link>
                                            </span>
                                        </h4>
                                        <p>#{task.description}</p>
                                        <p>
                                            <h:commandLink action="#{taskHome.markTaskDoing(task.id)}"
                                                           styleClass="btn btn-sm btn-success">
                                                <span class="fa fa-play" aria-hidden="true"></span>START								
                                            </h:commandLink>
                                        </p>
                                    </li>
                                </ui:repeat>
                            </ul>

                        </ui:fragment>
                    </div>
                </div>

                <div id="doingtasks" class="col-md-4 col-xs-12">
                    <div class="card">
                        <!-- Default panel contents -->
                        <div class="card-header">
                            <span class="fa fa-hourglass-start" aria-hidden="true"></span>
                            WORK IN PROGRESS
                        </div>
                        <div class="card-body">
                            <p>Tasks had been assigned and started.</p>
                        </div>

                        <!-- List group -->
                        <ui:fragment  rendered="#{not empty taskHome.doingtasks}">
                            <ul id="doingtasks" class="list-group">
                                <ui:repeat var="task" value="#{taskHome.doingtasks}">
                                    <li class="list-group-item">
                                        <h4>##{task.id} #{task.name}</h4>
                                        <p>#{task.description}</p>
                                        <p>
                                            <h:commandLink action="#{taskHome.markTaskDone(task.id)}"
                                                           styleClass="btn btn-sm btn-info">
                                                <span class="fa fa-check" aria-hidden="true"></span>
                                                DONE
                                            </h:commandLink>
                                        </p>
                                    </li>
                                </ui:repeat>
                            </ul>
                        </ui:fragment>
                    </div>
                </div>
                <div id="donetasks" class="col-md-4 col-xs-12">
                    <div class="card">
                        <!-- Default panel contents -->
                        <div class="card-header">
                            <span class="fa fa-check-circle-o" aria-hidden="true"></span>
                            DONE
                        </div>
                        <div class="card-body">
                            <p>Tasks had been done successfully.</p>
                        </div>

                        <!-- List group -->
                        <ui:fragment rendered="#{not empty taskHome.donetasks}">
                            <ul id="donetasks" class="list-group">
                                <ui:repeat var="task" value="#{taskHome.donetasks}">
                                    <li class="list-group-item">
                                        <h4>##{task.id} #{task.name}</h4>
                                        <p>#{task.description}</p>
                                        <p>

                                            <h:commandLink action="#{taskHome.deleteTask(task.id)}"
                                                           styleClass="btn btn-sm btn-danger">
                                                <span class="fa fa-trash" aria-hidden="true"></span> DELETE
                                            </h:commandLink>
                                        </p>
                                    </li>
                                </ui:repeat>
                            </ul>
                        </ui:fragment>
                    </div>
                </div>
            </div>
        </h:form>
    </ui:define>
</ui:composition>
```



In the above codes, 

* The root element `ui:composition` specifies `template` attribute to use `/WEB-INF/layout/template.xhtml` as template layout.
* A series of `ui:define` content fragment will replace the `ui:insert` in the *template.xhtml* at rendering time.

> More details about the Facelets taglibs, check the [Jakarta Server Faces 2.3.2 VDL Documentation](https://jakarta.ee/specifications/faces/2.3/vdldoc/) .


## Baking the backend bean



To display the tasks, it requires a backend bean to serve the data for `tasks.xhtml` template.

Create a CDI bean.

```java
@Named("taskHome")
@ViewScoped()
public class TaskHome implements Serializable {

    //@Inject
    private static final Logger LOGGER = Logger.getLogger(TaskHome.class.getName());
    
    @Inject 
    FacesContext facesContext;

    @Inject
    private TaskRepository taskRepository;

    private List<TaskDetails> todotasks = new ArrayList<>();

    private List<TaskDetails> doingtasks = new ArrayList<>();

    private List<TaskDetails> donetasks = new ArrayList<>();

    public List<TaskDetails> getTodotasks() {
        return todotasks;
    }

    public List<TaskDetails> getDoingtasks() {
        return doingtasks;
    }

    public List<TaskDetails> getDonetasks() {
        return donetasks;
    }

    public void init() {
        LOGGER.log(Level.INFO, "initalizing TaskHome...");
        retrieveAllTasks();
    }

    private void retrieveAllTasks() {
        LOGGER.log(Level.INFO, "retriveing all tasks...");
        this.todotasks = findTasksByStatus(Task.Status.TODO);
        this.doingtasks = findTasksByStatus(Task.Status.DOING);
        this.donetasks = findTasksByStatus(Task.Status.DONE);
    }

    private List<TaskDetails> findTasksByStatus(Task.Status status) {
        List<TaskDetails> taskList = new ArrayList<>();
        List<Task> tasks = taskRepository.findByStatus(status);

        tasks.stream().map((task) -> {
            TaskDetails details = new TaskDetails();
            details.setId(task.getId());
            details.setName(task.getName());
            details.setDescription(task.getDescription());
            details.setCreatedDate(task.getCreatedDate());
            details.setLastModifiedDate(task.getLastModifiedDate());
            return details;
        }).forEach((details) -> {
            taskList.add(details);
        });

        return taskList;
    }

    public void deleteTask(Long id) {

        LOGGER.log(Level.INFO, "delete task of id@{0}", id);

        Task  task= taskRepository.findOptionalById(id)
                    .orElseThrow(()-> new TaskNotFoundException(id));
        taskRepository.delete(task);

        // retrieve all tasks
        retrieveAllTasks();

        FacesMessage deleteInfo = new FacesMessage(FacesMessage.SEVERITY_WARN, "Task is deleted!", "Task is deleted!");
        facesContext.addMessage(null, deleteInfo);
    }

    public void markTaskDoing(Long id) {
        LOGGER.log(Level.INFO, "changing task DONG @{0}", id);

        Task task = taskRepository.findOptionalById(id)
                    .orElseThrow(()-> new TaskNotFoundException(id));
        task.setStatus(Task.Status.DOING);
        taskRepository.update(task);

        // retrieve all tasks
        retrieveAllTasks();
    }

    public void markTaskDone(Long id) {
        LOGGER.log(Level.INFO, "changing task DONE @{0}", id);

        Task task = taskRepository.findOptionalById(id)
                    .orElseThrow(()-> new TaskNotFoundException(id));
        task.setStatus(Task.Status.DONE);
        taskRepository.update(task);

        // retrieve all tasks
        retrieveAllTasks();
    }

}
```

In the above codes,

* The `ViewScoped` is imported from package `javax.faces.view`, which is a CDI compatible scope. All  legacy JSF scopes are not recommended in JSF 2.3, you should consider use standard CDI scopes instead. 

* The bean is annotated with `@Named`, thus in the template file, the bean can be accessed via Expressing Language.

* In JSF 2.3, a lot of JSF built-in components are exposed as CDI beans, that means they can be injected by the `@Inject` annotation, such as `FacesContext`, `ExternalContext` etc.

* In the `tasks.xhtml`, there is a `viewAction` metadata set to call `TaskHome.init` method to initialize the data in the page in JSF **invoke application** phase. 

> The details of a JSF request lifecycle can be found [in the Jakarta EE tutorial](https://eclipse-ee4j.github.io/jakartaee-tutorial/jsf-intro007.html). 

## Add a task

Create a Facelets template file to render a form to add or edit a task.

```xml
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:jsf="http://xmlns.jcp.org/jsf"
                xmlns:ui="http://xmlns.jcp.org/jsf/facelets"
                xmlns:f="http://xmlns.jcp.org/jsf/core"
                xmlns:h="http://xmlns.jcp.org/jsf/html"
                xmlns:p="http://xmlns.jcp.org/jsf/passthrough"
                xmlns:c="http://java.sun.com/jsp/jstl/core"
                template="/WEB-INF/layout/template.xhtml">
 
        <f:metadata>
            <f:viewParam name="id" value="#{editTaskAction.taskId}">
                <!--                <f:validateRequired></f:validateRequired>-->
            </f:viewParam>
            <f:viewAction action="#{editTaskAction.init()}" />
        </f:metadata>
   
    <ui:define name="pageTitle"> 
        <ui:fragment rendered="#{empty editTaskAction.task.id}">ADD TASK</ui:fragment>
        <ui:fragment rendered="#{not empty editTaskAction.task.id}">EDIT TASK (ID: #{editTaskAction.task.id}, STATUS: #{editTaskAction.task.status})</ui:fragment>
    </ui:define>
    <ui:define name="content">
        <div class="row">
            <div class="col-md-12">
                <h:form id="form" role="form" class="form">
                    <div class="form-group">
                        <label class="text-uppercase" jsf:for="name">Task Name:</label>
                        <input type="text" 
                               jsf:id="name" 							
                               class="form-control #{not empty facesContext.getMessageList('form:name')?'is-invalid':''}" 
                               jsf:value="#{editTaskAction.task.name}"
                               jsf:required="true" 
                               jsf:requiredMessage="Task name is required."
                               placeholder="Type task name here...">
                        </input>
                        <small class="invalid-feedback">
                            <h:message for="name" showDetail="false" showSummary="true" />
                        </small>
                    </div>
                    <div
                        class="form-group">
                        <label class="text-uppercase" jsf:for="description">Task Description:</label>
                        <textarea jsf:id="description" 
                                  class="form-control #{not empty facesContext.getMessageList('form:description')?'is-invalid':''}" 
                                  rows="8"
                                  jsf:value="#{editTaskAction.task.description}" 
                                  jsf:required="true"
                                  jsf:requiredMessage="Description is required."
                                  placeholder="Describe the task content here..." />
                        <small class="invalid-feedback">
                            <h:message for="description" showDetail="false"
                                       showSummary="true" />
                        </small>
                    </div>

                    <div class="form-group">
                        <h:commandButton id="submitTask" type="submit"
                                         styleClass="btn btn-lg btn-primary"
                                         action="#{editTaskAction.save()}" value="Save Task"></h:commandButton>
                    </div>
                </h:form>
            </div>
        </div>
    </ui:define>
</ui:composition>
```

In above codes,

* In the metadata,try to initialize the data if there is a `taskId` parameter is provided.
* We use HTML 5 compatible forms for `input` and `textarea` components, which are friendly for the existing visual web development tools.

The following is the backend bean.

```java
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

```

There are a few projects provide mature JSF components which can speed up your development.

* [PrimeFaces](https://www.primefaces.org/)
* [BootFaces](https://www.bootsfaces.net/), JSF components based on Bootstrap
* [ButterFaces](http://www.butterfaces.org/), another JSF components project based on Bootstrap 4 and JQuery
* [OmniFaces](http://showcase.omnifaces.org/), a swiss-knife like JSF utility lib.

Some other files we did not motioned here, please check the [complete codes](https://github.com/hantsy/jakartaee-faces-sample) from my Github.

## Running the Application

You can simply run the application in your IDE, or from Maven command line. More details check the [docs](https://github.com/hantsy/jakartaee8-starter/blob/master/docs/README.md) of [Jakarta EE 8 starter](https://github.com/hantsy/jakartaee8-starter/).

There is an exception, when using Open Liberty, you have to prepare the DataSource yourself in the server.xml configuration file. 

Define the Jdbc lib and a default DataSource in *src/main/liberty/config/server.xml*.

```xml
	<!-- Derby Library Configuration -->    
	<library id="derbyJDBCLib">
	  <fileset dir="${shared.resource.dir}" includes="derby*.jar"/>
	</library>

	<!-- Datasource Configuration -->
    <!-- remove jndiName="" to serve java:comp/DefaultDataSource for Java EE 7 or above -->
	<dataSource id="DefaultDataSource">
	  <jdbcDriver libraryRef="derbyJDBCLib" />
	  <properties.derby.embedded databaseName="taskdb" createDatabase="create"/>
	</dataSource>
</server>
```

And prepare jdbc lib related resource by `maven-dependency-plugin`.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>${maven-dependency-plugin.version}</version>
    <executions>
        <execution>
            <id>copy</id>
            <phase>package</phase>
            <goals>
                <goal>copy</goal>
            </goals>   
        </execution>
    </executions>                     
    <configuration>
        <artifactItems>
            <artifactItem>
                <groupId>org.apache.derby</groupId>
                <artifactId>derby</artifactId>
                <version>${derby.version}</version>
                <type>jar</type>
                <overWrite>false</overWrite>
            </artifactItem>
        </artifactItems>
        <outputDirectory>${project.build.directory}/liberty/wlp/usr/shared/resources</outputDirectory>
    </configuration>                                    
</plugin>             
<!-- Enable liberty-maven-plugin -->
<plugin>
    <groupId>io.openliberty.tools</groupId>
    <artifactId>liberty-maven-plugin</artifactId>
    <version>${liberty-maven-plugin.version}</version>
</plugin>
```

Execute the following command when deploying to Open Liberty.

```bash
mvn liberty:create dependency:copy liberty:start
```



## Bonus: Testing 

We have explored testing Jakarta EE 8 in [a former post](https://medium.com/swlh/testing-jakarta-ee-8-applications-9ca250da20e3), it is easy to test JSF web UI via Arquillian Drone and Arquillian Graphene. 

* Arquillian Drone makes the WebDriver work seamless in JBoss  Arquillian. 
* Arquillian Graphene extends it,  add advanced Page Object pattern and Ajax support, etc.

Declare the following dependencies in `dependencyManagement`.

```xml
<!-- Selenium bom is optional -->
<!-- Selenium BOM -->
<dependency>
    <groupId>org.jboss.arquillian.selenium</groupId>
    <artifactId>selenium-bom</artifactId>
    <version>3.8.1</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
<!-- Arquillian Drone dependencies and WebDriver/Selenium dependencies -->
<dependency>
    <groupId>org.jboss.arquillian.extension</groupId>
    <artifactId>arquillian-drone-bom</artifactId>
    <version>${version.org.jboss.arquillian.drone}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
<!-- To use Arquillian Graphene 2-->
<dependency>
    <groupId>org.jboss.arquillian.graphene</groupId>
    <artifactId>graphene-webdriver</artifactId>
    <version>${version.org.jboss.arquillian.graphene}</version>
    <type>pom</type>
    <scope>test</scope>
</dependency>
<!-- To use WebDriver -->
<dependency>
    <groupId>org.jboss.arquillian.extension</groupId>
    <artifactId>arquillian-drone-webdriver-depchain</artifactId>
    <version>${version.org.jboss.arquillian.drone}</version>
    <type>pom</type>
    <scope>test</scope>
</dependency>
```

Add the dependencies in `dependencies`.

```xml
<dependency>
    <groupId>org.jboss.arquillian.graphene</groupId>
    <artifactId>graphene-webdriver</artifactId>
    <type>pom</type>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.jboss.shrinkwrap.resolver</groupId>
    <artifactId>shrinkwrap-resolver-depchain</artifactId>
    <scope>test</scope>
    <type>pom</type>
</dependency>
<!-- https://mvnrepository.com/artifact/com.google.guava/guava -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>28.2-jre</version>
    <scope>test</scope>
</dependency>
```

Configure the Webdriver used to run the tests.

Create a property in *pom.xml*.

```xml
<!-- PhantomJS will be our default browser if no profile is specified-->
<browser>phantomjs</browser> 
```

Set browser to use *phantomjs* by default.

In the *src/test/resources/arqullian.xml* file, add a qualifier *webdriver* to apply the settings of browser property.

```xml
<extension qualifier="webdriver">
    <property name="browser">${browser}</property>
</extension>
```

Add some profiles to override the browser value to switch to other webdriver.

```xml
<profile>
    <id>firefox</id>
    <properties>
        <browser>firefox</browser>
    </properties>
</profile>
<profile>
    <id>chrome</id>
    <properties>
        <browser>chrome</browser>
    </properties>
</profile>
<profile>
    <id>chromeheadless</id>
    <properties>
        <browser>chromeheadless</browser>
    </properties>
</profile>
```
Create a test using Arquillian Drone.

```java
@RunWith(Arquillian.class)
public class HomeScreenTest {

    private static final Logger LOGGER = Logger.getLogger(HomeScreenTest.class.getName());

    private static final String WEBAPP_SRC = "src/main/webapp";

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackage(Bootstrap.class.getPackage())
                .addPackage(Task.class.getPackage())
                .addPackage(FacesConfigurationBean.class.getPackage())
                .addPackage(TaskHome.class.getPackage())
                //Add JPA persistence configuration.
                //WARN: In a war archive, persistence.xml should be put into /WEB-INF/classes/META-INF/, not /META-INF
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
                // Enable CDI
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                // add template resources.
                .merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                        .importDirectory(WEBAPP_SRC).as(GenericArchive.class),
                        "/", Filters.include(".*\\.(xhtml|css|xml)$")
                );

        LOGGER.log(Level.INFO, "deployment unit:{0}", war.toString(true));
        return war;
    }

    @ArquillianResource
    private URL deploymentUrl;

    @Drone
    private WebDriver browser;
   
    @FindBy(id = "todotasks")
    private WebElement todotasks;

    @FindBy(id = "doingtasks")
    private WebElement doingtasks;

    @FindBy(id = "donetasks")
    private WebElement donetasks;

    @Test
    public void testHomePage() {
        final String url = deploymentUrl.toExternalForm();
        LOGGER.log(Level.INFO, "deploymentUrl:{0}", url);
        this.browser.get(url + "/tasks.xhtml");
        assertTrue(todotasks.findElements(By.cssSelector("li.list-group-item")).size() == 2);
        assertTrue(doingtasks.findElements(By.cssSelector("li.list-group-item")).isEmpty());
        assertTrue(donetasks.findElements(By.cssSelector("li.list-group-item")).isEmpty());
    }
}
```
In the above codes,
* Use `@Drone` to initialize a WebDriver.
* `@FindBy` is used to locate the WebElement.
*  Use `this.browser.get` to navigate a page.

Extract the web elements into a class using the  *Page Object* pattern.

```java
@Location("tasks.xhtml")
public class HomePage {

    @FindBy(id = "todotasks")
    private WebElement todotasks;

    @FindBy(id = "doingtasks")
    private WebElement doingtasks;

    @FindBy(id = "donetasks")
    private WebElement donetasks;

    public void assertTodoTasksSize(int size) {
        assertTrue(todotasks.findElements(By.cssSelector("li.list-group-item")).size() == size);
    }

    public void assertDoingTasksSize(int size) {
        assertTrue(doingtasks.findElements(By.cssSelector("li.list-group-item")).size() == size);
    }

    public void assertDoneTasksSize(int size) {
        assertTrue(donetasks.findElements(By.cssSelector("li.list-group-item")).size() == size);
    }    
    
}
```

Create a test case for the home page.

```java
 @Test
 public void testHomePageObject(@InitialPage HomePage home) {
 home.assertTodoTasksSize(2);
 }
```
In the test, you can inject the Page class by `@Page`, if the page class is annotated with `@Location`, it can be  initialized by using a `@InitialPage` annotation..

In this sample, we just checked the task items count in the home pages.

> For those new to Arquillian Drone and Arquillian Graphene2, please read [the official step-by-step guide](http://arquillian.org/guides/functional_testing_using_graphene/) from JBoss Arquillian website.

Get the [complete codes](https://github.com/hantsy/jakartaee-faces-sample) from my Github.

