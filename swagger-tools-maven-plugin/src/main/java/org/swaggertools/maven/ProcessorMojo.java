package org.swaggertools.maven;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.swaggertools.core.run.Configuration;
import org.swaggertools.core.run.ProcessorFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.swaggertools.core.run.Configuration.*;

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
                Map<Configuration, String> configurations = parseOptions(options);
                new ProcessorFactory(configurations).create().process();
                addCompileSourceRoot(configurations);
            } catch (Exception e) {
                getLog().error(e);
                throw new MojoFailureException(e.getMessage());
            }
        }
    }

    private void printHelp() {
        getLog().info("Possible options:");
        for (Configuration config : Configuration.values()) {
            getLog().info(StringUtils.rightPad(config.getKey(), 40) + "  :  " + config.getDescription());
        }
    }

    private Map<Configuration, String> parseOptions(Map<String, String> options) {
        Map<Configuration, String> res = new HashMap<>();
        for (Configuration config : Configuration.values()) {
            String value = options.get(config.getKey());
            if (value != null) {
                res.put(config, value);
            }
        }
        return res;
    }

    private void addCompileSourceRoot(Map<Configuration, String> configurations) {
        Set<String> targets = findTargets(configurations, TARGET_MODEL_LOCATION, TARGET_CLIENT_LOCATION, TARGET_SERVER_LOCATION);
        for(String target : targets) {
            project.addCompileSourceRoot(target);
        }
    }

    private Set<String> findTargets(Map<Configuration, String> configurations, Configuration... keys) {
        Set<String> targets = new HashSet<>();
        for(Configuration key : keys) {
            String target = configurations.get(key);
            if (target != null) {
                targets.add(target);
            }
        }
        return targets;
    }
}
