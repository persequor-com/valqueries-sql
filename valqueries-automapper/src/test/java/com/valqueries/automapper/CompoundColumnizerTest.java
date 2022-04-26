package com.valqueries.automapper;

import com.valqueries.automapper.schema.ValqueriesColumnToken;
import io.ran.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CompoundColumnizerTest {
    private CompoundColumnizer<MyModel> columnizer;
    @Mock
    GenericFactory genericFactory;
    MappingHelper mappingHelper;
    Collection<MyModel> ts;
    SqlNameFormatter sqlNameFormatter = new SqlNameFormatter();
    @Mock
    SqlDialect dialect;
    TypeDescriber<MyModel> typeDescriber = TypeDescriberImpl.getTypeDescriber(MyModel.class);
    private ZonedDateTime now = ZonedDateTime.parse("2020-01-01T13:15:15Z");

    @Before
    public void setup() throws InstantiationException, IllegalAccessException {
        when(dialect.escapeColumnOrTable(any(String.class))).thenAnswer((a) -> {
            return a.getArgument(0, String.class);
        });
        when(genericFactory.get(MyModel.class)).thenReturn(AutoMapper.get(MyModel.class).newInstance());
        when(dialect.column(any(Property.class))).thenAnswer((a) -> {
            return new ValqueriesColumnToken(sqlNameFormatter, dialect, a.getArgument(0, Property.class));
        });
        mappingHelper = new MappingHelper(genericFactory);
        ts = new ArrayList<>();
        now = ZonedDateTime.now();
        ts.add(new MyModel("1","one", now.minus(Duration.ofMinutes(1))));
        ts.add(new MyModel("2","two", now.minus(Duration.ofMinutes(2))));
        columnizer = new CompoundColumnizer<MyModel>(
            genericFactory,
            mappingHelper,
            ts,
            sqlNameFormatter,
            dialect,
            typeDescriber
        );


    }

    @Test
    public void happyPath_getColumns() {
        Set<String> columns = columnizer.getColumns();
        assertEquals(3, columns.size());
        assertEquals("id", columns.stream().skip(0).findFirst().get());
        assertEquals("title", columns.stream().skip(1).findFirst().get());
        assertEquals("updated_at", columns.stream().skip(2).findFirst().get());
    }

    @Test
    public void happyPath_getColumnsWithoutKey() {
        Set<String> columnsWithoutKey = columnizer.getColumnsWithoutKey();
        assertEquals(2,  columnsWithoutKey.size());
        assertEquals("title", columnsWithoutKey.stream().skip(0).findFirst().get());
        assertEquals("updated_at", columnsWithoutKey.stream().skip(1).findFirst().get());
    }

    @Test
    public void happyPath_getValueTokens() {
        List<List<String>> valueTokens = columnizer.getValueTokens();
        assertEquals(2,  valueTokens.size());
        assertEquals(3,  valueTokens.get(0).size());
        assertEquals(3,  valueTokens.get(1).size());
        assertEquals("id_0", valueTokens.get(0).get(0));
        assertEquals("title_0", valueTokens.get(0).get(1));
        assertEquals("updated_at_0", valueTokens.get(0).get(2));
        assertEquals("id_1", valueTokens.get(1).get(0));
        assertEquals("title_1", valueTokens.get(1).get(1));
        assertEquals("updated_at_1", valueTokens.get(1).get(2));
    }

    @Test
    public void happyPath_getFields() {
        Map<String, String> fields = columnizer.getFields();
        assertEquals("id", fields.get("id"));
        assertEquals("title", fields.get("title"));
        assertEquals("updated_at", fields.get("updated_at"));
    }

    @Test
    public void happyPath_getFieldsWithoutKeys() {
        Map<String, String> fields = columnizer.getFieldsWithoutKeys();
        assertEquals("title", fields.get("title"));
        assertEquals("updated_at", fields.get("updated_at"));
    }

    @Test
    public void happyPath_getKeys() {
        Set<String> keys = columnizer.getKeys();
        assertEquals(1, keys.size());
        assertEquals("id", keys.stream().skip(0).findFirst().get());
    }


}
