package org.swaggertools.cli;

import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.HelpPrinter;
import org.swaggertools.core.run.Processor;
import org.swaggertools.core.run.ProcessorFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Generator {
    public static void main(String[] args) {
        new Generator().run(args);
    }

    public void run(String[] args) {
        try {
            Map<String, String> options = readOptions(args);
            if (options.isEmpty() || options.containsKey("help")) {
                printHelp();
            } else {
                Processor processor = new ProcessorFactory().create(options);
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
        printer.print("source.<property>=<value>", "Set source property");
        printer.print("target.<name>.class=<value>", "Set target <name> class name");
        printer.print("target.<name>.<property>=<value>", "Set target <name> property");
        printer.print("");
        printer.printProperties();
        System.out.println(printer.getHelp());
    }

    protected Map<String, String> readOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        for(String arg : args) {
            readOption(arg, options);
        }
        return options;
    }

    private void readOption(String arg, Map<String, String> options) {
        if (arg.startsWith("--")) {
            arg = arg.substring(2);
        }
        int i = arg.indexOf('=');
        if (i > 0) {
            String key = arg.substring(0, i).trim();
            String value = arg.substring(i + 1).trim();
            options.put(key, value);
        } else {
            options.put(arg, null);
        }
    }

}
