package com.metaweb.gridworks.history;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONWriter;

import com.metaweb.gridworks.Gridworks;
import com.metaweb.gridworks.Jsonizable;
import com.metaweb.gridworks.ProjectManager;
import com.metaweb.gridworks.model.Project;

public class History implements Jsonizable {
    static public Change readOneChange(LineNumberReader reader) throws Exception {
        /* String version = */ reader.readLine();

        String className = reader.readLine();
        Class<? extends Change> klass = getChangeClass(className);

        Method load = klass.getMethod("load", LineNumberReader.class);

        return (Change) load.invoke(null, reader);
    }

    static public void writeOneChange(Writer writer, Change change) throws Exception {
        writer.write(Gridworks.s_version); writer.write('\n');
        writer.write(change.getClass().getName()); writer.write('\n');

        Properties options = new Properties();
        options.setProperty("mode", "save");

        change.save(writer, options);
    }

    @SuppressWarnings("unchecked")
    static public Class<? extends Change> getChangeClass(String className) throws ClassNotFoundException {
        return (Class<? extends Change>) Class.forName(className);
    }

    protected long               _projectID;
    protected List<HistoryEntry> _pastEntries;
    protected List<HistoryEntry> _futureEntries;

    public History(Project project) {
        _projectID = project.id;
        _pastEntries = new ArrayList<HistoryEntry>();
        _futureEntries = new ArrayList<HistoryEntry>();
    }

    public void addEntry(HistoryEntry entry) {
        for (HistoryEntry entry2 : _futureEntries) {
            entry2.delete();
        }

        entry.apply(ProjectManager.singleton.getProject(_projectID));
        _pastEntries.add(entry);
        _futureEntries.clear();

        setModified();
    }

    protected void setModified() {
        ProjectManager.singleton.getProjectMetadata(_projectID).updateModified();
    }

    public List<HistoryEntry> getLastPastEntries(int count) {
        if (count <= 0) {
            return new LinkedList<HistoryEntry>(_pastEntries);
        } else {
            return _pastEntries.subList(Math.max(_pastEntries.size() - count, 0), _pastEntries.size());
        }
    }

    public void undoRedo(long lastDoneEntryID) {
        if (lastDoneEntryID == 0) {
            undo(_pastEntries.size());
        } else {
            for (int i = 0; i < _pastEntries.size(); i++) {
                if (_pastEntries.get(i).id == lastDoneEntryID) {
                    undo(_pastEntries.size() - i - 1);
                    return;
                }
            }

            for (int i = 0; i < _futureEntries.size(); i++) {
                if (_futureEntries.get(i).id == lastDoneEntryID) {
                    redo(i + 1);
                    return;
                }
            }
        }
    }

    protected HistoryEntry getEntry(long entryID) {
        for (int i = 0; i < _pastEntries.size(); i++) {
            if (_pastEntries.get(i).id == entryID) {
                return _pastEntries.get(i);
            }
        }

        for (int i = 0; i < _futureEntries.size(); i++) {
            if (_futureEntries.get(i).id == entryID) {
                return _futureEntries.get(i);
            }
        }
        return null;
    }

    protected void undo(int times) {
        Project project = ProjectManager.singleton.getProject(_projectID);

        while (times > 0 && _pastEntries.size() > 0) {
            HistoryEntry entry = _pastEntries.get(_pastEntries.size() - 1);

            entry.revert(project);

            times--;

            _pastEntries.remove(_pastEntries.size() - 1);
            _futureEntries.add(0, entry);
        }
        setModified();
    }

    protected void redo(int times) {
        Project project = ProjectManager.singleton.getProject(_projectID);

        while (times > 0 && _futureEntries.size() > 0) {
            HistoryEntry entry = _futureEntries.get(0);

            entry.apply(project);

            times--;

            _pastEntries.add(entry);
            _futureEntries.remove(0);
        }
        setModified();
    }

    public void write(JSONWriter writer, Properties options)
            throws JSONException {

        writer.object();

        writer.key("past"); writer.array();
        for (HistoryEntry entry : _pastEntries) {
            entry.write(writer, options);
        }
        writer.endArray();

        writer.key("future"); writer.array();
        for (HistoryEntry entry : _futureEntries) {
            entry.write(writer, options);
        }
        writer.endArray();

        writer.endObject();
    }

    public void save(Writer writer, Properties options) throws IOException {
        writer.write("pastEntryCount="); writer.write(Integer.toString(_pastEntries.size())); writer.write('\n');
        for (HistoryEntry entry : _pastEntries) {
            entry.save(writer, options); writer.write('\n');
        }

        writer.write("futureEntryCount="); writer.write(Integer.toString(_futureEntries.size())); writer.write('\n');
        for (HistoryEntry entry : _futureEntries) {
            entry.save(writer, options); writer.write('\n');
        }

        writer.write("/e/\n");
    }

    public void load(Project project, LineNumberReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null && !"/e/".equals(line)) {
            int equal = line.indexOf('=');
            CharSequence field = line.subSequence(0, equal);
            String value = line.substring(equal + 1);

            if ("pastEntryCount".equals(field)) {
                int count = Integer.parseInt(value);

                for (int i = 0; i < count; i++) {
                    _pastEntries.add(HistoryEntry.load(project, reader.readLine()));
                }
            } else if ("futureEntryCount".equals(field)) {
                int count = Integer.parseInt(value);

                for (int i = 0; i < count; i++) {
                    _futureEntries.add(HistoryEntry.load(project, reader.readLine()));
                }
            }
        }
    }
}