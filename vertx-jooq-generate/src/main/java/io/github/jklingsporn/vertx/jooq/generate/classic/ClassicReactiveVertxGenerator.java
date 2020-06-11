package io.github.jklingsporn.vertx.jooq.generate.classic;

import java.time.LocalDateTime;

import org.jooq.codegen.JavaWriter;
import org.jooq.meta.TypedElementDefinition;

import io.github.jklingsporn.vertx.jooq.generate.builder.DelegatingVertxGenerator;
import io.github.jklingsporn.vertx.jooq.generate.builder.VertxGeneratorBuilder;

/**
 * Created by jensklingsporn on 06.02.18.
 */
public class ClassicReactiveVertxGenerator extends DelegatingVertxGenerator {

    public ClassicReactiveVertxGenerator() {
        super(VertxGeneratorBuilder.init().withClassicAPI().withPostgresReactiveDriver().build());
    }
    
    // added by nixos89 on 11.06.2020.
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
    
    
}
