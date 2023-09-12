package me.comfortable_andy.mapable;

import lombok.*;
import me.comfortable_andy.mapable.resolvers.ResolverRegistry;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;

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

    @EqualsAndHashCode(callSuper = true)
    @ToString
    public static class ClassWithParent extends SerializableClass {

        public ClassWithParent(int integer) {
            super(integer);
        }
    }

    public record SomeRecord(int a, int b, int c) {
    }

    public static class ClassWithPrimitiveArray {
        private final int[] array = new int[] { 1, 2, 3, 4, 5 };
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

        System.out.println();
        System.out.println(map);
        System.out.println();

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
        final ClassWithoutAnnotation[] array = new ClassWithoutAnnotation[]{
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

    @Test
    public void mapRoundHouse_ClassWithParent_Success() throws ReflectiveOperationException {
        final ClassWithParent withParent = new ClassWithParent(52395);
        final Map<String, Object> map = annotationNotRequired.asMap(withParent);
        assertEquals(withParent, annotationNotRequired.fromMap(map, ClassWithParent.class));
    }

    @Test
    public void fromMap_FedWithPrimitiveWrappers_Success() throws ReflectiveOperationException {
        final Map<String, Object> map = new HashMap<>() {{
            put("someInteger", 32523);
            put("someDouble", 23532.3225);
            put("str", "urhgurhuirehegriuhgerhiuegruhi");
        }};
        final ClassWithoutAnnotation object = annotationNotRequired.fromMap(map, ClassWithoutAnnotation.class);
        System.out.println(object);
        assertEquals(map.get("someDouble"), object.someDouble);
        assertEquals(map.get("someInteger"), object.someInteger);
        assertEquals(map.get("str"), object.str);
    }

    @Test
    public void mapRoundHouse_Record_Success() throws ReflectiveOperationException {
        final SomeRecord record = new SomeRecord(1, 3, 4);
        final Map<String, Object> map = annotationNotRequired.asMap(record, SomeRecord.class);
        System.out.println(map);
        assertEquals(record, annotationNotRequired.fromMap(map, SomeRecord.class));
    }

    @Test
    public void mapRoundHouse_PrimitiveArray_Success() throws ReflectiveOperationException {
        final ClassWithPrimitiveArray primitiveArray = new ClassWithPrimitiveArray();
        final Map<String, Object> map = annotationNotRequired.asMap(primitiveArray);
        System.out.println(map);
        assertArrayEquals(primitiveArray.array, annotationNotRequired.fromMap(map, ClassWithPrimitiveArray.class).array);
    }

}