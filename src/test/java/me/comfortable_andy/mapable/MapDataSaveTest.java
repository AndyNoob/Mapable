package me.comfortable_andy.mapable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Data save test with lombok
 *
 * @author AndyNoob
 */
public class MapDataSaveTest {

    public static void main(String[] args) {
        MyDataB b = new MyDataB(420, false, "wssup");
        MyDataA a = new MyDataA(69, 'b', "sup bro", b);

        try {
            Map<String, Object> map = Mapable.asMap(a);

            System.out.println("Printing out map with the serialized data:");
            System.out.println(map + "\n");

            MyDataA retrieved = (MyDataA) Mapable.fromMap(map);

            System.out.println("Printing out deserialized data:");
            System.out.println(retrieved);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Getter
    @Setter
    @ToString
    public static class MyDataA {
        @Mapable.MapMe // required
        private float a;
        @Mapable.MapMe
        private char b;
        @Mapable.MapMe
        private String name;
        @Mapable.MapMe
        private MyDataB myDataB;

        public MyDataA() {
        }

        public MyDataA(float a, char b, String name, MyDataB myDataB) {
            this.a = a;
            this.b = b;
            this.name = name;
            this.myDataB = myDataB;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class MyDataB {
        @Mapable.MapMe
        private int a;
        @Mapable.MapMe
        private boolean b;
        @Mapable.MapMe(mapName = "another-name") // this will change the field name in the map
        private String name;

        public MyDataB() {
        }

        public MyDataB(int a, boolean b, String name) {
            this.a = a;
            this.b = b;
            this.name = name;
        }
    }
}
