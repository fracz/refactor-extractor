/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.server.webadmin.steps;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;

import org.neo4j.server.webdriver.ElementReference;
import org.neo4j.server.webdriver.WebadminWebdriverLibrary;
import org.openqa.selenium.By;

import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;
import cuke4duke.spring.StepDefinitions;

@StepDefinitions
public class DataBrowserSteps
{
    private final ElementReference saveButton;

    private final WebadminWebdriverLibrary wl;

    public DataBrowserSteps(  WebadminWebdriverLibrary wl ) throws InvocationTargetException, InstantiationException, IllegalAccessException
    {
        this.wl = wl;
        saveButton = new ElementReference(wl, By.xpath( "//button[@class='data-save-properties button']" ));
    }

    @Given("^I have created a node through webadmin$")
    public void iHaveCreatedANode() throws Exception {
        wl.createNodeInDataBrowser();
    }

    @Given("^I have created a relationship through webadmin$")
    public void iHaveCreatedARelationship() throws Exception {
        wl.createRelationshipInDataBrowser();
    }

    @When("^I look at the webadmin data browser in a web browser$")
    public void iLookAtDatabrowserInABrowser() throws Exception
    {
        wl.goToWebadminStartPage();
        wl.clickOnTab( "Data browser" );
    }

    @When("I enter (.+) into the data browser search field")
    public void iEnterXIntoTheDatabrowserSearchField(String keysToSend) throws Exception {
        wl.searchForInDataBrowser( keysToSend );
    }

    @Then("^The data browser item subtitle should be (.+)$")
    public void theDataBrowserItemHeadlineShouldBe(String expected) {
        wl.getDataBrowserItemHeadline().waitForTextToChangeTo( expected );
    }

    @Then("^The data browser item subtitle should change from (.+)$")
    public void theDataBrowserShouldChangeFrom(String expected) {
        wl.getDataBrowserItemHeadline().waitForTextToChangeFrom( expected );
    }

    @Then("^The data browser item subtitle should change to (.+)$")
    public void theDataBrowserItemHeadlineShouldChangeTo(String expected) {
        wl.getDataBrowserItemHeadline().waitForTextToChangeTo( expected );
    }

    @Then("^The databrowser save button should say (.+)$")
    public void theDataBrowserSaveButtonShouldSay(String text) {
        assertThat( saveButton.getText(), is( text ));
    }

    @Then("^The databrowser save button should change to saying (.+)$")
    public void theDataBrowserSaveButtonChangeToSaying(String text) {
        saveButton.waitForTextToChangeTo( text );
    }

    @Then("^The currently visible (node|relationship) in webadmin should have a property (.+) with the value (.+)$")
    public void theCurrentlyVisibleNodeShouldHavePropertyXAndValueY(String type, String expectedKey, String expectedValue) throws Exception {
        theNodeOrRelationshipWithUrlUShouldHavePropertyXAndValueY(type, wl.getCurrentDatabrowserItemSubtitle(), expectedKey, expectedValue );
    }

    @Then("^The (node|relationship) with url (.+) in webadmin should have a property (.+) with the value (.+)$")
    public void theNodeOrRelationshipWithUrlUShouldHavePropertyXAndValueY(String type, String url, String expectedKey, String expectedValue) throws Exception {
        iEnterXIntoTheDatabrowserSearchField(url);
        ElementReference el = wl.getElement( By.xpath( "//input[@value='"+expectedKey+"']/../../..//input[@class='property-value']"  ) );
        el.getElement();
        assertThat(el.getValue(), is(expectedValue));
    }
}