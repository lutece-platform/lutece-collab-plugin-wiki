![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=collab-plugin-wiki-deploy)
# Plugin wiki

## Introduction

 **Full featured WIKI :** 

## Markdown Library
This version of the wiki collab use Tui-Editor for the markdown rendering. You can find the documentation here : https://ui.toast.com/tui-editor/
To get the cdn files, you can clone the project and build it or you can download the cdn files from the project.
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
