/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author HyunGil Jeong
 */
public class AgentInfo {

    public static final Comparator<AgentInfo> AGENT_NAME_ASC_COMPARATOR = new Comparator<AgentInfo>() {
        @Override
        public int compare(AgentInfo lhs, AgentInfo rhs) {
            final String lhsAgentId = lhs.agentId == null ? "" : lhs.agentId;
            final String rhsAgentId = rhs.agentId == null ? "" : rhs.agentId;
            return lhsAgentId.compareTo(rhsAgentId);
        }
    };

    private String applicationName;
    private String agentId;
    private long startTimestamp;
    private String hostName;
    private String ip;
    private String ports;
    private ServiceType serviceType;
    private int pid;
    private String vmVersion;
    private String agentVersion;
    private ServerMetaDataBo serverMetaData;

    @JsonInclude(Include.NON_DEFAULT)
    private long initialStartTimestamp;

    @JsonInclude(Include.NON_NULL)
    private AgentStatus status;

    public AgentInfo() {
    }

    public AgentInfo(AgentInfoBo agentInfoBo) {
        this.applicationName = agentInfoBo.getApplicationName();
        this.agentId = agentInfoBo.getAgentId();
        this.startTimestamp = agentInfoBo.getStartTime();
        this.hostName = agentInfoBo.getHostName();
        this.ip = agentInfoBo.getIp();
        this.ports = agentInfoBo.getPorts();
        this.serviceType = agentInfoBo.getServiceType();
        this.pid = agentInfoBo.getPid();
        this.vmVersion = agentInfoBo.getVmVersion();
        this.agentVersion = agentInfoBo.getAgentVersion();
        this.serverMetaData = agentInfoBo.getServerMetaData();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getVmVersion() {
        return vmVersion;
    }

    public void setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public ServerMetaDataBo getServerMetaData() {
        return serverMetaData;
    }

    public void setServerMetaData(ServerMetaDataBo serverMetaData) {
        this.serverMetaData = serverMetaData;
    }

    public long getInitialStartTimestamp() {
        return initialStartTimestamp;
    }

    public void setInitialStartTimestamp(long initialStartTimestamp) {
        this.initialStartTimestamp = initialStartTimestamp;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
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
        AgentInfo other = (AgentInfo)obj;
        if (agentId == null) {
            if (other.agentId != null)
                return false;
        } else if (!agentId.equals(other.agentId))
            return false;
        if (serviceType == null) {
            if (other.serviceType != null)
                return false;
        } else if (!serviceType.equals(other.serviceType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AgentInfo [applicationName=" + applicationName + ", agentId=" + agentId + ", startTimestamp="
                + startTimestamp + ", hostName=" + hostName + ", ip=" + ip + ", ports=" + ports + ", serviceType="
                + serviceType + ", pid=" + pid + ", vmVersion=" + vmVersion + ", agentVersion=" + agentVersion
                + ", serverMetaData=" + serverMetaData + ", initialStartTimestamp=" + initialStartTimestamp
                + ", status=" + status + "]";
    }

}