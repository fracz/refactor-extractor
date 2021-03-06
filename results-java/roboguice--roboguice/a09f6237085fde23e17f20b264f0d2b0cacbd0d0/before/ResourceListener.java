/*
 * Copyright 2009 Michael Burton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package roboguice.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Nullable;
import com.google.inject.spi.TypeEncounter;

import android.app.Application;
import android.graphics.drawable.Drawable;

/**
 *
 * @author Mike Burton
 */
public class ResourceListener implements StaticTypeListener {
    protected Application app;

    public ResourceListener( Application app ) {
        this.app = app;
    }

    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        Class<?> c = typeLiteral.getRawType();
        while( c!=null ) {
            for (Field field : c.getDeclaredFields())
                if( !Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(InjectResource.class) )
                    typeEncounter.register(new ResourceMembersInjector<I>(field, app, field.getAnnotation(InjectResource.class)));
            c = c.getSuperclass();
        }
    }

    @SuppressWarnings("unchecked")
    public void requestStaticInjection(Class<?>... types) {
        for( Class<?> c : types ) {
            while( c!=null ) {
                for (Field field : c.getDeclaredFields())
                    if( Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(InjectResource.class) )
                        new ResourceMembersInjector(field, app, field.getAnnotation(InjectResource.class)).injectMembers(null);
                c = c.getSuperclass();
            }
        }

    }
}


class ResourceMembersInjector<T> implements MembersInjector<T> {
    protected Field field;
    protected Application app;
    protected InjectResource annotation;

    public ResourceMembersInjector( Field field, Application app, InjectResource annotation ) {
        this.field = field;
        this.app = app;
        this.annotation = annotation;
    }

    public void injectMembers(T instance) {

        Object value = null;

        try {

            final int id = annotation.value();
            final Class<?> t = field.getType();

            if( String.class.isAssignableFrom(t) )
                value = app.getResources().getString(id);

            else if( Drawable.class.isAssignableFrom(t) )
                value = app.getResources().getDrawable(id);


            if( value==null && field.getAnnotation(Nullable.class)==null )
                throw new NullPointerException( String.format("Can't inject null value into %s.%s when field is not @Nullable", field.getDeclaringClass(), field.getName() ));

            field.setAccessible(true);
            field.set(instance, value );

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);

        } catch (IllegalArgumentException f ) {
            throw new IllegalArgumentException( String.format("Can't assign %s value %s to %s field %s", value!=null ? value.getClass() : "(null)", value, field.getType(), field.getName() ));
        }
    }
}