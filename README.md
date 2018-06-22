# swagger-tools

[![Build Status](https://travis-ci.org/agliznetsov/swagger-tools.svg?branch=master)](https://travis-ci.org/agliznetsov/swagger-tools)
[![Codecov](https://img.shields.io/codecov/c/github/agliznetsov/swagger-tools.svg)](http://codecov.io/github/agliznetsov/swagger-tools)
[![Download CLI](https://img.shields.io/maven-central/v/com.github.agliznetsov.swagger-tools/swagger-tools-cli.svg)](https://repo1.maven.org/maven2/com/github/agliznetsov/swagger-tools/swagger-tools-cli)



## Overview

This project is mainly oriented to java code generation. Currently it only supports Spring MVC dialect.

### Source

- Swagger 2.0 or OpenAPI 3.0 API definition in json/yaml format 

### Targets

- Model classes, jackson annotated
- Java client SDK, based on the Spring RestTemplate
- Server API bindings, MVC annotated. **Not working yet!** because of the Spring issue (https://jira.spring.io/browse/SPR-11055)

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
            <version>0.1.1</version>
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
   swagger 'com.github.agliznetsov.swagger-tools:swagger-tools-cli:0.1.1'
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