package org.swaggertools.core.config;

import org.junit.Test;
import org.swaggertools.core.targets.client.ClientGenerator;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConfigTest {
    @Test
    public void name() {
        ClientGenerator generator = new ClientGenerator();
        List<Configuration> configurations = generator.getConfigurations();
        assertEquals(7, configurations.size());
        HelpPrinter printer = new HelpPrinter("");
        printer.printProperties(configurations);
        String help = printer.getHelp();
        assertEquals(
                "  dialect                                 Client implementation dialect [RestTemplate,WebClient,HttpClient] (default: 'RestTemplate')\n" +
                "  date-time-class                         String 'date-time' full class name\n" +
                "  location                                Server classes target directory\n" +
                "  client-suffix                           Client classes name suffix (default: 'Client')\n" +
                "  client-package                          Client classes package name\n" +
                "  factory-name                            Client factory class name. If empty no factory is generated.\n" +
                "  model-package                           Models package name\n",
                help);
    }
}
