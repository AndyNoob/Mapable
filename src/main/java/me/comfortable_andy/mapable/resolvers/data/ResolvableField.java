package me.comfortable_andy.mapable.resolvers.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import me.comfortable_andy.mapable.Mapable;

import java.lang.reflect.Field;

import static me.comfortable_andy.mapable.util.ClassUtil.WRAPPERS;

@Getter
@ToString
public class ResolvableField {

    private final FieldInfo info;
    private final Object value;
    private final Mapable instance;

    public ResolvableField(@NonNull final FieldInfo info, /* @Nullable */ final Object value, @NonNull final Mapable instance) {
        this.info = info;
        this.value = value;
        this.instance = instance;
    }

    public void applyToJavaField(@NonNull final Object owning, @NonNull final Field javaField) throws IllegalAccessException {
        if (value == null) return;
        if (!javaField.trySetAccessible()) throw new IllegalStateException("Could access " + javaField);

        if (value.getClass() != String.class && WRAPPERS.contains(value.getClass())) {
            switch (value.getClass().getSimpleName()) {
                case "Double":
                    javaField.setDouble(owning, (double) value);
                    break;
                case "Float":
                    javaField.setFloat(owning, (float) value);
                    break;
                case "Long":
                    javaField.setLong(owning, (long) value);
                    break;
                case "Integer":
                    javaField.setInt(owning, (int) value);
                    break;
                case "Short":
                    javaField.setShort(owning, (short) value);
                    break;
                case "Character":
                    javaField.setChar(owning, (char) value);
                    break;
                case "Byte":
                    javaField.setByte(owning, (byte) value);
                    break;
            }
        } else javaField.set(owning, value);
    }
}
