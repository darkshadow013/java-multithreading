class CustomSingleton {

    private static CustomSingleton instance;
    private String name;
    private CustomSingleton(String name) {
        this.name = name;
    }

    public static CustomSingleton getInstance() {
        if (instance == null) {
            synchronized(CustomSingleton.class) {
                instance = new CustomSingleton("Testing");
            }
        }
        return instance;
    }

    public String getName() {
        return instance.name;
    }
}

public class TestSingleton {
    public static void main(String[] args) {
        CustomSingleton singleton = CustomSingleton.getInstance();
        System.out.println(singleton.getName());
        System.out.println(CustomSingleton.getInstance().hashCode() == singleton.hashCode());
    }
}