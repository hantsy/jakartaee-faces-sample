package com.example.it;

import com.example.Bootstrap;
import com.example.config.FacesConfigurationBean;
import com.example.domain.Task;
import com.example.web.TaskHome;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author hantsy
 */
@ExtendWith(ArquillianExtension.class)
@ExtendWith(InitialPageIgnoreExtension.class)
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
        assertEquals(2, todotasks.findElements(By.cssSelector("li.list-group-item")).size());
        assertTrue(doingtasks.findElements(By.cssSelector("li.list-group-item")).isEmpty());
        assertTrue(donetasks.findElements(By.cssSelector("li.list-group-item")).isEmpty());
        List<WebElement> todoTasksWebElements = todotasks.findElements(By.cssSelector("li.list-group-item"));
        if (!todoTasksWebElements.isEmpty()) {
            WebElement buttonElement = todoTasksWebElements.get(0).findElement(By.cssSelector("a.btn"));
            Graphene.guardHttp(buttonElement).click();

            Graphene.waitGui();

            assertEquals(1, todotasks.findElements(By.cssSelector("li.list-group-item")).size());
            assertEquals(1, doingtasks.findElements(By.cssSelector("li.list-group-item")).size());
        }
    }

    @Test
    public void testHomePageObject(@InitialPage HomePage home) {
        home.assertTodoTasksSize(2);
    }

}
