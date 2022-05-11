package com.valqueries.automapper;

import javax.inject.Inject;

public class GraphNodeRepository extends ValqueriesCrudRepositoryImpl<GraphNode, String> {
    @Inject
    public GraphNodeRepository(ValqueriesRepositoryFactory factory) {
        super(factory, GraphNode.class, String.class);
    }
}
