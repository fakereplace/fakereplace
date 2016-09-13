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

It also provides Wildfly integration, and a maven plugin to allow maven to automatically replace classes after it has
compiled them.

Getting Fakereplace
-------------------

Download the latest release from https://mvnrepository.com/artifact/org.fakereplace/fakereplace-dist

To build it yourself simply run

`
mvn package --settings=.settings.xml
`

And then use the shaded jar that is build in the `dist\target` directory. Note that you only need to use the custom settings
file if you do not already have the JBoss nexus repositories in your personal settings file (as some tests are executed
against Wildfly).

Getting Started
---------------

There is a single jar in the distribution, fakereplace.jar to use it you need to set the JVM -javaagent option to point
to this jar. If you are not using Wildfly you also need to specify the packages that you want to be able to hot
replace.

For example, on Wildfly you would edit standalone.conf and add the following to JAVA_OPTS:

`
-javaagent:/path/to/fakereplace.jar
`

For other containers, you would need to add the following to the JVM options:

`
-javaagent:/path/to/fakereplace.jar=packages=com.mycompany.myclasses
`

Where ${com.mycompany.myclasses} is the top level package of the classes that you want to hot replace. All classes in
this package or sub packages are instrumented to allow them to be replaced. If you are using Wildfly this step is not
necessary, as the integration will just mark all user deployed classes as replaceable.

To set the JVM options you will probably need to modify your app servers startup script, or if you are using an IDE
server plugin set the VM arguments in the launch configuration.

Performing a hot deployment
---------------------------

There are currently 4 different ways to perform a hot deployment

### Use your IDE

Fakereplace will automatically enhance the normal IDE hot swapping capability. You should no longer get errors if you try and add/remove methods etc.

### Have Fakereplace watch your source files (currently Wildfly only)

When you start your container specify the following system property fakereplace.source-paths.[test.war]=/path/to/src/main/java (replacing [test.war] with the actual name of your deployment.
Fakereplace will then scan your source directory for changes, and attempt to automatically compile them. This is only triggered when a web request hits the container.

### Have Fakereplace watch your class files

If you are using an exploded type deployment where the class files are on the file system rather than in a jar then Wildfly will automatically watch them for changes and attempt to replace them if they are modified.

### Using the maven plugin

Fakereplace also provides a maven plugin that communicates with Fakereplace over a socket.

Using the maven plugin
----------------------

You will need to tell Fakereplace to start the socket server to be notified of changes. To do this add
the 'server' option to the fakereplace option string (e.g. ``-javaagent:/path/to/fakereplace.jar=server`).

You should see the following on startup:

`
Fakereplace listening on port 6555
`

To actually a hot deployment via the maven plugin add the following to your projects
pom.xml:


      <build>
        <plugins>
          <plugin>
            <groupId>org.fakereplace</groupId>
            <artifactId>fakereplace-maven-plugin</artifactId>
            <version>1.0.0.Alpha3</version>
            <executions>
              <execution>
                <phase>package</phase>
                <goals>
                  <goal>fakereplace</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>

And then run

`
mvn package
`

to perform the hot replacement.


Supported Options
-----------------
In addition to the *packages* option mentioned above, the following options are also supported. They should be specified
after the *-javaagent* command and comma seperated, e.g.

`
-javaagent:/path/to/fakereplace.jar=packages=com.mycompany.myclasses,log=trace
`

* **packages** Hot replacable packages
* **log** Supported options are trace,debug,info,error
* **index-file** The path to the fakereplace index file. Fakereplace stores this file after the first run to speed up later boots
* **dump-dir** Dumps classes to this dir on hot replacement, only useful for developers working on Fakereplace
* **remote** If this is present Fakereplace will start its server, it can also be used to specify the port number e.g. remote=6222

Other
-----

If you run into a bug report it at:

https://github.com/fakereplace/fakereplace/issues


For details on how if works see

https://github.com/fakereplace/fakereplace/wiki/How-It-Works


