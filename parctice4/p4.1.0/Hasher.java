import java.util.Scanner;

public class Hasher {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(CryptoHash.hashString(scanner.nextLine()));
    }
}
