package workbox.plugin;

import com.google.gson.GsonBuilder;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerateLibDependencyAction implements Action<Task> {

    private final File dirFile;
    private final Map<String, List<Repository>> repositoryMap;
    private final Map<String, Set<Lib>> libMap;

    public GenerateLibDependencyAction(File dirFile, Map<String, List<Repository>> repositoryMap, Map<String, Set<Lib>> libMap) {
        this.dirFile = dirFile;
        this.repositoryMap = repositoryMap;
        this.libMap = libMap;
    }

    @Override
    public void execute(@NotNull Task task) {
        System.out.println("doTask: " + task.getName());
        if (!dirFile.exists()) {
            System.out.println("mkdir: " + dirFile.getAbsolutePath());
            dirFile.mkdirs();
        }
        Set<String> nameSet = new HashSet<>();
        nameSet.addAll(repositoryMap.keySet());
        nameSet.addAll(libMap.keySet());
        List<Module> moduleList = new ArrayList<>();
        for (String projectName : nameSet) {
            Module module = new Module();
            module.name = projectName;
            module.libs = new ArrayList<>(libMap.get(projectName));
            module.repositories = new ArrayList<>(repositoryMap.get(projectName));
            moduleList.add(module);
        }
        String jsonString = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(moduleList);
        File jsonFile = new File(dirFile, "dependencies.json");
        try {
            Files.write(jsonFile.toPath(), jsonString.getBytes());
            System.out.println("jsonFile: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
