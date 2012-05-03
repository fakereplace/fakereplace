Fakereplace Class Reloader
==========================

This project provides a javaagent and a client to hot replace classes in the JVM over and above what is provided by
standard JDK hotswap.

It does this using bytecode trickery to instrument both the classes to be replaced and the reflection API, which allows
it to fake replacements that hotswap would not normally allow (For instance, if you remove a method the agent simply
adds back a noop method and then hides it from the reflection API).


Provided Integrations
---------------------

Being able to hot replace classes is one thing, but it is not very useful unless the framework you are using can also
pickup on these changes and re-load its metadata. To this end Fakereplace integrates with the following frameworks:

* **Seam 2**
* **Weld** (Basic integration, it is still a work in progress)
* **JSF**
* **Metawidget**

It also provides JBoss AS7 integration, and a maven plugin to allow maven to automatically replace classes after it has
compiled them.


Getting Started
---------------

There is a single jar in the distribution, fakereplace.jar to use it you need to set the JVM -javaagent option to point
to this jar. If you are not using JBoss AS7 you also need to specify the packages that you want to be able to hot
replace.

For example, on JBoss AS7 you would edit standalone.conf and add the following to JAVA_OPTS:

`
-javaagent:/path/to/fakereplace.jar
`

For other containers, you would need to add the following to the JVM options:

`
-javaagent:/path/to/fakereplace.jar=packages=com.mycompany.myclasses
`

Where ${com.mycompany.myclasses} is the top level package of the classes that you want to hot replace. All classes in
this package or sub packages are instrumented to allow them to be replaced. If you are using JBoss AS7 this step is not
nessesary, as the integration will just mark all user deployed classes as replacable.

To set the JVM options you will probably need to modify your app servers startup script, or if you are using an IDE
server plugin set the VM arguments in the launch configuration.

Performing a hot deployment
---------------------------

To actually perform a hot deployment add the following to your projects
pom.xml:

`
  <build>
    <plugins>
      <plugin>
        <groupId>org.fakereplace</groupId>
        <artifactId>fakereplace-plugin</artifactId>
        <version>1.0.0.Alpha1</version>
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
`

You will need the [JBoss Maven Repository](https://community.jboss.org/wiki/MavenGettingStarted-Users) in your pom.xml
or settings.xml.

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

Other
-----

If you run into a bug report it at:

https://github.com/fakereplace/fakereplace/issues


For details on how if works see

https://github.com/fakereplace/fakereplace/wiki/How-It-Works


