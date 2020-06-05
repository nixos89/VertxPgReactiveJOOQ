package com.ns.vertx.pg.converteres;

/* NOTE - added for conversion of Timestamp to LocalDateTime by this suggestion: 
 	https://github.com/jklingsporn/vertx-jooq/issues/134#issuecomment-593831035 */
//TODO: implement CustomVertxGenerator class
public class CustomVertxGenerator extends DelegatingVertxGenerator {
	
	// link at https://github.com/jklingsporn/vertx-jooq/blob/master/vertx-jooq-generate/src/test/java/io/github/jklingsporn/vertx/jooq/generate/custom/CustomVertxGenerator.java
	
	/*
	public CustomVertxGenerator() {
        super(VertxGeneratorBuilder.init().withClassicAPI().withJDBCDriver().build());
    }

    @Override
    protected boolean handleCustomTypeFromJson(TypedElementDefinition<?> column, String setter, String columnType, String javaMemberName, JavaWriter out) {
        if(isType(columnType, LocalDateTime.class)){
            out.tab(2).println("%s(json.getString(\"%s\")==null?null:LocalDateTime.parse(json.getString(\"%s\")));", setter, javaMemberName, javaMemberName);
            return true;
        }
        return super.handleCustomTypeFromJson(column, setter, columnType, javaMemberName, out);
    }

    @Override
    protected boolean handleCustomTypeToJson(TypedElementDefinition<?> column, String getter, String columnType, String javaMemberName, JavaWriter out) {
        if(isType(columnType, LocalDateTime.class)){
            out.tab(2).println("json.put(\"%s\",%s()==null?null:%s().toString());", getJsonKeyName(column),getter,getter);
            return true;
        }
        return super.handleCustomTypeToJson(column, getter, columnType, javaMemberName, out);
    }
    */
}
