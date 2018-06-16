package org.swaggertools.core.config;

import org.apache.commons.lang3.StringUtils;
import org.swaggertools.core.source.ApiDefinitionSource;
import org.swaggertools.core.target.model.JacksonModelGenerator;
import org.swaggertools.core.target.spring.ClientGenerator;
import org.swaggertools.core.target.spring.ServerGenerator;

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
        printProperties(new ApiDefinitionSource());

        sb.append("\n");
        sb.append("Target [model] properties:\n");
        printProperties(new JacksonModelGenerator());

        sb.append("\n");
        sb.append("Target [client] properties:\n");
        printProperties(new ClientGenerator());

        sb.append("\n");
        sb.append("Target [server] properties:\n");
        printProperties(new ServerGenerator());
    }

    private void printProperties(AutoConfigurable<?> autoConfigurable) {
        for(Configuration config : autoConfigurable.getConfigurations()) {
            print(config.getName(), config.getDescription());
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
