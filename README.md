# Mapable - Simple serializing with Reflection

### Quick example
```java
class Foe {
  @Mapable.MapMe // required for field to be serialized
  private MyData data;
  ...
  
  public Foe() { // required for creating object on deserialization
  }
  
  public Foe(...) {
    ...
  }
  
  public String toString() {
    ...
  }
}

class SomeOtherClass {
  private File saveFile = ...

  public void save(Foe foe) throws ReflectiveOperationException {
    Map<String, Object> serialized = Mapable.asMap(foe); 
    // expected output: "{===some_package.Foe, data={===some_package.MyData...}}"
    ...
  }
  
  public Foe read() throws ReflectiveOperationException {
    Map<String, Object> serialized = ...
    return Mapable.fromMap(serialized);
    // expected output: instance of Foe
  }
}
```

### How it works
```python
  objectToMap -> asMap():
    map <- new Map
    
    check if objectToMap is array
    
    put (==, objectToMap.class) to map
  
    for (non-static, non-transient fields) as field
      mapMe <- @MapMe from field
      name <- mapMe.mapName else field.name
      value <- field is not serializable then (field.value -> asMap()) else field.value
      put (name, value) in map
    return map
```
