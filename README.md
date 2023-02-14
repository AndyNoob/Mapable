# Mapable - Everything can be a MAP!
## How it worky
### `Mapable#asMap`
1. Receives input object `toMap`, and `@Nullable clazz` (the class of the object is inferred)
2. Collects all fields (including superclasses) via Reflection
   1. Checks if a `IResolver` is registered for this type
      1. **Yes**
         1. Calls [`IResolver#resolve`](https://github.com/AndyNoob/Mapable/blob/f8b511444cbfe808c69fefb759b1ceba3bf0ae19/src/main/java/me/comfortable_andy/mapable/resolvers/IResolver.java#L17)
      2. **No**
         1. Calls [`Mapable#asMap`](#mapableasmap);
3. Done!