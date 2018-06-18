package org.swaggertools.core.config;

import org.apache.commons.lang3.StringUtils;
import org.swaggertools.core.run.ProcessorFactory;
import org.swaggertools.core.source.ApiDefinitionSource;

import java.util.List;

public class HelpPrinter {
    private String prefix;
    private StringBuilder sb;

    public HelpPrinter(String prefix) {
        this.prefix = prefix;
        sb = new StringBuilder();
    }

    public String getHelp() {
        return sb.toString();
    }

    public void printProperties() {
        sb.append("Source properties:\n");
        printProperties(new ApiDefinitionSource().getConfigurations());

        ProcessorFactory.getTargets().forEach((k, v) -> {
            sb.append("\n");
            sb.append("Target [" + k + "] properties:\n");
            printProperties(v);
        });
    }

    private void printProperties(List<Configuration> configurations) {
        for (Configuration config : configurations) {
            String desc = config.getDescription();
            if (config.getDefaultValue() != null && !config.getDefaultValue().isEmpty()) {
                desc += " (default: '" + config.getDefaultValue() + "')";
            }
            print(config.getName(), desc);
        }
    }

    public void print(String name) {
        sb.append(name);
        sb.append("\n");
    }

    public void print(String name, String description) {
        sb.append("  ");
        sb.append(StringUtils.rightPad(prefix + name, 40));
        sb.append(description);
        sb.append("\n");
    }
}
