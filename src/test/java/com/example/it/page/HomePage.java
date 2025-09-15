/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.it.page;


import org.jboss.arquillian.graphene.page.Location;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author hantsy
 */
@Location("tasks.xhtml")
public class HomePage {

    @FindBy(id = "todotasks")
    private WebElement todotasks;

    @FindBy(id = "doingtasks")
    private WebElement doingtasks;

    @FindBy(id = "donetasks")
    private WebElement donetasks;

    public void assertTodoTasksSize(int size) {
        assertEquals(todotasks.findElements(By.cssSelector("li.list-group-item")).size(), size);
    }

    public void assertDoingTasksSize(int size) {
        assertEquals(doingtasks.findElements(By.cssSelector("li.list-group-item")).size(), size);
    }

    public void assertDoneTasksSize(int size) {
        assertEquals(donetasks.findElements(By.cssSelector("li.list-group-item")).size(), size);
    }    
    
}