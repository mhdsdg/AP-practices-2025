import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        PasswordBreaker breaker = new PasswordBreaker();
        breaker.loadPasswords(new File("passwords.txt"));
        Scanner scanner = new Scanner(System.in);
        breaker.setTargetHash(scanner.nextLine());
        breaker.startCracking(Integer.parseInt(scanner.nextLine()));
        System.out.println(breaker.getSpecialHashCount());
        String password = breaker.getFoundPassword();
        if (password != null) {
            System.out.println(password);
        } else {
            System.out.println("NOT FOUND");
        }
    }
}