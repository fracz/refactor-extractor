package io.mycat.migrate;

import io.mycat.route.function.PartitionByCRC32PreSlot;
import io.mycat.route.function.PartitionByCRC32PreSlot.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by magicdoom on 2016/9/15.
 */
public class MigrateTask implements Serializable {

    private String from;
    private String to;
    private String table;
    private List<Range> slots=new ArrayList<>();

    private String method;
    private String fclass=PartitionByCRC32PreSlot.class.getName();

    private String schema;


    private int slaveId;

    private transient String zkpath;
    private transient String binlogFile;
    private transient int pos;

    public List<Range> getSlots() {
        return slots;
    }

    public void setSlots(List<Range> slots) {
        this.slots = slots;
    }

    public int getSize()
    {   int size=0;
        for (Range slot : slots) {
           size=size+slot.size;
        }
        return size;
    }

    public String getBinlogFile() {
        return binlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getFclass() {
        return fclass;
    }

    public void setFclass(String fclass) {
        this.fclass = fclass;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public int getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }

    public String getZkpath() {
        return zkpath;
    }

    public void setZkpath(String zkpath) {
        this.zkpath = zkpath;
    }

    public void addSlots(Range range)
    {
        slots.add(range);
    }

    public void addSlots(List<Range> ranges)
    {
        slots.addAll(ranges);
    }


}