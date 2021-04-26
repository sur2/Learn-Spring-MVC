package hello.servlet.basic;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HelloData {
    private String username;
    private int age;

    @Override
    public String toString() {
        return "HelloData{" +
                "username='" + username + '\'' +
                ", age=" + age +
                '}';
    }
}
