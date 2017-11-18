package ru.compscicenter.edide;

import com.intellij.openapi.editor.LogicalPosition;

import java.util.ArrayList;

/**
 * User: lia
 * Date: 02.12.13
 * Time: 18:16
 */
public class Task {
    ArrayList<TaskFile> files;
    private StringBuilder taskText;
    private  String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public Task(int n) {
        files =  new ArrayList<TaskFile>(n);
        taskText = new StringBuilder();
    }
    public void addTaskFile(TaskFile taskFile) {
        files.add(taskFile);
    }
    public int getFileNum() {
        return files.size();
    }

    public TaskFile getTaskFile(int index) {
        return files.get(index);
    }
    public void addTaskTextLine(String line) {
        taskText.append(line);
    }
    public  String getTaskText() {
        return taskText.toString();
    }
    public boolean contains(String filename) {
        for (TaskFile f : files) {
            if (f.getName().equals(filename)) {
                return true;
            }
        }
        return false;
    }

    public TaskFile getTaskFilebyName(String name) {
        for (TaskFile f: files) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
}