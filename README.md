![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=collab-plugin-wiki-deploy)
# Plugin wiki

## Introduction

 **Full featured WIKI :** 


 
* Use standard markdown syntax
* Can be easily customized by macros or bootstrap to add new rendering features

 **Fully integrated to Lutece platform :** 


 
* Use MyLutece authentication and roles
* Compatible with Extend plugin and all its modules (comment, rating, hits, opengraph...)
* Support Lutece's avatar and pseudo features
* Use the graphical theme of the site. It will change the same way as all the site when the theme is modified.
* Code rendering skins can be managed into "Site's properties"



[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-wiki/)

 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*
 
 ** To use that plugins ** 

Make sure you have those two plugin in your pom.xml

```xml
  <dependency>
            <groupId>fr.paris.lutece.plugins</groupId>
            <artifactId>plugin-mylutece</artifactId>
            <version>[4.0.5,)</version>
            <type>lutece-plugin</type>
        </dependency>
        <dependency>
            <groupId>fr.paris.lutece.plugins</groupId>
            <artifactId>module-mylutece-database</artifactId>
            <version>[6.0.0,)</version>]
            <type>lutece-plugin</type>
        </dependency>
```

Activate the plugins and create an user with mylutece database and gave him the role admin for the wiki plugin

 ** Upgrade from v 3.01 or earlier to 3.02 ** 
Make sure you upgrade lutece core as well, since that plugin uses bootstrapt, contained by lutece core.
Run the mysql upgrade file
The 3.0.2 version offer to edit all versions of the same topic and you can choose witch one you want to publish.
In earlier versions, the last version would be published when saved.  
In the database, we have had in the topic_version a new column, is_published : 1 is for the published version of one topic if not it's 0.  
During the transition, for all the versions already here, is_published will be set to 2. 
Meaning that, when this topic page (from previous version of the wiki plugin) is gonna be called in the view page, if published is at two, then we will call the lastest version the the topic, and we will set it the one so it's published (just like in the previous of the wiki plugin).
The transition from the wikicreole to the markdown is gonna happen when you first go on the edit page. 
Is you alrealdy went on the view page, you will be able the create a new version from the publish page, 
if not, we recommand to save it in a new version before activating the auto-save mod. 

