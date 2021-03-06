package im.actor.core.api.rpc;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.core.network.parser.*;

import java.io.IOException;

public class ResponseBool extends Response {

    public static final int HEADER = 0xd1;
    public static ResponseBool fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ResponseBool(), data);
    }

    private boolean value;

    public ResponseBool(boolean value) {
        this.value = value;
    }

    public ResponseBool() {

    }

    public boolean value() {
        return this.value;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.value = values.getBool(1);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBool(1, this.value);
    }

    @Override
    public String toString() {
        String res = "response Bool{";
        res += "value=" + this.value;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}