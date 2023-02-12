package me.comfortable_andy.mapable;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MapableTest {


    //region Classes
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class ClassWithoutAnnotation {
        private final int someInteger;
        private final double someDouble;
        private final String str;
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class ClassWithAnnotation {
        @Mapable.MapMe
        private final long longBoi;
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class ClassWithEnumField {
        private final RandomEnum enumThingy;
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class ClassWithSerializable {
        private final SerializableClass thing;
    }

    public static enum RandomEnum {
        SOMETHING,
        SOME_OTHER_THING,
        WHAT_IS_THIS_MADNESS,
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class SerializableClass implements Serializable {
        private final int integer;
    }
    //endregion

    private final Mapable annotationRequired = new Mapable.MapableBuilder().setLog(true).setNeedAnnotation(true).createMapable(), annotationNotRequired = new Mapable.MapableBuilder().setLog(true).setNeedAnnotation(false).createMapable(), enumName = new Mapable.MapableBuilder().setLog(true).setUseEnumName(true).createMapable(), ordinal = new Mapable.MapableBuilder().setLog(true).setUseEnumName(false).createMapable(), mapSerializable = new Mapable.MapableBuilder().setLog(true).setMapSerializable(true).createMapable(), dontMapSerializable = new Mapable.MapableBuilder().setLog(true).setMapSerializable(false).createMapable();

    @Test
    public void asMap_ClassWithoutAnnotation_AnnotationRequired_EmptyMap() throws ReflectiveOperationException {
        final var obj = new ClassWithoutAnnotation(120, 4.44, "hello");
        final Map<String, Object> map = annotationRequired.asMap(obj);
        System.out.println(map);
        assertEquals(1, map.size());
        assertEquals(obj.getClass().getName(), map.get(Mapable.CLAZZ_KEY));
    }

    @Test
    public void asMap_ClassWithAnnotation_AnnotationRequired_FilledMap() throws ReflectiveOperationException {
        final var obj = new ClassWithAnnotation(69235232432432L);
        final Map<String, Object> map = annotationRequired.asMap(obj);
        System.out.println(map);
        assertEquals(1 + obj.getClass().getDeclaredFields().length, map.size());
        assertEquals(obj.getClass().getName(), map.get(Mapable.CLAZZ_KEY));
    }

    @Test
    public void asMap_ClassWithoutAnnotation_AnnotationNotRequired_FilledMap() throws ReflectiveOperationException {
        final var obj = new ClassWithoutAnnotation(12, 32.32432, "23923");
        final Map<String, Object> map = annotationNotRequired.asMap(obj);

        System.out.println(map);
        assertEquals(1 + obj.getClass().getDeclaredFields().length, map.size());
        assertEquals(obj.getClass().getName(), map.get(Mapable.CLAZZ_KEY));
    }

    @Test
    public void mapRoundHouse_ClassWithEnum_WithAndWithoutEnumName_UnmappedShouldBeSame() throws ReflectiveOperationException {
        final var obj = new ClassWithEnumField(RandomEnum.SOME_OTHER_THING);
        final Map<String, Object> nameMap = enumName.asMap(obj);
        final Map<String, Object> ordinalMap = ordinal.asMap(obj);
        System.out.println("Name: " + nameMap);
        System.out.println("Ordinal: " + ordinalMap);
        assertEquals(obj, enumName.fromMap(nameMap), "Should have the same content");
        assertEquals(obj, ordinal.fromMap(ordinalMap), "Should have the same content");
    }

    @Test
    public void mapRoundHouse_ClassWithSerializable_MapAndDontMapSerializable_ShouldBeUnmapped() throws ReflectiveOperationException {
        final var obj = new ClassWithSerializable(new SerializableClass(59));
        final var map = mapSerializable.asMap(obj);
        final var dontMap = dontMapSerializable.asMap(obj);

        System.out.println("Dont: " + dontMap);
        System.out.println("Do: " + map);

        assertEquals(obj, mapSerializable.fromMap(map));
        assertEquals(obj, dontMapSerializable.fromMap(map));
    }

}