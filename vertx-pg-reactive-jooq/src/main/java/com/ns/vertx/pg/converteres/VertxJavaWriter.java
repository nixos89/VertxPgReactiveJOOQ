package com.ns.vertx.pg.converteres;

import java.io.File;

import org.jooq.codegen.JavaWriter;

/**
* Exposes ref-methods from JavaWriter
*/
public class VertxJavaWriter extends JavaWriter {


   public VertxJavaWriter(File file, String fullyQualifiedTypes, String encoding) {
       super(file, fullyQualifiedTypes, encoding);
   }


   @Override
   protected String beforeClose(String string) {
       return super.beforeClose(string);
   }


   @Override
   public String ref(String clazzOrId, int keepSegments) {
       return super.ref(clazzOrId, keepSegments);
   }

   @Override
   public String ref(String clazz) {
       return super.ref(clazz);
   }
}
