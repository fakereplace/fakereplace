package org.fakereplace.test.weld;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ByteArrayAsset;
import org.testng.annotations.Test;

public class SimpleWeldTest extends Arquillian
{
   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create("test.jar", JavaArchive.class).addClasses(SimpleProducer.class, SimpleReciever.class).addManifestResource(new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"));
   }

   @Inject
   SimpleReciever reciever;

   @Test
   public void simpleTest()
   {
      assert reciever.value.equals("hello world");
   }

}
