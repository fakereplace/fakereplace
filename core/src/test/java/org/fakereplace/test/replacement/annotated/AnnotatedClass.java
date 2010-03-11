package org.fakereplace.test.replacement.annotated;

@Annotation1(svalue = "test")
public class AnnotatedClass
{

   @Annotation1(svalue = "hello")
   public String field;

}
