package org.swaggertools.cli;

import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.HelpPrinter;
import org.swaggertools.core.run.Processor;
import org.swaggertools.core.run.ProcessorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Generator {
    public static void main(String[] args) {
        new Generator().run(args);
    }

    Map<String, String> options = new HashMap<>();
    List<String> sources = new ArrayList<>();

    public void run(String[] args) {
        try {
            readOptions(args);
            if (options.isEmpty() || options.containsKey("help")) {
                printHelp();
            } else {
                Processor processor = new ProcessorFactory().create(sources.toArray(new String[0]), options);
                processor.process();
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.error("Error", e);
            System.exit(1);
        }
    }

    private void printHelp() {
        HelpPrinter printer = new HelpPrinter("--");
        printer.print("Usage:");
        printer.print("help", "Print help");
        printer.print("sources=<value>", "Set source location. Can be used multiple times.");
        printer.print("source.<property>=<value>", "Set source property");
        printer.print("target.<name>.<property>=<value>", "Set target <name> property");
        printer.print("");
        printer.printProperties();
        System.out.println(printer.getHelp());
    }

    protected Map<String, String> readOptions(String[] args) {
        for(String arg : args) {
            readOption(arg);
        }
        return options;
    }

    private void readOption(String arg) {
        if (arg.startsWith("--")) {
            arg = arg.substring(2);
        }
        int i = arg.indexOf('=');
        if (i > 0) {
            String key = arg.substring(0, i).trim();
            String value = arg.substring(i + 1).trim();
            if ("sources".equals(key)) {
                sources.add(value);
            }
            options.put(key, value);
        } else {
            options.put(arg, null);
        }
    }

}
