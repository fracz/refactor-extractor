package backtype.storm.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import backtype.storm.Config;

public class Cluster {

    /**
     * key: supervisor id, value: supervisor details
     */
    private Map<String, SupervisorDetails>   supervisors;
    /**
     * key: topologyId, value: topology's current assignments.
     */
    private Map<String, SchedulerAssignment> assignments;

    /**
     * a map from hostname to supervisor id.
     */
    private Map<String, List<String>>        hostToIds;

    public Cluster(Map<String, SupervisorDetails> supervisors, Map<String, SchedulerAssignment> assignments){
        this.supervisors = new HashMap<String, SupervisorDetails>(supervisors.size());
        this.supervisors.putAll(supervisors);
        this.assignments = new HashMap<String, SchedulerAssignment>(assignments.size());
        this.assignments.putAll(assignments);
        this.hostToIds = new HashMap<String, List<String>>();
        for (String nodeId : supervisors.keySet()) {
            SupervisorDetails supervisor = supervisors.get(nodeId);
            String host = supervisor.getHost();
            if (!this.supervisors.containsKey(host)) {
                this.hostToIds.put(host, new ArrayList<String>());
            }
            this.hostToIds.get(host).add(nodeId);
        }
    }

    /**
     * Gets all the topologies which needs scheduling.
     *
     * @param topologies
     * @return
     */
    public List<TopologyDetails> needsSchedulingTopologies(Topologies topologies) {
        List<TopologyDetails> ret = new ArrayList<TopologyDetails>();
        for (TopologyDetails topology : topologies.getTopologies()) {
            if (needsScheduling(topology)) {
                ret.add(topology);
            }
        }

        return ret;
    }

    /**
     * Does the topology need scheduling?
     *
     * A topology needs scheduling if one of the following conditions holds:
     * <ul>
     *   <li>Although the topology is assigned slots, but is squeezed. i.e. the topology is assigned less slots than desired.</li>
     *   <li>There are unassigned executors in this topology</li>
     * </ul>
     */
    public boolean needsScheduling(TopologyDetails topology) {
        int desiredNumWorkers = ((Number) topology.getConf().get(Config.TOPOLOGY_WORKERS)).intValue();
        int assignedNumWorkers = this.getAssignedNumWorkers(topology);

        if (desiredNumWorkers > assignedNumWorkers) {
            return true;
        }

        return this.getUnassignedExecutors(topology).size() > 0;
    }

    /**
     * Gets a executor -> component-id map which needs scheduling in this topology.
     *
     * @param topology
     * @return
     */
    public Map<ExecutorDetails, String> getNeedsSchedulingExecutorToComponents(TopologyDetails topology) {
        Collection<ExecutorDetails> allExecutors = topology.getExecutors();

        SchedulerAssignment assignment = this.assignments.get(topology.getId());
        if (assignment != null) {
            Collection<ExecutorDetails> assignedExecutors = assignment.getExecutors();
            allExecutors.removeAll(assignedExecutors);
        }

        return topology.selectExecutorToComponents(allExecutors);
    }

    /**
     * Gets a component-id -> executors map which needs scheduling in this topology.
     *
     * @param topology
     * @return
     */
    public Map<String, List<ExecutorDetails>> getNeedsSchedulingComponentToExecutors(TopologyDetails topology) {
        Map<ExecutorDetails, String> executorToComponents = this.getNeedsSchedulingExecutorToComponents(topology);
        Map<String, List<ExecutorDetails>> componentToExecutors = new HashMap<String, List<ExecutorDetails>>();
        for (ExecutorDetails executor : executorToComponents.keySet()) {
            String component = executorToComponents.get(executor);
            if (!componentToExecutors.containsKey(component)) {
                componentToExecutors.put(component, new ArrayList<ExecutorDetails>());
            }

            componentToExecutors.get(component).add(executor);
        }

        return componentToExecutors;
    }


    /**
     * Get all the used ports of this supervisor.
     *
     * @param cluster
     * @return
     */
    public List<Integer> getUsedPorts(SupervisorDetails supervisor) {
        Map<String, SchedulerAssignment> assignments = this.getAssignments();
        List<Integer> usedPorts = new ArrayList<Integer>();

        for (SchedulerAssignment assignment : assignments.values()) {
            for (WorkerSlot slot : assignment.getExecutorToSlots().values()) {
                if (slot.getNodeId().equals(supervisor.getId())) {
                    usedPorts.add(slot.getPort());
                }
            }
        }

        return usedPorts;
    }

    /**
     * Return the available ports of this supervisor.
     *
     * @param cluster
     * @return
     */
    public List<Integer> getAvailablePorts(SupervisorDetails supervisor) {
        List<Integer> usedPorts = this.getUsedPorts(supervisor);

        List<Integer> ret = new ArrayList<Integer>();
        ret.addAll(supervisor.allPorts);
        ret.removeAll(usedPorts);

        return ret;
    }

    /**
     * Return all the available slots on this supervisor.
     *
     * @param cluster
     * @return
     */
    public List<WorkerSlot> getAvailableSlots(SupervisorDetails supervisor) {
        List<Integer> ports = this.getAvailablePorts(supervisor);
        List<WorkerSlot> slots = new ArrayList<WorkerSlot>(ports.size());

        for (Integer port : ports) {
            slots.add(new WorkerSlot(supervisor.getId(), port));
        }

        return slots;
    }

