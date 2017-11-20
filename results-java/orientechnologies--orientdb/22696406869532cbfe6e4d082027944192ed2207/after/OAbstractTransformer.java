/*
 *
 *  * Copyright 2010-2014 Orient Technologies LTD (info(at)orientechnologies.com)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.orientechnologies.orient.etl.transformer;

import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.etl.OAbstractETLComponent;

/**
 * Abstract Transformer.
 */
public abstract class OAbstractTransformer extends OAbstractETLComponent implements OTransformer {
  @Override
  public Object transform(final Object input) {
    if (input == null)
      return null;

    if (skip(input))
      return input;
    else
      return executeTransform(input);
  }

  protected abstract Object executeTransform(final Object input);

  protected boolean skip(final Object input) {
    if (ifFilter != null && input instanceof ODocument) {
      final Object result = ifFilter.evaluate((ODocument) input, null, context);
      if (!(result instanceof Boolean))
        throw new OConfigurationException("'if' expression in Transformer " + getName() + " returned '" + result
            + "' instead of boolean");

      return !((Boolean) result).booleanValue();
    }

    return false;
  }
}