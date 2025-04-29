package controllers;
/*
Explanation:
- This is a controller class for the sign-up menu Controller.
- This class will be used to implement functions that do sign up menu operations.
- notice that this class should not have any input and output and just use it to implement functionalities.
 */

import models.App;
import models.Result;
import models.User;
import models.enums.Menu;
import java.util.regex.*;

public class SignUpMenuController {
    public Result register(String username, String password , String email , String name) {
        if (!isUsernameValid(username)) {
            return new Result(false, "username format is invalid!");
        }
        if (!isUsernameUnique(username)){
            return new Result(false, "this username is already taken!");
        }
        if (!isPasswordValid(password)) {
            return new Result(false, "password format is invalid!");
        }
        if (!isEmailValid(email)) {
            return new Result(false, "email format is invalid!");
        }
        if (!isNameValid(name)) {
            return new Result(false, "name format is invalid!");
        }
        App.users.add(new User(username, password, email, name));
        App.setCurrentMenu(Menu.LOGINMENU);
        return new Result(true, "user registered successfully.you are now in login menu!");
    }
    public String goToLogin(){
        App.setCurrentMenu(Menu.LOGINMENU);
        return "you are now in login menu!";}
    public void exit(){ App.setCurrentMenu(Menu.EXIT);}

    public boolean isUsernameValid(String username) {
        return username.length() <= 10 && username.length() >= 4 && username.matches("^[a-zA-Z][a-zA-Z-_.0-9]*$");

    }
    public boolean isUsernameUnique(String username) {
        return getByUsername(username) == null;
    }
    public boolean isPasswordValid(String password) {
        String[] regexes = {"[a-z]","[A-Z]","[0-9]","[!@#$%^&*]"};
        for (String regex : regexes) {
            Matcher matcher = Pattern.compile(regex).matcher(password);
            if (!matcher.find()) {
                return false;
            }
        }
        if(password.matches("^[a-zA-Z0-9!@#$%^&*]{6,12}$")) return true;
        return false;
    }

    public boolean isDomainValid(String domain) {
        // Check if domain starts and ends with a letter
        if (!Character.isLetter(domain.charAt(0)) || !Character.isLetter(domain.charAt(domain.length() - 1))) {
            return false;
        }

        long a = domain.chars().filter(ch -> ch == '-').count() + domain.chars().filter(ch -> ch == '.').count() ;
        return a <= 1;
    }

    public boolean isEmailValid(String email) {
        // Regex to capture user, domain, and TLD in groups
        String regex = "^(?<user>[a-zA-Z][a-zA-Z-_.0-9]{3,9})@(?<domain>[a-z.-]{3,7})\\.(?<tld>org|com|net|edu)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            return false; // Email does not match the overall pattern
        }

        String user = matcher.group("user");
        String domain = matcher.group("domain");

        if (!isUsernameValid(user)) {
            return false;
        }

        // Validate domain
        return isDomainValid(domain);
    }
    public boolean isNameValid(String name) {
        return name.matches("^[a-zA-Z][a-zA-Z-]*[a-zA-Z]$") || name.matches("^[a-zA-Z]$");
    }
    public User getByUsername(String username) {
        for (User user : App.users) {
            if (user.getUsername().equals(username)) { return user; }
        }
        return null;
    }

}
