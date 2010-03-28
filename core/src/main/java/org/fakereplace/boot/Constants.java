package org.fakereplace.boot;

public class Constants
{
   public final static String GENERATED_CLASS_PACKAGE = "org.fakereplace.proxies";

   public static final String ADDED_METHOD_NAME = "______REDEFINED_METHOD_DELEGATOR_";

   public static final String ADDED_METHOD_DESCRIPTOR = "(I[Ljava/lang/Object;)Ljava/lang/Object;";

   public static final String ADDED_FIELD_NAME = "______REDEFINED_FIELD_ARRAY_";

   public static final String ADDED_FIELD_DESCRIPTOR = "[Ljava/lang/Object;";

   public static final String ADDED_STATIC_METHOD_NAME = "______REDEFINED_STATIC_METHOD_DELEGATOR_";

   public static final String ADDED_STATIC_METHOD_DESCRIPTOR = "(I[Ljava/lang/Object;)Ljava/lang/Object;";

   public static final String ADDED_CONSTRUCTOR_DESCRIPTOR = "(I[Ljava/lang/Object;Lorg/fakereplace/util/ConstructorArgument;)V";

   public static final String ADDED_METHOD_CALLING_METHOD = "________METHOD_CALL__";

   public static final String ADDED_METHOD_CALLING_METHOD_DESCRIPTOR = "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";

   public static final String REPLACABLE_PACKAGES_KEY = "org.fakereplace.packages";
}