    /**
     * get the unassigned executors of the topology.
     */
    public List<ExecutorDetails> getUnassignedExecutors(TopologyDetails topology) {
        if (topology == null) {
            return new ArrayList<ExecutorDetails>(0);
        }

        Collection<ExecutorDetails> allExecutors = topology.getExecutors();
        SchedulerAssignment assignment = this.getAssignmentById(topology.getId());

        if (assignment != null) {
            Set<ExecutorDetails> assignedExecutors = assignment.getExecutors();
            allExecutors.removeAll(assignedExecutors);
        }

        List<ExecutorDetails> ret = new ArrayList<ExecutorDetails>(allExecutors.size());
        ret.addAll(allExecutors);

        return ret;
    }

    /**
     * Gets the number of workers assigned to this topology.
     *
     * @param topology
     * @return
     */
    public int getAssignedNumWorkers(TopologyDetails topology) {
        SchedulerAssignment assignment = this.getAssignmentById(topology.getId());
        if (topology == null || assignment == null) {
            return 0;
        }

        Set<WorkerSlot> slots = new HashSet<WorkerSlot>();
        slots.addAll(assignment.getExecutorToSlots().values());

        return slots.size();
    }

    /**
     * Assign the slot to the executors for this topology.
     *
     * @throws RuntimeException if the specified slot is already occupied.
     */
    public void assign(WorkerSlot slot, String topologyId, Collection<ExecutorDetails> executors) {
        if (this.isSlotOccupied(slot)) {
            new RuntimeException("slot: [" + slot.getNodeId() + ", " + slot.getPort() + "] is already occupied.");
        }

        SchedulerAssignment assignment = this.getAssignmentById(topologyId);
        if (assignment == null) {
            assignment = new SchedulerAssignment(topologyId, new HashMap<ExecutorDetails, WorkerSlot>());
            this.assignments.put(topologyId, assignment);
        }

        assignment.assign(slot, executors);
    }

    /**
     * Gets all the available slots in the cluster.
     *
     * @return
     */
    public List<WorkerSlot> getAvailableSlots() {
        List<WorkerSlot> slots = new ArrayList<WorkerSlot>();
        for (SupervisorDetails supervisor : this.supervisors.values()) {
            slots.addAll(this.getAvailableSlots(supervisor));
        }

        return slots;
    }

    /**
     * Free the specified slot.
     *
     * @param slot
     */
    public void freeSlot(WorkerSlot slot) {
        SupervisorDetails supervisor = this.supervisors.get(slot.getNodeId());

        if (supervisor != null) {
            // remove the slot from the existing assignments
            for (SchedulerAssignment assignment : this.assignments.values()) {
                if (assignment.isSlotOccupied(slot)) {
                    assignment.removeSlot(slot);
                    break;
                }
            }
        }
    }

    /**
     * free the slots.
     *
     * @param slots
     */
    public void freeSlots(Collection<WorkerSlot> slots) {
        for (WorkerSlot slot : slots) {
            this.freeSlot(slot);
        }
    }

    /**
     * Checks the specified slot is occupied.
     *
     * @param slot the slot be to checked.
     * @return
     */
    public boolean isSlotOccupied(WorkerSlot slot) {
        for (SchedulerAssignment assignment : this.assignments.values()) {
            if (assignment.isSlotOccupied(slot)) {
                return true;
            }
        }

        return false;
    }

    /**
     * get the current assignment for the topology.
     */
    public SchedulerAssignment getAssignmentById(String topologyId) {
        if (this.assignments.containsKey(topologyId)) {
            return this.assignments.get(topologyId);
        }

        return null;
    }

    /**
     * Get a specific supervisor with the <code>nodeId</code>
     */
    public SupervisorDetails getSupervisorById(String nodeId) {
        if (this.supervisors.containsKey(nodeId)) {
            return this.supervisors.get(nodeId);
        }

        return null;
    }

    /**
     * Get all the supervisors on the specified <code>host</code>.
     *
     * @param host hostname of the supervisor
     * @return the <code>SupervisorDetails</code> object.
     */
    public List<SupervisorDetails> getSupervisorsByHost(String host) {
        List<String> nodeIds = this.hostToIds.get(host);
        List<SupervisorDetails> ret = new ArrayList<SupervisorDetails>();

        if (nodeIds != null) {
            for (String nodeId : nodeIds) {
                ret.add(this.getSupervisorById(nodeId));
            }
        }

        return ret;
    }

    /**
     * Set assignment for the specified topology.
     *
     * @param topologyId the id of the topology the assignment is for.
     * @param assignment the assignment to be assigned.
     */
    public void setAssignmentById(String topologyId, SchedulerAssignment assignment) {
        this.assignments.put(topologyId, assignment);
    }

    /**
     * Get all the assignments.
     */
    public Map<String, SchedulerAssignment> getAssignments() {
        return this.assignments;
    }

    /**
     * Get all the supervisors.
     */
    public Map<String, SupervisorDetails> getSupervisors() {
        return this.supervisors;
    }
}