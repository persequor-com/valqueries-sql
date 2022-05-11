package com.valqueries.automapper;

import io.ran.ObjectMap;
import io.ran.token.Token;

public class UncheckedObjectMap extends ObjectMap {
    public void set(Token key, Object value) {
        put(key, value);
    }
}