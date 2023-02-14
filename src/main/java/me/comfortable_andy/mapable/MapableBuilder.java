package me.comfortable_andy.mapable;

public class MapableBuilder {

    private boolean needAnnotation = false;
    private boolean mapSerializable = true;
    private boolean log = false;

    public MapableBuilder setNeedAnnotation(boolean needAnnotation) {
        this.needAnnotation = needAnnotation;
        return this;
    }

    public MapableBuilder setMapSerializable(boolean mapSerializable) {
        this.mapSerializable = mapSerializable;
        return this;
    }

    public MapableBuilder setLog(boolean log) {
        this.log = log;
        return this;
    }

    public Mapable createMapable() {
        return new Mapable(needAnnotation, mapSerializable, log);
    }
}
