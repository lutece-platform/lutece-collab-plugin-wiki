![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=collab-plugin-wiki-deploy)
# Plugin wiki

## Introduction

 **Full featured WIKI :** 

## Markdown Library

## To use that plugins

Make sure you have those two plugin in your pom.xml

```xml
        <dependency>
            <groupId>fr.paris.lutece.plugins</groupId>
            <artifactId>module-mylutece-database</artifactId>
            <version>[6.0.0,)</version>
            <type>lutece-plugin</type>
        </dependency>
```

Activate the plugins and create an user with mylutece database and gave him the role admin for the wiki plugin

## Upgrade from v 3.01 or earlier to 3.02
Run the mysql upgrade file
The 3.0.2 version offer to edit all versions of the same topic and you can choose witch one you want to publish.
