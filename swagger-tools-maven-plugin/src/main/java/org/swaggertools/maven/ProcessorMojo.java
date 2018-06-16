package org.swaggertools.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.swaggertools.core.config.HelpPrinter;
import org.swaggertools.core.run.ProcessorFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ProcessorMojo extends AbstractMojo {
    @Parameter
    private Boolean skip;

    @Parameter
    private Boolean help;

    @Parameter
    private Map<String, String> options;

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip != null && skip) {
            getLog().info("Swagger processing is skipped.");
        } else if ((help != null && help) || options == null) {
            printHelp();
        } else {
            getLog().info("Process swagger definition");
            try {
                new ProcessorFactory().create(options).process();
                addCompileSourceRoot();
            } catch (Exception e) {
                getLog().error(e);
                throw new MojoFailureException(e.getMessage());
            }
        }
    }

    private void printHelp() {
        HelpPrinter printer = new HelpPrinter("");
        printer.printProperties();
        getLog().info(printer.getHelp());
    }

    private void addCompileSourceRoot() {
        Set<String> targets = findTargets();
        for (String target : targets) {
            project.addCompileSourceRoot(target);
        }
    }

    private Set<String> findTargets() {
        Set<String> targets = new HashSet<>();
        options.forEach((k, v) -> {
            if (k.endsWith(".target")) {
                targets.add(v);
            }
        });
        return targets;
    }
}
