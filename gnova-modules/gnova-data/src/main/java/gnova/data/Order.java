package gnova.data;

/**
 * 排列顺序
 */
public class Order {

    public static Order NewOrder(String name) {
        return new Order(name, Type.Ascend);
    }

    public static Order NewOrder(String name, Type type) {
        return new Order(name, type);
    }

    public enum Type {

        /**
         * 升序
         */
        Ascend,

        /**
         * 降序
         */
        Descend
    }

    private String name;
    private Type type;

    public Order(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


}
