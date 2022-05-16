package com.valqueries.automapper;

import io.ran.*;
import io.ran.token.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RelationsHelper {
    private ValqueriesRepositoryFactory factory;

    public RelationsHelper(ValqueriesRepositoryFactory factory) {
        this.factory = factory;
    }

    public Collection<Object> getManyToManyRelations(RelationDescriber relationDescriber, Object t, Object relation, Class<?> via) {
        Collection<Object> manyToManyRelations = new ArrayList<>();

        if (!via.equals(None.class)) {
            for (Object rel : ((Collection<?>) relation)) {
                Mapping manyToManyRelation = (Mapping) factory.getGenericFactory().get(via);
                UncheckedObjectMap map = new UncheckedObjectMap();
                boolean bothSidesOfRelationIsSameClass = relationDescriber.getFromClass().equals(relationDescriber.getToClass());
                RelationDescriber fromOnSource = relationDescriber;
                RelationDescriber fromOnVia = relationDescriber.getVia().get(0);
                RelationDescriber toOnVia = relationDescriber.getVia().get(1);

                Optional<RelationDescriber> toOnTarget = TypeDescriberImpl.getTypeDescriber(relationDescriber.getToClass().clazz).relations().stream().filter(f -> {
                    boolean sameTargetClass =  f.getFromClass().equals(toOnVia.getToClass());
                    boolean sameViaClass = f.getRelationAnnotation().via().equals(relationDescriber.getRelationAnnotation().via());
                    boolean sameKeys1 = f.getToKeys().toProperties().equals(toOnVia.getFromKeys().toProperties());
                    boolean sameKeys2 = f.getFromKeys().toProperties().equals(toOnVia.getToKeys().toProperties());

                    return sameTargetClass && sameViaClass && (
                            !bothSidesOfRelationIsSameClass
                                    ||
                                    (sameKeys1 && sameKeys2)
                    );
                }).findFirst();


                for (int i = 0; i < fromOnVia.getFromKeys().size(); i++) {
                    Token sourceToken = getKeys(fromOnSource.getFromKeys(), fromOnVia.getFromKeys(), i);
                    Token viaToken = getKeys(fromOnSource.getToKeys(), fromOnVia.getToKeys(), i);

                    map.set(viaToken, factory.getMappingHelper().getKey(t).getValues().get(sourceToken).getValue());
                }

                for (int i = 0; i < toOnVia.getFromKeys().size(); i++) {
                    Token viaToken = getKeys(toOnTarget.map(RelationDescriber::getToKeys).orElse(null), toOnVia.getFromKeys(), i);
                    Token targetToken = getKeys(toOnTarget.map(RelationDescriber::getFromKeys).orElse(null), toOnVia.getToKeys(), i);

                    map.set(viaToken, factory.getMappingHelper().getKey(rel).getValues().get(targetToken).getValue());
                }

                manyToManyRelation.hydrate(map);
                manyToManyRelations.add(manyToManyRelation);
            }
        }
        return manyToManyRelations;
    }


    public Token getKeys(KeySet realObjectKeys, KeySet viaKeys, int i) {
        if (realObjectKeys == null || realObjectKeys.isEmpty()) {
            return viaKeys.get(i).getToken();
        } else {
            if (!realObjectKeys.get(i).getToken().equals(viaKeys.get(i).getToken())) {
                throw new RuntimeException("fields and relationFields values on relation does not match: "+realObjectKeys.get(i).getToken().camelHump()+" vs "+viaKeys.get(i).getToken());
            }
            return viaKeys.get(i).getToken();
        }
    }
}
