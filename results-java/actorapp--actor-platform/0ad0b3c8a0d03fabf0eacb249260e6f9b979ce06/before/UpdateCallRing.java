package im.actor.core.api.updates;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.core.network.parser.*;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import im.actor.core.api.*;

public class UpdateCallRing extends Update {

    public static final int HEADER = 0x31;
    public static UpdateCallRing fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UpdateCallRing(), data);
    }

    private User user;
    private String callId;

    public UpdateCallRing(@NotNull User user, @NotNull String callId) {
        this.user = user;
        this.callId = callId;
    }

    public UpdateCallRing() {

    }

    @NotNull
    public User getUser() {
        return this.user;
    }

    @NotNull
    public String getCallId() {
        return this.callId;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.user = values.getObj(1, new User());
        this.callId = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.user == null) {
            throw new IOException();
        }
        writer.writeObject(1, this.user);
        if (this.callId == null) {
            throw new IOException();
        }
        writer.writeString(2, this.callId);
    }

    @Override
    public String toString() {
        String res = "update CallRing{";
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}