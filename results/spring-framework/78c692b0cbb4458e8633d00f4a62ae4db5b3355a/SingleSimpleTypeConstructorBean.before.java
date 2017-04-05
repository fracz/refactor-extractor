/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

/**
 * @author Juergen Hoeller
 * @since 23.10.2004
 */
public class SingleSimpleTypeConstructorBean {

	private boolean singleBoolean;

	private boolean secondBoolean;

	private String testString;

	public SingleSimpleTypeConstructorBean(boolean singleBoolean) {
		this.singleBoolean = singleBoolean;
	}

	public SingleSimpleTypeConstructorBean(String testString, boolean secondBoolean) {
		this.testString = testString;
		this.secondBoolean = secondBoolean;
	}

	public boolean isSingleBoolean() {
		return singleBoolean;
	}

	public boolean isSecondBoolean() {
		return secondBoolean;
	}

	public String getTestString() {
		return testString;
	}

}