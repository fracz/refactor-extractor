package com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition;

import com.dangdang.ddframe.rdb.sharding.api.BaseShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.RangeShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ConditionTest {

    @Test
    public void assertGetShardingValue() {
        Condition condition = new Condition(new Column("test", "test"), new SQLNumberExpression(1));
        BaseShardingValue shardingValue = condition.getShardingValue(Collections.emptyList());
        assertThat(shardingValue.getType(), is(ShardingValue.ShardingValueType.SINGLE));
        assertThat((Integer) ((ShardingValue) shardingValue).getValue(), is(1));
        condition = new Condition(new Column("test", "test"), Arrays.<SQLExpression>asList(new SQLNumberExpression(1), new SQLNumberExpression(2)));
        shardingValue = condition.getShardingValue(Collections.emptyList());
        assertThat(shardingValue.getType(), is(ShardingValue.ShardingValueType.LIST));
        Iterator<?> iterator = ((ShardingValue) shardingValue).getValues().iterator();
        assertThat((Integer) iterator.next(), is(1));
        assertThat((Integer) iterator.next(), is(2));
        condition = new Condition(new Column("test", "test"), new SQLNumberExpression(1), new SQLNumberExpression(2));
        shardingValue = condition.getShardingValue(Collections.emptyList());
        assertThat(shardingValue.getType(), is(ShardingValue.ShardingValueType.RANGE));
        assertThat((Integer) ((RangeShardingValue) shardingValue).getValueRange().lowerEndpoint(), is(1));
        assertThat((Integer) ((RangeShardingValue) shardingValue).getValueRange().upperEndpoint(), is(2));
    }
}