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
package org.neo4j.server.webdriver;

import java.lang.reflect.InvocationTargetException;

import org.neo4j.server.steps.ServerIntegrationTestFacade;
import org.openqa.selenium.By;

public class WebadminWebdriverLibrary extends WebdriverLibrary
{

    private String serverUrl;
    private final ElementReference dataBrowserSearchField;
    private final ElementReference dataBrowserItemSubtitle;

    public WebadminWebdriverLibrary(WebDriverFacade wf, ServerIntegrationTestFacade serverFacade) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        super(wf);

        setServerUrl( serverFacade.getServerUrl() );

        dataBrowserItemSubtitle = new ElementReference(this, By.xpath( "//div[@id='data-area']//div[@class='title']//p[@class='small']" ));
        dataBrowserSearchField = new ElementReference(this, By.id( "data-console" ));
    }

    public void setServerUrl(String url) {
        serverUrl = url;
    }

    public void goToServerRoot() {
        d.get( serverUrl );
    }

    public void goToWebadminStartPage() throws Exception {
        goToServerRoot();
        waitForTitleToBe( "Neo4j Monitoring and Management Tool" );
    }

    public void clickOnTab(String tab) {
        getElement( By.xpath( "//ul[@id='mainmenu']//a[contains(.,'"+tab+"')]") ).click();
    }

    public void searchForInDataBrowser(CharSequence ... keysToSend) throws Exception {
        clearInput( dataBrowserSearchField );
        dataBrowserSearchField.sendKeys( keysToSend );
    }

    public String getCurrentDatabrowserItemSubtitle() {
        return dataBrowserItemSubtitle.getText();
    }

    public String createNodeInDataBrowser() throws Exception {
        goToWebadminStartPage();
        clickOnTab( "Data browser" );
        String prevItemHeadline = getCurrentDatabrowserItemSubtitle();

        clickOnButton( "Node" );

        dataBrowserItemSubtitle.waitForTextToChangeFrom( prevItemHeadline );

        return getCurrentDatabrowserItemSubtitle();
    }

    public String createRelationshipInDataBrowser() throws Exception {
        createNodeInDataBrowser();
        String prevItemHeadline = getCurrentDatabrowserItemSubtitle();

        clickOnButton( "Relationship" );
        getElement( By.id( "create-relationship-to" ) ).sendKeys( "0" );
        clickOnButton( "Create" );

        dataBrowserItemSubtitle.waitForTextToChangeFrom( prevItemHeadline );

        return getCurrentDatabrowserItemSubtitle();
    }

    public ElementReference getDataBrowserItemHeadline() {
        return dataBrowserItemSubtitle;
    }

}