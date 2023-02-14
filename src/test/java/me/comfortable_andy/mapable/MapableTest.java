package me.comfortable_andy.mapable;

import lombok.*;
import me.comfortable_andy.mapable.resolvers.ResolverRegistry;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.comfortable_andy.mapable.MapableConstants.CLAZZ_KEY;
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

    @RequiredArgsConstructor
    @ToString
    public static class ClassWithSerializable {
        @Getter
        private final SerializableClass thing;

        @Override // Lombok decided to not work
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassWithSerializable that = (ClassWithSerializable) o;
            return thing.equals(that.thing);
        }

        @Override
        public int hashCode() {
            return Objects.hash(thing.toString());
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class ClassWithList {
        private final List<ClassWithoutAnnotation> list;
    }

    public enum RandomEnum {
        SOMETHING,
        SOME_OTHER_THING,
        WHAT_IS_THIS_MADNESS,
    }

    @EqualsAndHashCode
    @RequiredArgsConstructor
    @ToString
    public static class SerializableClass implements Serializable {
        private final int integer;
    }
    //endregion

    private final Mapable annotationRequired = new MapableBuilder().setLog(true).setNeedAnnotation(true).createMapable(), annotationNotRequired = new MapableBuilder().setLog(true).setNeedAnnotation(false).createMapable(), mapSerializable = new MapableBuilder().setLog(true).setMapSerializable(true).createMapable(), dontMapSerializable = new MapableBuilder().setLog(true).setMapSerializable(false).createMapable();

    @Test
    public void asMap_ClassWithoutAnnotation_AnnotationRequired_EmptyMap() throws ReflectiveOperationException {
        final var obj = new ClassWithoutAnnotation(120, 4.44, "hello");
        final Map<String, Object> map = annotationRequired.asMap(obj);
        System.out.println(map);
        assertEquals(1, map.size());
        assertEquals(obj.getClass().getName(), map.get(CLAZZ_KEY));
    }

    @Test
    public void asMap_ClassWithAnnotation_AnnotationRequired_FilledMap() throws ReflectiveOperationException {
        final var obj = new ClassWithAnnotation(69235232432432L);
        System.out.println(ResolverRegistry.getInstance());
        final Map<String, Object> map = annotationRequired.asMap(obj);
        System.out.println(map);
        assertEquals(1 + obj.getClass().getDeclaredFields().length, map.size());
        assertEquals(obj.getClass().getName(), map.get(CLAZZ_KEY));
    }

    @Test
    public void asMap_ClassWithoutAnnotation_AnnotationNotRequired_FilledMap() throws ReflectiveOperationException {
        final var obj = new ClassWithoutAnnotation(12, 32.32432, "23923");
        final Map<String, Object> map = annotationNotRequired.asMap(obj);

        System.out.println(map);
        assertEquals(1 + obj.getClass().getDeclaredFields().length, map.size());
        assertEquals(obj.getClass().getName(), map.get(CLAZZ_KEY));
    }

    @Test
    public void mapRoundHouse_ClassWithEnum_WithAndWithoutEnumName_UnmappedShouldBeSame() throws ReflectiveOperationException {
        final var obj = new ClassWithEnumField(RandomEnum.SOME_OTHER_THING);
        final Map<String, Object> nameMap = annotationNotRequired.asMap(obj);
        assertEquals(obj, annotationNotRequired.fromMap(nameMap), "Should have the same content");
    }

    @Test
    public void mapRoundHouse_ClassWithSerializable_MapSerializable_ShouldBeMapped() throws ReflectiveOperationException {
        final var obj = new ClassWithSerializable(new SerializableClass(59));
        final var map = mapSerializable.asMap(obj);

        System.out.println(map);

        assertTrue(map.get("thing") instanceof Map);

        final ClassWithSerializable doMap = mapSerializable.fromMap(map, ClassWithSerializable.class);

        System.out.println(doMap);

        assertEquals(obj, doMap);
    }

    @Test
    public void mapRoundHouse_ClassWithSerializable_DontMapSerializable_ShouldNotBeMapped() throws ReflectiveOperationException {
        final var obj = new ClassWithSerializable(new SerializableClass(59));
        final var dontMap = dontMapSerializable.asMap(obj);

        System.out.println(dontMap);

        assertTrue(dontMap.get("thing") instanceof SerializableClass);

        final ClassWithSerializable dont = dontMapSerializable.fromMap(dontMap, ClassWithSerializable.class);

        System.out.println(dont);

        assertEquals(obj, dont);
    }

    @Test
    public void mapRoundHouse_ArrayAndListClassWithoutAnnotation_WithoutAnnotation_Fail() throws ReflectiveOperationException {
        final ClassWithoutAnnotation[] array = new ClassWithoutAnnotation[] {
                new ClassWithoutAnnotation(1, 2.2, "3"),
                new ClassWithoutAnnotation(2, 3.2, "2"),
                new ClassWithoutAnnotation(3, 4.2, "1"),
                new ClassWithoutAnnotation(4, 5.2, "23"),
        };
        final ClassWithList withList = new ClassWithList(Arrays.asList(array));
        final Map<String, Object> arrMap = annotationNotRequired.asMap(array);

        assertThrows(IllegalArgumentException.class, () -> annotationNotRequired.fromMap(arrMap, ClassWithoutAnnotation[].class));

        final Map<String, Object> listMap = annotationNotRequired.asMap(withList);
        System.out.println(listMap);
        assertEquals(withList.list, annotationNotRequired.fromMap(listMap, ClassWithList.class).list);
    }

}