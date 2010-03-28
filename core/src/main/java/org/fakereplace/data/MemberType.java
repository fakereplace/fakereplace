package org.fakereplace.data;

public enum MemberType
{
   /**
    * normal methods are methods from the java source code
    */
   NORMAL,
   /**
    * fake methods are methods that we are pretending exist
    */
   FAKE,

   FAKE_CONSTRUCTOR,
   /**
    * added delegates are methods that the transformer added at load time to
    * directly call a superclass fake method so subclasses can use the normal
    * virtual method resolution rules
    * 
    */
   ADDED_DELEGATE,
   /**
    * This is a method that we have to implement with a noop as it was removed
    * from the source
    */
   REMOVED_METHOD,
   /**
    * This is a method that has been added that should not be visible to the
    * user
    */
   ADDED_SYSTEM;
}
