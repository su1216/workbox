package workbox.plugin;

import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.Variant;
import com.android.build.api.variant.VariantBuilder;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.util.List;

public final class WorkboxPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        List<String> taskNames = project.getGradle().getStartParameter().getTaskNames();
        for (String taskName : taskNames) {
            if (taskName.contains("Release")) {
                System.out.println("do not install workbox plugin in release version");
                return;
            }
        }
        System.out.println("install workbox plugin.");
        System.out.println();

        ExtensionContainer container = project.getExtensions();
        AndroidComponentsExtension<?, VariantBuilder, Variant> androidComponentsExtension = container.getByType(AndroidComponentsExtension.class);
        androidComponentsExtension.onVariants(androidComponentsExtension.selector().all(), variant -> {
            String variantName = variant.getName().substring(0, 1).toUpperCase() + variant.getName().substring(1);
            String libDependencyTaskName = "create" + variantName + "Asset";
            TaskProvider<GenerateLibDependencyTask> taskProvider = project.getTasks().register(libDependencyTaskName, GenerateLibDependencyTask.class, generateLibDependencyTask -> generateLibDependencyTask.getOutputs().upToDateWhen(task -> false));
            variant.getSources().getAssets().addGeneratedSourceDirectory(taskProvider, GenerateLibDependencyTask::getLibDependencyDir);

            String gitLogTaskName = variant.getName() + "GenerateGitLog";
            TaskProvider<GenerateGitLogTask> gitLogTaskProvider = project.getTasks().register(gitLogTaskName, GenerateGitLogTask.class, generateGitLogTask -> generateGitLogTask.getOutputs().upToDateWhen(task -> false));
            variant.getSources().getAssets().addGeneratedSourceDirectory(gitLogTaskProvider, GenerateGitLogTask::getGitLogDir);
        });
    }
}
