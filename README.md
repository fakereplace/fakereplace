Fakereplace Class Reloader
==========================

[![Build Status](https://travis-ci.org/fakereplace/fakereplace.svg?branch=master)](https://travis-ci.org/fakereplace/fakereplace)

This project provides a javaagent and a client to hot replace classes in the JVM over and above what is provided by
standard JDK hotswap.

It does this using bytecode trickery to instrument both the classes to be replaced and the reflection API, which allows
it to fake replacements that hotswap would not normally allow (For instance, if you remove a method the agent simply
adds back a noop method and then hides it from the reflection API).


Provided Integrations
---------------------

Being able to hot replace classes is one thing, but it is not very useful unless the framework you are using can also
pickup on these changes and re-load its metadata. To this end Fakereplace integrates with the following frameworks:

* **Weld** (Basic integration, it is still a work in progress)
* **JSF**
* **Metawidget**
* **Hibernate** (This restarts the EMF if an entity is modified, this is still experimental)
* **Resteasy**

It also provides Wildfly integration.

Getting Fakereplace
-------------------

Download the latest release from https://mvnrepository.com/artifact/org.fakereplace/fakereplace-dist

To build it yourself simply run

`
mvn package --settings=.settings.xml
`

And then use the shaded jar that is build in the `dist\target` directory. Note that you only need to use the custom settings
file if you do not already have the JBoss Nexus repositories in your personal settings file (as some tests are executed
against Wildfly).

Getting Started
---------------

There is a single jar in the distribution, `fakereplace.jar` to use it you need to set the JVM `-javaagent` option to point
to this jar. If you are not using Wildfly you also need to specify the packages that you want to be able to hot
replace.

For example, on Wildfly you would edit standalone.conf and add the following to `JAVA_OPTS`:

`
-javaagent:/path/to/fakereplace.jar
`

For other containers, you would need to add the following to the JVM options:

`
-javaagent:/path/to/fakereplace.jar=packages=com.mycompany.myclasses
`

Where `com.mycompany.myclasses` is the top level package of the classes that you want to hot replace. All classes in
this package or sub packages are instrumented to allow them to be replaced. If you are using Wildfly this step is not
necessary, as the integration will just mark all user deployed classes as replaceable.

To set the JVM options you will probably need to modify your app servers startup script, or if you are using an IDE
server plugin set the VM arguments in the launch configuration.

Start your application in DEBUG mode and attach a debugger to it.

Performing a hot deployment
---------------------------

There are currently 3 different ways to perform a hot deployment while debugging.

:information_source: After a successful hot deployment you should see outputs in the log like
`INFO Fakereplace is replacing class com/mycompany/myclasses/MyService`

### A) Use your IDE

Fakereplace will automatically enhance the normal IDE hot swapping capability. You should no longer get errors if you try and add/remove methods etc.

### B) Have Fakereplace watch your source files (currently Wildfly only)

When you start your container specify the following system property `fakereplace.source-paths.[test.war]=/path/to/src/main/java` (replacing `[test.war]` with the actual name of your deployment.
Fakereplace will then scan your source directory for changes, and attempt to automatically compile them. This is only triggered when a web request hits the container.

### C) Have Fakereplace watch your class files

If you are using an exploded type deployment where the class files are on the file system rather than in a jar then Wildfly will automatically watch them for changes and attempt to replace them if they are modified.

Supported Options
-----------------
In addition to the *packages* option mentioned above, the following options are also supported. They should be specified
after the *-javaagent* command and comma separated, e.g.

`
-javaagent:/path/to/fakereplace.jar=packages=com.mycompany.myclasses,log=trace
`

* **packages** Hot replaceable packages (multiple packages separated by `;`) 
* **log** Supported options are `trace,debug,info,error`
* **index-file** The path to the fakereplace index file. Fakereplace stores this file after the first run to speed up later boots
* **dump-dir** Dumps classes to this dir on hot replacement, only useful for developers working on Fakereplace
* **remote** If this is present Fakereplace will start its server, it can also be used to specify the port number e.g. `remote=6222`
* **no-index=true** Will tell Fakereplace not to use an index file to speed up subsequent boots.

Other
-----

If you run into a bug report it at:

https://github.com/fakereplace/fakereplace/issues


For details on how if works see

https://github.com/fakereplace/fakereplace/wiki/How-It-Works


