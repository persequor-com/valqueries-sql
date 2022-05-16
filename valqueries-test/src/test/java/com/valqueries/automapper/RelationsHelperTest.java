package com.valqueries.automapper;

import io.ran.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RelationsHelperTest {
    RelationsHelper relationsHelper;
    @Mock
    private ValqueriesRepositoryFactory factory;
    @Mock
    private GenericFactory genericFactory;

    @Before
    public void setup() {
        when(factory.getGenericFactory()).thenReturn(genericFactory);
        when(genericFactory.get(any())).thenAnswer((i) -> AutoMapper.get(i.getArgument(0, Class.class)).newInstance());
        when(factory.getMappingHelper()).thenReturn(new MappingHelper(genericFactory));
        relationsHelper = new RelationsHelper(factory);

    }
    @Test
    public void hasOppositeRelationFields_getManyToManyRelations_forGraph() {
        TypeDescriber<GraphNode> graphNodeDescriber = TypeDescriberImpl.getTypeDescriber(GraphNode.class);

        GraphNode node1 = new GraphNode();
        node1.setId("1");
        GraphNode node2 = new GraphNode();
        node2.setId("2");
        GraphNode node3 = new GraphNode();
        node3.setId("3");
        node1.setNextNodes(Arrays.asList(node2));
        node2.setPreviousNodes(Arrays.asList(node1));
        node2.setNextNodes(Arrays.asList(node3));
        node3.setPreviousNodes(Arrays.asList(node2));
        RelationDescriber previous = graphNodeDescriber.relations().get("previous_nodes");
        RelationDescriber next = graphNodeDescriber.relations().get("next_nodes");
        Collection<Object> previousLinks = relationsHelper.getManyToManyRelations(previous, node2, node2.getPreviousNodes(), GraphNodeLink.class);
        Collection<Object> nextLinks = relationsHelper.getManyToManyRelations(next, node2, node2.getNextNodes(), GraphNodeLink.class);


        assertEquals(node2.getId(), ((GraphNodeLink)nextLinks.stream().findFirst().get()).getFromId());
        assertEquals(node3.getId(), ((GraphNodeLink)nextLinks.stream().findFirst().get()).getToId());
        assertEquals(node1.getId(), ((GraphNodeLink)previousLinks.stream().findFirst().get()).getFromId());
        assertEquals(node2.getId(), ((GraphNodeLink)previousLinks.stream().findFirst().get()).getToId());
    }

    @Test
    public void hasOppositeRelationFields_getManyToManyRelations_complexStructure() {
        TypeDescriber<Marriage> marriageTypeDescriber = TypeDescriberImpl.getTypeDescriber(Marriage.class);

        Person me = new Person();
        me.setId("me");
        Person myWife = new Person();
        myWife.setId("wife");
        Marriage myMarriage = new Marriage();
        myMarriage.setPersons(Arrays.asList(me,  myWife));
        me.setMarriages(Arrays.asList(myMarriage));
        Person myChild = new Person();
        myMarriage.setChildren(Arrays.asList(myChild));

        RelationDescriber persons = marriageTypeDescriber.relations().get("persons");
        RelationDescriber children = marriageTypeDescriber.relations().get("children");
        Collection<Object> personsCollection = relationsHelper.getManyToManyRelations(persons, myMarriage, myMarriage.getPersons(), PersonMarriage.class);
        Collection<Object> childrenCollection = relationsHelper.getManyToManyRelations(children, myMarriage, myMarriage.getChildren(), ChildMarriage.class);


        assertEquals(myMarriage.getId(), ((PersonMarriage)personsCollection.stream().findFirst().get()).getMarriageId());
        assertEquals(me.getId(), ((PersonMarriage)personsCollection.stream().findFirst().get()).getPersonId());
        assertEquals(myMarriage.getId(), ((PersonMarriage)personsCollection.stream().skip(1).findFirst().get()).getMarriageId());
        assertEquals(myWife.getId(), ((PersonMarriage)personsCollection.stream().skip(1).findFirst().get()).getPersonId());
        assertEquals(myChild.getId(), ((ChildMarriage)childrenCollection.stream().findFirst().get()).getPersonId());
        assertEquals(myMarriage.getId(), ((ChildMarriage)childrenCollection.stream().findFirst().get()).getMarriageId());
    }
}