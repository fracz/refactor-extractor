/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.framework.autoproxy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Juergen Hoeller
 * @author Dave Syer
 * @author Chris Beams
 */
public final class BeanNameAutoProxyCreatorInitTests {

	@Test(expected=IllegalArgumentException.class)
	public void testIgnoreAdvisorThatIsCurrentlyCreation() {
		ClassPathXmlApplicationContext ctx =
			new ClassPathXmlApplicationContext(getClass().getSimpleName() + "-context.xml", getClass());
		TestBean bean = (TestBean) ctx.getBean("bean");
		bean.setName("foo");
		assertEquals("foo", bean.getName());
		bean.setName(null); // should throw
	}

}