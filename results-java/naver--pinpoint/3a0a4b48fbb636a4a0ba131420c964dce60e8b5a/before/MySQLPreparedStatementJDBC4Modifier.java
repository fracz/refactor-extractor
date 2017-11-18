/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.modifier.db.mysql;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Type;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.profiler.interceptor.GroupDelegateStaticInterceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.PreparedStatementBindVariableInterceptor;
import com.navercorp.pinpoint.profiler.util.BindVariableFilter;
import com.navercorp.pinpoint.profiler.util.IncludeBindVariableFilter;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.profiler.util.PreparedStatementUtils;

/**
 * @author emeroad
 */
public class MySQLPreparedStatementJDBC4Modifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLPreparedStatementJDBC4Modifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/JDBC4PreparedStatement";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }
        try {
            InstrumentClass preparedStatement = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            bindVariableIntercept(preparedStatement, classLoader, protectedDomain);

            return preparedStatement.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }

    private void bindVariableIntercept(InstrumentClass preparedStatement, ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
        // TODO Need to add parameter type to filter arguments
        // Cannot specify methods without parameter type information because each JDBC driver has different API.
        BindVariableFilter exclude = new IncludeBindVariableFilter(new String[]{"setRowId", "setNClob", "setSQLXML"});
        List<Method> bindMethod = PreparedStatementUtils.findBindVariableSetMethod(exclude);

        // TODO Do we have to utilize this logic?
        // It would be better to create util api in bci package which adds interceptors to multiple methods.
        final InterceptorGroupInvocation scope = byteCodeInstrumentor.getInterceptorGroupTransaction(MYSQLScope.SCOPE_NAME);
        Interceptor interceptor = new GroupDelegateStaticInterceptor(new PreparedStatementBindVariableInterceptor(), scope);
        int interceptorId = -1;
        for (Method method : bindMethod) {
            String methodName = method.getName();
            String[] parameterType = JavaAssistUtils.getParameterType(method.getParameterTypes());
            try {
                if (interceptorId == -1) {
                    interceptorId = preparedStatement.addInterceptor(methodName, parameterType, interceptor, Type.after);
                } else {
                    preparedStatement.reuseInterceptor(methodName, parameterType, interceptorId, Type.after);
                }
            } catch (NotFoundInstrumentException e) {
                // Cannot find bind variable setter method. This is not an error. logging will be enough.
                // Did not log stack trace intentionally
                if (logger.isDebugEnabled()) {
                    logger.debug("bindVariable api not found. method:{} param:{} Cause:{}", methodName, Arrays.toString(parameterType), e.getMessage());
                }
            }
        }
    }
}