package com.alloy.dts.blade.mongo;

import com.alloy.dts.blade.DataBlade;
import com.alloy.dts.blade.DataBladesPool;
import com.alloy.dts.camel.context.ProducerContext;
import com.alloy.dts.component.DTSComponent;
import com.alloy.dts.project.ConnectorProjectEntry;
import com.alloy.dts.project.Project;

import java.io.InputStreamReader;
import java.util.Objects;

public class MongoDataBladesPoolTest {
    private DataBlade db;

    public void before() throws Exception {
        ConnectorProjectEntry connectorProjectEntry = Project.json.fromJson(new InputStreamReader(
                        Objects.requireNonNull(
                                MongoDataBladeTest.class.getResourceAsStream("../../../../../testmongo.json"))),
                ConnectorProjectEntry.class);
        MongoDataBladesPool pool = new MongoDataBladesPool(2);
        initProducerContext("MONGO",pool);
        pool.setupConnectorTypes(connectorProjectEntry);
        pool.createDataBlades();
        db = pool.getDataBlade(1);
    }
    public static void initProducerContext(String componentCategory, DataBladesPool pool) {
        DTSComponent comp = new DTSComponent();
        comp.setCategory(componentCategory);
        ProducerContext ctx = new ProducerContext(comp);
        ctx.ensureDefaultRedisEndpoint();
        ctx.setDataBladesPool(pool);
    }
}
