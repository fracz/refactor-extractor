/**
 * Copyright (c) 2002-2010 "Neo Technology,"
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

package org.neo4j.webadmin.functional.web;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Ignore;
import org.neo4j.rest.WebServerFactory;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.AdminServer;
import org.neo4j.webadmin.TestUtil;
import org.neo4j.webadmin.backup.BackupManager;
import org.neo4j.webadmin.domain.BackupFailedException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.quartz.SchedulerException;

/**
 * Test that the webadmin HTTP console works and produces output as expected.
 *
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 *
 */

@Ignore
public abstract class WebDriverTest {

	protected WebDriver webDriver = new FirefoxDriver();

	@BeforeClass
    public static void startWebServer() throws IOException, SchedulerException,
            BackupFailedException
    {
        TestUtil.deleteTestDb();
        WebServerFactory.getDefaultWebServer().startServer(  );
        AdminServer.INSTANCE.startServer(AdminServer.DEFAULT_PORT(), "target/classes/public");
        BackupManager.INSTANCE.start();
    }

	@AfterClass
    public static void stopWebServer() throws Exception
    {
		WebServerFactory.getDefaultWebServer().stopServer();
        AdminServer.INSTANCE.stopServer();
        BackupManager.INSTANCE.stop();
        DatabaseLocator.shutdownGraphDatabase( );
    }

	@Before
	public void initWebDriver() {
		webDriver.get("http://localhost:9988/index-no-feedback.html");

		waitForElementToAppear(By.id("mainmenu-dashboard"));
	}

	@After
	public void closeWindow() {
		webDriver.close();
	}

	protected ElementReference dashboardMenu = new ElementReference() {
		public RenderedWebElement getElement() {
			return (RenderedWebElement) waitForElementToAppear(By.id("mainmenu-dashboard")).findElement(By.tagName("a"));
		}
	};

	protected ElementReference consoleMenu = new ElementReference() {
		public RenderedWebElement getElement() {
			return (RenderedWebElement) waitForElementToAppear(By.id("mainmenu-console")).findElement(By.tagName("a"));
		}
	};

	protected ElementReference configMenu = new ElementReference() {
		public RenderedWebElement getElement() {
			return (RenderedWebElement) waitForElementToAppear(By.id("mainmenu-config")).findElement(By.tagName("a"));
		}
	};

	protected ElementReference dataMenu = new ElementReference() {
		public RenderedWebElement getElement() {
			return (RenderedWebElement) waitForElementToAppear(By.id("mainmenu-data")).findElement(By.tagName("a"));
		}
	};

	protected ElementReference backupMenu = new ElementReference() {
		public RenderedWebElement getElement() {
			return (RenderedWebElement) waitForElementToAppear(By.id("mainmenu-backup")).findElement(By.tagName("a"));
		}
	};

	protected ElementReference ioMenu = new ElementReference() {
		public RenderedWebElement getElement() {
			return (RenderedWebElement) waitForElementToAppear(By.id("mainmenu-io")).findElement(By.tagName("a"));
		}
	};

	protected ElementReference jmxMenu = new ElementReference() {
		public RenderedWebElement getElement() {
			return (RenderedWebElement) waitForElementToAppear(By.id("mainmenu-jmx")).findElement(By.tagName("a"));
		}
	};

	protected ElementReference configDatabaseLocation = new ElementReference() {
		public RenderedWebElement getElement() {
			return waitForElementToAppear(By.id("mor_setting_db.root"));
		}
	};

	protected ElementReference consoleWrap = new ElementReference() {
		public RenderedWebElement getElement() {
			return waitForElementToAppear(By.className("mor_console_wrap"));
		}
	};

	protected ElementReference consoleInput = new ElementReference() {
		public RenderedWebElement getElement() {
			return waitForElementToAppear(By.id("mor_console_input"));
		}
	};

	protected ElementReference dashboardValueTrackers =  new ElementReference() {
		public RenderedWebElement getElement() {
			return waitForElementToAppear(By.id("mor_monitor_valuetrackers"));
		}
	};

	protected void waitForElementToBeVisible(ElementReference elRef){
		long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
        	try {
	           if(elRef.getElement().getValueOfCssProperty("display") != "none") {
	        	   return;
				}
			} catch(StaleElementReferenceException e ) {

			}
        }

        throw new RuntimeException("Element did not become visible within a reasonable time. Element was: " + elRef.getElement().toString());
	}

	protected void waitForAttributeToBe(ElementReference elRef, String attributeName, String expectedValue){
		long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
        	try {
	           if(elRef.getElement().getAttribute(attributeName) == expectedValue) {
	        	   return;
				}
			} catch(StaleElementReferenceException e ) {

			}
        }

        throw new RuntimeException("Element did not become visible within a reasonable time. Element was: " + elRef.getElement().toString());
	}

	protected void waitForAttributeToChangeFrom(ElementReference elRef, String attributeName, String currentValue){
		long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
        	try {
	        	if(elRef.getElement().getAttribute(attributeName) != currentValue) {
	        	   return;
	        	}
        	} catch(StaleElementReferenceException e ) {

        	}
        }

        throw new RuntimeException("Element attribute did not change within a reasonable time. Element was: " + elRef.getElement().toString());
	}

	protected RenderedWebElement waitForElementToAppear(By findBy) {
		long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
        	try{
                return (RenderedWebElement) webDriver.findElement(findBy);
        	}
        	catch(NoSuchElementException ex)
        	{
        		;
        	}
        }

        throw new NoSuchElementException("Unable to locate element: " + findBy.toString());
	}


}