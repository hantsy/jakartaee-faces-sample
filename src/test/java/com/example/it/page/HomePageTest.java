package com.example.it.page;

import com.example.Bootstrap;
import com.example.config.FacesConfigurationBean;
import com.example.domain.Task;
import com.example.web.TaskHome;
import org.jboss.arquillian.container.test.api.Deployment;
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

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author hantsy
 */
@ExtendWith(ArquillianExtension.class)
public class HomePageTest {

    private static final Logger LOGGER = Logger.getLogger(HomePageTest.class.getName());

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

    // see: https://github.com/arquillian/arquillian-core/issues/312
    @Test
    public void testHomePageObject(@ArquillianResource @InitialPage HomePage home) {
        home.assertTodoTasksSize(2);
    }

}
