package config;

public class GetPath {
    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        System.out.println("Working Path = " + path);
        System.out.println("Base Path = " + path.replace("/IC/Noberto/PGCC003", ""));
    }
}
