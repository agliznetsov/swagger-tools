package org.swaggertools.core.config;

import org.junit.Test;
import org.swaggertools.core.targets.ClientGenerator;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConfigTest {
    @Test
    public void name() {
        ClientGenerator generator = new ClientGenerator();
        List<Configuration> configurations = generator.getConfigurations();
        assertEquals(6, configurations.size());
        HelpPrinter printer = new HelpPrinter("");
        printer.printProperties(configurations);
        String help = printer.getHelp();
        assertEquals(
                "  location                                Server classes target directory\n" +
                "  client-package                          Client classes package name\n" +
                "  model-package                           Models package name\n" +
                "  client-suffix                           Client classes name suffix (default: 'Client')\n" +
                "  dialect                                 Client implementation dialect [RestTemplate,WebClient] (default: 'RestTemplate')\n" +
                "  factory-name                            Client factory class name. If empty no factory is generated.\n",
                help);
    }
}
