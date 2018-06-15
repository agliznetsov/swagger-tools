package org.swaggertools.cli;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.swaggertools.core.config.Configuration;
import org.swaggertools.core.run.Processor;
import org.swaggertools.core.run.ProcessorFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Generator {
    public static void main(String[] args) {
        new Generator().run(args);
    }

    private void run(String[] args) {
        Options options = createOptions();
        try {
            generate(new DefaultParser().parse(options, args));
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(100, "swagger-tools-cli", "", options, "", true);
            System.exit(1);
        }
    }

    private void generate(CommandLine commandLine) {
        Processor processor = null;
        try {
            processor = new ProcessorFactory(readOptions(commandLine)).create();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            System.exit(1);
        }
        if (processor != null) {
            processor.process();
        }
    }

    private Map<Configuration, String> readOptions(CommandLine commandLine) {
        Map<Configuration, String> options = new HashMap<>();
        for (Configuration config : Configuration.values()) {
            String value = commandLine.getOptionValue(config.getKey());
            if (value != null) {
                options.put(config, value);
            }
        }
        return options;
    }

    private Options createOptions() {
        Options options = new Options();
        for (Configuration config : Configuration.values()) {
            Option.Builder builder = Option.builder().longOpt(config.getKey()).hasArg().desc(config.getDescription());
            builder.required(config == Configuration.SOURCE_LOCATION);
            options.addOption(builder.build());
        }
        return options;
    }

}
