Fakereplace Class Reloader

=================================

THIS PROJECT IS STILL PRE ALPHA

There is no guarantee that it will work for you, and you should not run 
with the javaagent enabled in a production environment.  

Currently this project only works with Jboss Seam 2.x. Support for other
frameworks will be added once the core has stabilised. 


Getting Started

===========================

There are two jars in the fakereplace distribution, fakereplace.jar and
seam-agent.jar. fakereplace.jar is a java agent, to use it you need to set
the JVM -javaagent option. You also need to set another JVM option:

-Dorg.fakereplace.packages=com.mycompany.myclasses

Where com.mycompany.myclasses is the top level package of the classes that
I am trying to hot replace. All classes in this package or subpackages are
instrumented to allow them to be replaced.

To set the JVM options you will probably need to modify your app servers 
startup script, or if you are using the eclipse server plugin set the 
VM arguments in the lauch configuration. The JVM parameter is:

-javaagent:/path/to/fakereplace.jar


The other archive in the distribution, seam-agent.jar needs to be included
in your app. It contains a seam filter that functions in a similar way to
the seam hot deploy filter. If you are using a war you should put this in 
WEB-INF/lib  and for ear deployments you should put it in ear/lib (depending 
on your application server you may need to add it to application.xml as well).

  


