package workbox.plugin;

import com.google.gson.GsonBuilder;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GenerateLibDependencyTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getLibDependencyDir();

    @TaskAction
    public void taskAction() {
        Map<String, List<Repository>> repositoryMap = new Hashtable<>();
        Map<String, Set<Lib>> libMap = new Hashtable<>();
        Set<Project> allprojects = getProject().getRootProject().getAllprojects();
        for (Project subProject : allprojects) {
            collectRepositories(subProject, repositoryMap);
            collectLibs(subProject, libMap);
        }

        Set<String> projects = repositoryMap.keySet();
        for (String name : projects) {
            List<Repository> repositories = repositoryMap.get(name);
            for (Repository repository : repositories) {
                System.out.println("repository: " + repository);
            }
            System.out.println("project=" + name);
            Set<Lib> libs = libMap.get(name);
            for (Lib lib : libs) {
                System.out.println("lib: " + lib);
            }
        }

        System.out.println("finish collecting");

        try {
            File generatedAssetsDir = getLibDependencyDir().get().getAsFile();
            File generatedDir = new File(generatedAssetsDir, "generated");
            generatedDir.mkdirs();
            File generated = new File(generatedDir, "dependencies.json");
            Set<String> nameSet = new HashSet<>();
            nameSet.addAll(repositoryMap.keySet());
            nameSet.addAll(libMap.keySet());
            System.out.println("libMap=" + libMap);
            List<Module> moduleList = new ArrayList<>();
            for (String projectName : nameSet) {
                Module module = new Module();
                module.name = projectName;
                Set<Lib> libSet = libMap.get(projectName);
                module.libs = new ArrayList<>();
                addAll(libSet, module.libs);
                module.repositories = repositoryMap.get(projectName);
                moduleList.add(module);
            }
            System.out.println("write dependencies.json");
            String jsonString = new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(moduleList);
            Files.write(generated.toPath(), jsonString.getBytes());
            System.out.println("jsonFile: " + generated.getAbsolutePath());
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                cause.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException("something wrong while add/allAll elements to ArrayList");
        }
    }

    // workaround for java.lang.ArrayIndexOutOfBoundsException (no error message)
    private static void addAll(Set<Lib> libSet, List<Lib> list) {
        for (Lib lib : libSet) {
            list.add(lib);
        }
    }

    private void collectRepositories(Project subProject, Map<String, List<Repository>> repositoryMap) {
        RepositoryHandler repositoryHandler = subProject.getRepositories();
        String projectName = subProject.getName();
        List<Repository> list;
        if (repositoryMap.containsKey(projectName)) {
            list = repositoryMap.get(projectName);
        } else {
            list = new ArrayList<>();
            repositoryMap.put(projectName, list);
        }
        repositoryHandler.forEach(artifactRepository -> {
            if (artifactRepository instanceof MavenArtifactRepository) {
                MavenArtifactRepository mavenArtifactRepository = (MavenArtifactRepository) artifactRepository;
                Repository repository = new Repository();
                repository.name = mavenArtifactRepository.getName();
                repository.url = String.valueOf(mavenArtifactRepository.getUrl());
                if (!list.contains(repository)) {
                    list.add(repository);
                }
            } else if (artifactRepository instanceof FlatDirectoryArtifactRepository) {
                FlatDirectoryArtifactRepository flatDirectoryArtifactRepository = (FlatDirectoryArtifactRepository) artifactRepository;
                Repository repository = new Repository();
                repository.name = flatDirectoryArtifactRepository.getName();
                repository.dirs = flatDirectoryArtifactRepository.getDirs()
                        .stream()
                        .map(file -> file.getAbsolutePath())
                        .collect(Collectors.toSet());
                if (!list.contains(repository)) {
                    list.add(repository);
                }
            } else {
                System.err.println("projectName: " + subProject.getName() + " name: " + artifactRepository.getName() + " toString: " + artifactRepository.toString());
            }
        });
    }

    private void collectLibs(Project subProject, Map<String, Set<Lib>> libMap) {
        List<String> allProjectNames = subProject.getRootProject()
                .getAllprojects()
                .stream()
                .map(Project::getName)
                .collect(Collectors.toList());
        ConfigurationContainer configurationContainer = subProject.getConfigurations();
        String projectName = subProject.getName();
        Set<Lib> set;
        if (libMap.containsKey(projectName)) {
            set = libMap.get(projectName);
        } else {
            set = new HashSet<>();
            libMap.put(projectName, set);
        }
        configurationContainer.parallelStream().forEach(configuration -> {
            configuration.getAllDependencies().parallelStream().forEach(dependency -> {
                String group = dependency.getGroup();
                if (allProjectNames.contains(group)) {
                    return;
                }
                if (group == null || "null".equalsIgnoreCase(group)) {
                    return;
                }
                Lib lib = new Lib();
                lib.groupId = dependency.getGroup();
                lib.artifactId = dependency.getName();
                lib.version = dependency.getVersion();
                set.add(lib);
            });
        });
    }
}
