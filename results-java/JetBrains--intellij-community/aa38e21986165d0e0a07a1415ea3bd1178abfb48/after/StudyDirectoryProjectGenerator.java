package ru.compscicenter.edide;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Log;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.jetbrains.python.newProject.PyNewProjectSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * User: lia
 */
public class StudyDirectoryProjectGenerator implements DirectoryProjectGenerator {

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Study project";
    }

    @Nullable
    @Override
    public Object showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
        return null;
    }


    public void createFile(String name, VirtualFile directory) throws IOException {
        VirtualFile currentFile = directory.createChildData(this, name);
        currentFile.setWritable(true);
        InputStream ip = StudyDirectoryProjectGenerator.class.getResourceAsStream(name);
        BufferedReader bf = new BufferedReader(new InputStreamReader(ip));
        OutputStream os = currentFile.getOutputStream(this);
        PrintWriter printWriter = new PrintWriter(os);
        while (bf.ready()) {
            printWriter.println(bf.readLine());
        }
        bf.close();
        printWriter.close();
    }
    @Override
    public void generateProject(@NotNull Project project, @NotNull final VirtualFile baseDir,
                                @Nullable Object settings, @NotNull Module module) {
        try {
            InputStream metaIS = StudyDirectoryProjectGenerator.class.getResourceAsStream("tasks.meta");
            BufferedReader reader = new BufferedReader(new InputStreamReader(metaIS));
            final int tasksNumber = Integer.parseInt(reader.readLine());
            final Task[] tasks = new Task[tasksNumber];
            for (int task = 0; task < tasksNumber; task++) {
                int n = Integer.parseInt(reader.readLine());
                tasks[task] = new Task(n);
                for (int h = 0; h < n; h++) {
                    tasks[task].setFileName(reader.readLine());
                }
            }
            reader.close();

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int task = 0; task < tasksNumber; task++) {
                            VirtualFile taskDirectory = baseDir.createChildDirectory(this, "task" + (task + 1));
                            for (int file = 0; file < tasks[task].getFileNum(); file++) {
                                final String curFileName = tasks[task].fileNames.get(file);
                                createFile(curFileName, taskDirectory);
                            }

                        }
                        VirtualFile vf = baseDir.createChildDirectory(this, "resources").createChildData(this, "utrunner.py");
                        vf.setWritable(true);
                        InputStream ip_utrunner = StudyDirectoryProjectGenerator.class.getResourceAsStream("utrunner.py");
                        BufferedReader bf_utrunner = new BufferedReader(new InputStreamReader(ip_utrunner));
                        OutputStream os_utrunner = vf.getOutputStream(this);
                        PrintWriter pw_utrunner = new PrintWriter(os_utrunner);
                        while (bf_utrunner.ready()) {
                            pw_utrunner.println(bf_utrunner.readLine());
                        }
                        bf_utrunner.close();
                        pw_utrunner.close();
                    } catch (IOException e) {
                        Log.print("Problems with creating files");
                        Log.print(e.toString());
                        Log.flush();
                    }

                }
            });
            EditorFactory.getInstance().addEditorFactoryListener(new StudyEditorFactoryListener(), project);
        } catch (IOException e) {
            Log.print("Problems with matadata file");
            Log.print(e.toString());
            Log.flush();
        }
    }

    @NotNull
    @Override
    public ValidationResult validate(@NotNull String s) {
        return ValidationResult.OK;
    }
}