/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.query.aggregation.histogram;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

@JsonTypeName("min")
public class MinPostAggregator extends ApproximateHistogramPostAggregator
{
  static final Comparator COMPARATOR = new Comparator()
  {
    @Override
    public int compare(Object o, Object o1)
    {
      return Double.compare(((Number) o).doubleValue(), ((Number) o1).doubleValue());
    }
  };

  private String fieldName;

  @JsonCreator
  public MinPostAggregator(
      @JsonProperty("name") String name,
      @JsonProperty("fieldName") String fieldName
  )
  {
    super(name, fieldName);
    this.fieldName = fieldName;
  }

  @Override
  public Comparator getComparator()
  {
    return COMPARATOR;
  }

  @Override
  public Set<String> getDependentFields()
  {
    return Sets.newHashSet(fieldName);
  }

  @Override
  public Object compute(Map<String, Object> values)
  {
    final ApproximateHistogram ah = (ApproximateHistogram) values.get(this.getFieldName());
    return ah.getMin();
  }

  @Override
  public String toString()
  {
    return "QuantilePostAggregator{" +
           "fieldName='" + fieldName + '\'' +
           '}';
  }
}