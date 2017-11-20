package org.jasig.cas.mgmt.services.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Defines the service bean that is produced by the webapp
 * and passed down for edit views.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceEditBean implements Serializable {
    private static final long serialVersionUID = 4882440567964605644L;

    private FormData formData = new FormData();
    private ServiceData serviceData = new ServiceData();
    private int status = -1;

    public ServiceData getServiceData() {
        return this.serviceData;
    }

    public void setServiceData(final ServiceData serviceData) {
        this.serviceData = serviceData;
    }

    public FormData getFormData() {
        return this.formData;
    }

    public void setFormData(final FormData formData) {
        this.formData = formData;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    /**
     * The type Form data.
     */
    public static class FormData {
        private List<String> availableAttributes = new ArrayList<>();
        private Map<String, Map<String, ?>> customComponent = new HashMap<>();

        public List<String> getAvailableAttributes() {
            return this.availableAttributes;
        }

        public void setAvailableAttributes(final List<String> availableAttributes) {
            this.availableAttributes = availableAttributes;
        }

        /**
         * Visible for serialization only. Use {@link FormData#getCustomComponent(String)} instead.
         *
         * @return all the custom components
         */
        public Map<String, Map<String, ?>> getCustomComponent() {
            return this.customComponent;
        }

        /**
         * Get the current properties for the specified custom component. The returned {@link Map} should only contain
         * nested Maps, Arrays, and simple objects.
         *
         * @param componentName name of the component to get the properties for (this should be unique for each
         *                      component)
         * @return current custom component properties
         */
        public Map<String, ?> getCustomComponent(final String componentName) {
            return this.customComponent.get(componentName);
        }

        /**
         * This is reserved for usage by any custom components that need to present their config to the management UI.
         * The provided {@link Map} should only contain nested Maps, Arrays, and simple objects.
         *
         * @param componentName name of the component to store the properties for (this should be unique for each
         *                      component)
         * @param properties    custom component properties
         */
        public void setCustomComponent(final String componentName, final Map<String, ?> properties) {
            this.customComponent.put(componentName, properties);
        }
    }

    /**
     * The type Service data.
     */
    public static class ServiceData {
        private String assignedId;
        private String serviceId;
        private String name;
        private String description;
        private String logoUrl;
        private String theme;
        private int evalOrder = Integer.MIN_VALUE;
        private Set<String> requiredHandlers = new HashSet<>();
        private String logoutUrl;
        private RegisteredServiceSupportAccessEditBean supportAccess = new RegisteredServiceSupportAccessEditBean();
        private String type = RegisteredServiceTypeEditBean.CAS.toString();
        private RegisteredServiceOAuthTypeEditBean oauth = new RegisteredServiceOAuthTypeEditBean();
        private String logoutType = RegisteredServiceLogoutTypeEditBean.BACK.toString();
        private RegisteredServiceUsernameAttributeProviderEditBean userAttrProvider =
                new RegisteredServiceUsernameAttributeProviderEditBean();
        private RegisteredServicePublicKeyEditBean publicKey = new RegisteredServicePublicKeyEditBean();
        private RegisteredServiceProxyPolicyBean proxyPolicy = new RegisteredServiceProxyPolicyBean();
        private RegisteredServiceAttributeReleasePolicyEditBean attrRelease
                = new RegisteredServiceAttributeReleasePolicyEditBean();
        private Map<String, Map<String, ?>> customComponent = new HashMap<>();

        public RegisteredServiceAttributeReleasePolicyEditBean getAttrRelease() {
            return this.attrRelease;
        }

        public void setAttrRelease(final RegisteredServiceAttributeReleasePolicyEditBean attrRelease) {
            this.attrRelease = attrRelease;
        }

        public RegisteredServicePublicKeyEditBean getPublicKey() {
            return this.publicKey;
        }

        public void setPublicKey(final RegisteredServicePublicKeyEditBean publicKey) {
            this.publicKey = publicKey;
        }

        public RegisteredServiceProxyPolicyBean getProxyPolicy() {
            return this.proxyPolicy;
        }

        public void setProxyPolicy(final RegisteredServiceProxyPolicyBean proxyPolicy) {
            this.proxyPolicy = proxyPolicy;
        }

        public String getTheme() {
            return this.theme;
        }

        public void setTheme(final String theme) {
            this.theme = theme;
        }

        public int getEvalOrder() {
            return this.evalOrder;
        }

        public void setEvalOrder(final int evalOrder) {
            this.evalOrder = evalOrder;
        }

        public Set<String> getRequiredHandlers() {
            return this.requiredHandlers;
        }

        public void setRequiredHandlers(final Set<String> requiredHandlers) {
            this.requiredHandlers = requiredHandlers;
        }

        public String getLogoutUrl() {
            return this.logoutUrl;
        }

        public void setLogoutUrl(final String logoutUrl) {
            this.logoutUrl = logoutUrl;
        }

        public RegisteredServiceOAuthTypeEditBean getOauth() {
            return this.oauth;
        }

        public void setOauth(final RegisteredServiceOAuthTypeEditBean oauth) {
            this.oauth = oauth;
        }

        public String getLogoutType() {
            return this.logoutType;
        }

        public void setLogoutType(final String logoutType) {
            this.logoutType = logoutType;
        }

        public RegisteredServiceUsernameAttributeProviderEditBean getUserAttrProvider() {
            return this.userAttrProvider;
        }

        public void setUserAttrProvider(final RegisteredServiceUsernameAttributeProviderEditBean userAttrProvider) {
            this.userAttrProvider = userAttrProvider;
        }

        public String getType() {
            return this.type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public RegisteredServiceSupportAccessEditBean getSupportAccess() {
            return this.supportAccess;
        }

        public void setSupportAccess(final RegisteredServiceSupportAccessEditBean supportAccess) {
            this.supportAccess = supportAccess;
        }

        public String getAssignedId() {
            return this.assignedId;
        }

        public void setAssignedId(final String assignedId) {
            this.assignedId = assignedId;
        }

        public String getServiceId() {
            return this.serviceId;
        }

        public void setServiceId(final String serviceId) {
            this.serviceId = serviceId;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public String getLogoUrl() {
            return this.logoUrl;
        }

        public void setLogoUrl(final String logoUrl) {
            this.logoUrl = logoUrl;
        }

        /**
         * Visible for serialization only. Use {@link ServiceData#getCustomComponent(String)} instead.
         *
         * @return all the custom components
         */
        public Map<String, ? extends Map<String, ?>> getCustomComponent() {
            return this.customComponent;
        }

        /**
         * Visible for deserialization only. Use {@link ServiceData#setCustomComponent(String, Map)} instead.
         *
         * @param customComponent all the custom component properties
         */
        public void setCustomComponent(final Map<String, Map<String, ?>> customComponent) {
            this.customComponent = customComponent;
        }

        /**
         * Get the current properties for the specified custom component. The returned {@link Map} should only contain
         * nested Maps, Arrays, and simple objects.
         *
         * @param componentName name of the component to get the properties for (this should be unique for each
         *                      component)
         * @return current custom component properties
         */
        public Map<String, ?> getCustomComponent(final String componentName) {
            return this.customComponent.get(componentName);
        }

        /**
         * This is reserved for usage by any custom components that need to present their config to the management UI.
         * The provided {@link Map} should only contain nested Maps, Arrays, and simple objects.
         *
         * @param componentName name of the component to store the properties for (this should be unique for each
         *                      component)
         * @param properties    custom component properties
         */
        public void setCustomComponent(final String componentName, final Map<String, ?> properties) {
            this.customComponent.put(componentName, properties);
        }
    }
}