# JOS Package

#### Maven plugin to generate JOS module package.

**Usage :**

```xml

<plugin>
    <groupId>ir.moke.jpkg</groupId>
    <artifactId>maven-jpkg</artifactId>
    <version>1.1</version>
    <configuration>
        <name>application</name>
        <version>0.1</version>
        <description>Main application</description>
        <maintainer>Mahdi Sheikh Hosseini (mah454)</maintainer>
        <dependencies>
            <dependency>
                <name>shared-library</name>
                <version>0.1</version>
            </dependency>
            <dependency>
                <name>something-else</name>
                <version>0.1</version>
            </dependency>
        </dependencies>
    </configuration>
</plugin>
```

**Configuration Parameters :**

| Parameter    | Required | example               | Description                                                                                                 |
|--------------|----------|-----------------------|-------------------------------------------------------------------------------------------------------------|
| name         | true     | application           | Module name                                                                                                 |
| version      | true     | 0.1                   | Module version                                                                                              |
| description  | false    | this is sample module | Describe module                                                                                             |
| maintainer   | false    | Mahdi Sheikh Hosseini | Package generator                                                                                           |
| url          | false    | http://example.com    | Package generator address                                                                                   |
| dependencies | false    |                       | jpkg could be dependent to another jpkg.<br/>this parameter describe list jpkg dependencies of current jpkg | 