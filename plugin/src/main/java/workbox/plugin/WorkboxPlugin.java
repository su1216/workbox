package workbox.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;

import com.google.gson.GsonBuilder;

import org.gradle.api.DefaultTask;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class WorkboxPlugin implements Plugin<Project> {

    private static class Module {
        private String name;
        private List<Repository> repositories;
        private List<Lib> libs;

        @Override
        public String toString() {
            return "Module{" +
                    "name='" + name + '\'' +
                    ", repositories=" + repositories +
                    ", libs=" + libs +
                    '}';
        }
    }

    private static class Repository {
        private String name;
        private String url; //flatDir url = null
        private Set<File> dirs;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Repository that = (Repository) o;
            return Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url);
        }

        @Override
        public String toString() {
            return "Repository{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", dirs=" + dirs +
                    '}';
        }
    }

    private static class Lib {
        private String groupId;
        private String artifactId;
        private String version;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Lib lib = (Lib) o;
            return Objects.equals(groupId, lib.groupId) &&
                    Objects.equals(artifactId, lib.artifactId) &&
                    Objects.equals(version, lib.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, artifactId, version);
        }

        @Override
        public String toString() {
            return "Lib{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", version='" + version + '\'' +
                    '}';
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
                repository.dirs = flatDirectoryArtifactRepository.getDirs();
                if (!list.contains(repository)) {
                    list.add(repository);
                }
            } else {
                System.err.println("projectName: " + subProject.getName() + " name: " + artifactRepository.getName() + " toString: " + artifactRepository.toString());
            }
        });
    }

    private void collectLibs(Project subProject, Map<String, Set<Lib>> libMap) {
        List<String> allProjectNames = subProject.getRootProject().getAllprojects().stream().map(Project::getName).collect(Collectors.toList());
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

    private void generateDependencyJson(Project project, Map<String, List<Repository>> repositoryMap, Map<String, Set<Lib>> libMap) {
        Set<Project> set = project.getRootProject().getAllprojects();
        for (Project p : set) {
            ExtensionContainer extensionContainer = p.getExtensions();
            try {
                AppExtension appExtension = extensionContainer.getByType(AppExtension.class);
                DomainObjectSet<ApplicationVariant> variants = appExtension.getApplicationVariants();
                for (BaseVariant variant : variants) {
                    makeGenerateTask(project, p, variant, repositoryMap, libMap);
                }
            } catch (UnknownDomainObjectException e) {
                //ignore
            }
        }
    }

    private void makeGenerateTask(Project project, Project p, BaseVariant variant, Map<String, List<Repository>> repositoryMap, Map<String, Set<Lib>> libMap) {
        String buildType = variant.getBuildType().getName();
        String taskName;
        if (variant.getFlavorName() == null || "".equals(variant.getFlavorName())) {
            taskName = buildType;
        } else {
            taskName = variant.getFlavorName() + buildType.substring(0, 1).toUpperCase() + buildType.substring(1);
        }
        Task generateTask = project.getTasks().findByName(taskName);
        if (generateTask != null) {
            System.out.println("found task: " + taskName);
            return;
        }
        taskName += "GenerateDependencies";
        generateTask = project.getTasks().create(taskName, DefaultTask.class);
        File dirFile = Paths.get(getGenerateTaskDirFile(project, variant).getAbsolutePath(), "assets", "generated").toFile();
        variant.registerJavaGeneratingTask(generateTask, dirFile);
        generateTask.doLast(task -> {
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
        });
        variant.register(generateTask);
    }

    private File getGenerateTaskDirFile(Project project, BaseVariant variant) {
        File fullFile = Paths.get(project.getProjectDir().getAbsolutePath(), "src", variant.getDirName()).toFile();
        System.out.println("fullFile: " + fullFile);
        if (fullFile.exists()) {
            return fullFile;
        }
        File flavorFile = Paths.get(project.getProjectDir().getAbsolutePath(), "src", variant.getFlavorName()).toFile();
        System.out.println("flavorFile: " + flavorFile);
        if (flavorFile.exists()) {
            return flavorFile;
        }

        File nameFile = Paths.get(project.getProjectDir().getAbsolutePath(), "src", variant.getName()).toFile();
        System.out.println("nameFile: " + nameFile);
        if (nameFile.exists()) {
            return nameFile;
        }

        File buildTypeFile = Paths.get(project.getProjectDir().getAbsolutePath(), "src", variant.getBuildType().getName()).toFile();
        System.out.println("buildTypeFile: " + buildTypeFile);
        return buildTypeFile;
    }

    @Override
    public void apply(Project project) {
        List<String> taskNames = project.getGradle().getStartParameter().getTaskNames();
        for (String taskName : taskNames) {
            if (taskName.contains("Release")) {
                System.out.println("do not install lib info plugin in release version");
                return;
            }
        }
        System.out.println("install lib info plugin.");
        project.getGradle().projectsEvaluated(gradle -> {
            System.out.println();
            Map<String, List<Repository>> repositoryMap = new Hashtable<>();
            Map<String, Set<Lib>> libMap = new Hashtable<>();
            Set<Project> allprojects = project.getRootProject().getAllprojects();
            for (Project subProject : allprojects) {
                collectRepositories(subProject, repositoryMap);
                collectLibs(subProject, libMap);
            }

            Set<String> projects = repositoryMap.keySet();
            for (String name : projects) {
                System.out.println("projectName: " + name);
                List<Repository> repositories = repositoryMap.get(name);
                for (Repository repository : repositories) {
                    System.out.println("repository: " + repository);
                }
                System.out.println();
                Set<Lib> libs = libMap.get(name);
                for (Lib lib : libs) {
                    System.out.println("lib: " + lib);
                }
            }

            generateDependencyJson(project, repositoryMap, libMap);
        });
    }
}
