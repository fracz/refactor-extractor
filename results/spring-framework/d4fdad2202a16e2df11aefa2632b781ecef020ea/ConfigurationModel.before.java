/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.config.java.model;

import static java.lang.String.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.config.java.annotation.Configuration;


/**
 * An abstract representation of a set of user-provided "Configuration classes", usually but not
 * necessarily annotated with {@link Configuration @Configuration}. The model is populated with a
 * {@link org.springframework.config.java.internal.parsing.ConfigurationParser} implementation which
 * may be reflection-based or ASM-based. Once a model has been populated, it can then be rendered
 * out to a set of BeanDefinitions. The model provides an important layer of indirection between the
 * complexity of parsing a set of classes and the complexity of representing the contents of those
 * classes as BeanDefinitions.
 *
 * <p>Interface follows the builder pattern for method chaining.</p>
 *
 * @author  Chris Beams
 * @see     org.springframework.config.java.internal.parsing.ConfigurationParser
 */
public final class ConfigurationModel implements Validatable {

    /* list is used because order and collection equality matters. */
    private final ArrayList<ConfigurationClass> configurationClasses = new ArrayList<ConfigurationClass>();
    private final ArrayList<Validator> validators = new ArrayList<Validator>();

    /**
     * Add a {@link Configuration @Configuration} class to the model. Classes may be added at will
     * and without any particular validation. Malformed classes will be caught and errors processed
     * during {@link #validate() validation}
     *
     * @param  configurationClass  user-supplied Configuration class
     */
    public ConfigurationModel add(ConfigurationClass configurationClass) {
        configurationClasses.add(configurationClass);
        return this;
    }

    public void registerValidator(Validator validator) {
        validators.add(validator);
    }

    /**
     * Return configuration classes that have been directly added to this model.
     *
     * @see  #getAllConfigurationClasses()
     */
    public ConfigurationClass[] getConfigurationClasses() {
        return configurationClasses.toArray(new ConfigurationClass[] { });
    }

//    /**
//     * Return all configuration classes, including all imported configuration classes. This method
//     * should be generally preferred over {@link #getConfigurationClasses()}
//     *
//     * @see  #getConfigurationClasses()
//     */
//    public ConfigurationClass[] getAllConfigurationClasses() {
//        ArrayList<ConfigurationClass> allConfigClasses = new ArrayList<ConfigurationClass>();
//
//        for (ConfigurationClass configClass : configurationClasses)
//            allConfigClasses.addAll(configClass.getSelfAndAllImports());
//
//        return allConfigClasses.toArray(new ConfigurationClass[allConfigClasses.size()]);
//    }

    public ConfigurationClass[] getAllConfigurationClasses() {
        return configurationClasses.toArray(new ConfigurationClass[configurationClasses.size()]);
    }

    /**
     * Recurses through the model validating each object along the way and aggregating any <var>errors</var>.
     *
     * @see ConfigurationClass#validate(java.util.List)
     * @see ModelMethod#validate(java.util.List)
     * @see Validator
     * @see UsageError
     */
    public void validate(List<UsageError> errors) {
        // user must specify at least one configuration
        if (configurationClasses.isEmpty())
            errors.add(new EmptyModelError());

        // cascade through model and allow handlers to register validators
        // depending on where they are registered (with the model, the class, or the method)
        // they will be called directly or indirectly below
        for (ConfigurationClass configClass : getAllConfigurationClasses()) {
            for(ModelMethod method : configClass.getMethods()) {
                for(Validator validator : method.getValidators()) {
                    if(validator.supports(method))
                        method.registerValidator(validator);
                    // TODO: support class-level validation
                    // if(validator.supports(configClass))
                   //     configClass.registerValidator(validator);
                    if(validator.supports(this))
                        this.registerValidator(validator);
                }
            }
        }

        // process any validators registered directly with this model object
        for(Validator validator : validators)
            validator.validate(this, errors);

        // each individual configuration class must be well-formed
        // note that each configClass detects usage errors on its imports recursively
        // note that each configClass will recursively process its respective methods
        for (ConfigurationClass configClass : configurationClasses)
            configClass.validate(errors);
    }

    @Override
    public String toString() {
        return format("%s: configurationClasses=%s",
                      getClass().getSimpleName(), configurationClasses);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configurationClasses == null) ? 0 : configurationClasses.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigurationModel other = (ConfigurationModel) obj;
        if (configurationClasses == null) {
            if (other.configurationClasses != null)
                return false;
        } else if (!configurationClasses.equals(other.configurationClasses))
            return false;
        return true;
    }


    public class EmptyModelError extends UsageError {
        public EmptyModelError() {
            super(null, 0);
        }

        @Override
        public String getDescription() {
            return format("Configuration model was empty. Make sure at least one "
                          + "@%s class has been specified.", Configuration.class.getSimpleName());
        }
    }

}