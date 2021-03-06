# swagger-tools

[![Build Status](https://travis-ci.org/agliznetsov/swagger-tools.svg?branch=master)](https://travis-ci.org/agliznetsov/swagger-tools)
[![Download CLI](https://img.shields.io/maven-central/v/com.github.agliznetsov.swagger-tools/swagger-tools-cli.svg)](https://repo1.maven.org/maven2/com/github/agliznetsov/swagger-tools/swagger-tools-cli)

## Overview

This project provide a set of tools to generate java code from API definition.

### Source

- Swagger 2.0 or OpenAPI 3.0 API definition in json/yaml format 
- Extensions
    - **x-ignore** to exclude operations from the code generation process  
    - **x-ignore-server** to exclude operations from the server code generation process  
    - **x-ignore-server-client** to exclude operations from the client code generation process  
    - **x-name** to specify OpenAPI 3 requestBody parameter name
    - **x-base-path** to specify OpenAPI 3 API base path
    - **x-response-entity** to make Client/Server return Spring ResponseEntity object
    - **x-model-package** to specify package name for the model classes

### Targets

- Model classes. Supported dialects:
    - Jackson2
- Java client SDK, can be used for unit testing or to create java client applications. Supported dialects:
    -  Spring RestTemplate
    -  Spring WebClient
    -  Apache HttpClient
- Server API interfaces with HTTP mapping annotations. Supported dialects:
    - Spring WebMVC
    - Spring Webflux
    - JAX-RS

### Run from command line  

To get list of arguments:

`java -jar swagger-tools-cli.jar`

To generate models and client code:

```sh
java -jar swagger-tools-cli.jar \
--source.location=swagger.yaml \
--target.model.location=./generated \
--target.model.model-package=com.example.model \
--target.client.location=./generated \
--target.client.model-package=com.example.model \
--target.client.client-package=com.example.client \
```

### Run from maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.agliznetsov.swagger-tools</groupId>
            <artifactId>swagger-tools-maven-plugin</artifactId>
            <version>0.2.0</version>
            <executions>
                <execution>
                    <id>petstore</id>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <options>
                            <source.location>${project.basedir}/src/main/resources/petstore.yaml</source.location>

                            <target.model.location>${project.build.directory}/generated-sources/swagger</target.model.location>
                            <target.model.model-package>org.swaggertools.demo.model</target.model.model-package>

                            <target.client.location>${project.build.directory}/generated-sources/swagger</target.client.location>
                            <target.client.model-package>org.swaggertools.demo.model</target.client.model-package>
                            <target.client.client-package>org.swaggertools.demo.client</target.client.client-package>
                        </options>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### Plugin configuration parameters:
- **skip**: Skip code generation
- **help**: Print the list of options
- **options**: Key/Value map of arguments for the code generator. Same as for the commandline version. 

Check also a complete sample application: [demo-webmvc](demo/demo-webmvc)

### Run from gradle

There is no specific gradle plugin yet, but you can run code generator from gradle using command line version:

```groovy
configurations {
   swagger
}
 
dependencies {
   swagger 'com.github.agliznetsov.swagger-tools:swagger-tools-cli:0.2.0'
}
 
task "swagger-generate"(type: JavaExec) {
   classpath = configurations.swagger
   main = 'org.swaggertools.cli.Generator'
   args = [
         "--source.location", "src/main/resources/swagger.yaml",
         "--target.model.location", "src/main/java",
         "--target.model.model-package", "com.example.model",
   ]
}
```

### Extensions

Additional targets can be added via java [ServiceLoader](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html):

- Implement [Target](swagger-tools-core/src/main/java/org/swaggertools/core/run/Target.java) interface
- List it in the META-INF/services
- Add your jar file to the classpath of the CLI or maven plugin