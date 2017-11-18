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

import static org.hamcrest.Matchers.not;
import static org.neo4j.server.webdriver.ElementAttributeIs.elementAttributeIs;
import static org.neo4j.server.webdriver.ElementTextIs.elementTextIs;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

/**
 * This is used rather than a direct
 * (RenderedWebElement) reference, because our UI renders a lot, and such
 * references often become stale. This allows code that encounters
 * StaleElementReferenceException to simply reload the element.
 *
 * This class also provides a subset of the RenderedWebElement API, which
 * abstracts away StaleElementReferenceException.
 */
public class ElementReference {

    protected By selector;
    protected WebdriverLibrary wl;
    protected boolean matchLast;

    public ElementReference(WebdriverLibrary wl, By selector) {
        this(wl, selector, false);
    }

    public ElementReference(WebdriverLibrary wl, By selector, boolean matchLast) {
        this.wl = wl;
        this.selector = selector;
        this.matchLast = matchLast;
    }

    public RenderedWebElement getElement() {
        return wl.getWebElement( selector );
    }

    public RenderedWebElement findElement(By by) {
        try {
            return (RenderedWebElement)this.getElement().findElement(by);
        } catch (StaleElementReferenceException e) {
            return this.findElement(by);
        }
    }

    public List<WebElement> findElements(By by) {
        try {
            return this.getElement().findElements(by);
        } catch (StaleElementReferenceException e) {
            return this.findElements(by);
        }
    }

    public String getAttribute( String attributeName ) {
        try {
            return this.getElement().getAttribute(attributeName);
        } catch (StaleElementReferenceException e) {
            return this.getAttribute(attributeName);
        }
    }

    public void click() {
        try {
            this.getElement().click();
        } catch (StaleElementReferenceException e) {
            this.click();
        }
    }

    public String getValueOfCssProperty(String cssProperty) {
        try {
            return this.getElement().getValueOfCssProperty(cssProperty);
        } catch (StaleElementReferenceException e) {
            return this.getValueOfCssProperty(cssProperty);
        }
    }

    public void sendKeys(CharSequence ... keysToSend) {
        try {
            this.waitUntilVisible();
            this.getElement().sendKeys(keysToSend);
        } catch (StaleElementReferenceException e) {
            this.sendKeys(keysToSend);
        }
    }

    public String getValue() {
        try {
            return this.getElement().getValue();
        } catch (StaleElementReferenceException e) {
            return this.getValue();
        }
    }

    public String getText() {
        try {
            this.waitUntilVisible();
            return this.getElement().getText();
        } catch (StaleElementReferenceException e) {
            return this.getText();
        }
    }

    public void clear() {
        try {
            this.getElement().clear();
        } catch (StaleElementReferenceException e) {
            this.clear();
        }
    }

    public void waitUntilVisible() {
        wl.waitForElementToAppear( selector );
    }

    public void waitUntilNotVisible() {
        wl.waitForElementToDisappear( selector );
    }

    public void waitForAttributeToBe(String attr, String value) {
        Condition<ElementReference> cond = new Condition<ElementReference>(elementAttributeIs(attr, value), this);
        cond.waitUntilFulfilled(10000, "Attribute "+attr+" did not change to "+value+" within a reasonable time.");
    }

    public void waitForAttributeToChangeFrom(String attr, String value) {
        Condition<ElementReference> cond = new Condition<ElementReference>(not( elementAttributeIs(attr, value) ), this);
        cond.waitUntilFulfilled(10000, "Attribute "+attr+" did not change from "+value+" within a reasonable time.");
    }

    public void waitForTextToChangeFrom(String value) {
        Condition<ElementReference> cond = new Condition<ElementReference>(not( elementTextIs(value) ), this);
        cond.waitUntilFulfilled(10000, "Element text did not change from "+value+" within a reasonable time.");
    }

    public void waitForTextToChangeTo(String value) {
        Condition<ElementReference> cond = new Condition<ElementReference>( elementTextIs(value), this);
        cond.waitUntilFulfilled(10000, "Element text did not change from "+value+" within a reasonable time.");
    }

}