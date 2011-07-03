Fakereplace Class Reloader

=================================

This project is still in the early stages of development, so there may be bugs.

Currently this project only works with Jboss Seam 2.x. Support for other
frameworks will be added once the core has stabilised. 


Getting Started

===========================

There is a single jar in the distribution, fakereplace.jar to use it 
you need to set the JVM -javaagent option. You also need to set another 
JVM option:

 -Dorg.fakereplace.packages=${com.mycompany.myclasses}

Where ${com.mycompany.myclasses} is the top level package of the classes that
you want to hot replace. All classes in this package or sub packages are
instrumented to allow them to be replaced. If you are using JBoss AS7 this
step is not nessesary.

To set the JVM options you will probably need to modify your app servers 
startup script, or if you are using the eclipse server plugin set the 
VM arguments in the launch configuration. The JVM parameter is:

-javaagent:/path/to/fakereplace.jar -Dorg.fakereplace.packages=${com.mycompany.myclasses}

To actually perform a hot deployment add the following to your projects
pom.xml:

  <build>
    <plugins>
      <plugin>
        <groupId>org.fakereplace</groupId>
        <artifactId>fakereplace-plugin</artifactId>
        <version>${fakereplace.version}</version>
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

mvn package

to perform the hot replacement.

If you run into a bug report it at:

https://github.com/fakereplace/fakereplace/issues


For details on how if works see

https://github.com/fakereplace/fakereplace/wiki/How-It-Works


