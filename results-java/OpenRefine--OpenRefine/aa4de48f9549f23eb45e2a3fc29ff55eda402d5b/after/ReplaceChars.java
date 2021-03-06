package com.metaweb.gridworks.expr.functions.strings;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import com.metaweb.gridworks.expr.EvalError;
import com.metaweb.gridworks.gel.ControlFunctionRegistry;
import com.metaweb.gridworks.gel.Function;

public class ReplaceChars implements Function {

    public Object call(Properties bindings, Object[] args) {
        if (args.length == 3) {
            Object o1 = args[0];
            Object o2 = args[1];
            Object o3 = args[2];
            if (o1 != null && o2 != null && o3 != null && o2 instanceof String && o3 instanceof String) {
                String str = (o1 instanceof String) ? (String) o1 : o1.toString();
                return StringUtils.replaceChars(str, (String) o2, (String) o3);
            }
        }
        return new EvalError(ControlFunctionRegistry.getFunctionName(this) + " expects 3 strings");
    }


    public void write(JSONWriter writer, Properties options)
        throws JSONException {

        writer.object();
        writer.key("description"); writer.value("Returns the string obtained by replacing all chars in f with the char in s at that same position");
        writer.key("params"); writer.value("string s, string f, string r");
        writer.key("returns"); writer.value("string");
        writer.endObject();
    }
}