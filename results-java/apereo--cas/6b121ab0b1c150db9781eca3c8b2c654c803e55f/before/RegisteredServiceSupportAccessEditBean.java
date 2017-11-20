package org.jasig.cas.mgmt.services.web.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Defines a JSON bean that is mapped
 * to the CAS service access strategy.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceSupportAccessEditBean implements Serializable {

    private static final long serialVersionUID = 2995938566845586064L;

    private String startingTime;
    private String endingTime;
    private boolean casEnabled;
    private boolean ssoEnabled;
    private boolean requireAll;
    private Map<String, Set<String>> requiredAttr = new HashMap<>();

    public boolean isCasEnabled() {
        return this.casEnabled;
    }

    public void setCasEnabled(final boolean casEnabled) {
        this.casEnabled = casEnabled;
    }

    public boolean isSsoEnabled() {
        return this.ssoEnabled;
    }

    public void setSsoEnabled(final boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public boolean isRequireAll() {
        return this.requireAll;
    }

    public void setRequireAll(final boolean requireAll) {
        this.requireAll = requireAll;
    }

    public Map<String, Set<String>> getRequiredAttr() {
        return this.requiredAttr;
    }

    public void setRequiredAttr(final Map<String, Set<String>> requiredAttr) {
        this.requiredAttr = requiredAttr;
    }

    public String getStartingTime() {
        return this.startingTime;
    }

    public void setStartingTime(final String startingTime) {
        this.startingTime = startingTime;
    }

    public String getEndingTime() {
        return this.endingTime;
    }

    public void setEndingTime(final String endingTime) {
        this.endingTime = endingTime;
    }
}