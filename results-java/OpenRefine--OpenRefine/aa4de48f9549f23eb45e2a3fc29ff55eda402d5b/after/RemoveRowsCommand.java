package com.metaweb.gridworks.commands.row;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.metaweb.gridworks.commands.EngineDependentCommand;
import com.metaweb.gridworks.model.AbstractOperation;
import com.metaweb.gridworks.model.Project;
import com.metaweb.gridworks.operations.row.RowRemovalOperation;

public class RemoveRowsCommand extends EngineDependentCommand {

    @Override
    protected AbstractOperation createOperation(Project project,
            HttpServletRequest request, JSONObject engineConfig) throws Exception {

        return new RowRemovalOperation(engineConfig);
    }
}