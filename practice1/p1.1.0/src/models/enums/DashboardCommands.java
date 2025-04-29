package models.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
Explanation:
- we have commands in our dashboard and this commands need regexes to be checked.
- put those regexes here and use them in your code.
- this regexes need some functions, put those functions in here.
 */
public enum DashboardCommands  {
    CreateGroup("\\s*create-group\\s+-n\\s+(?<name>.+?)\\s+-t\\s+(?<type>.+?)\\s*"),
    ShowOwnGroups("\\s*show\\s+my\\s+groups\\s*"),
    AddToGroup("\\s*add-user\\s+-u\\s+(?<username>.+?)\\s+-e\\s+(?<email>.+?)\\s+-g\\s+(?<groupid>.+?)\\s*"),
    AddExpense("\\s*add-expense\\s+-g\\s+(?<groupid>.+?)\\s+-s\\s+(?<type>(equally|unequally))\\s+-t\\s+(?<totalexpense>.+?)\\s+-n\\s+(?<numberofusers>.+?)\\s*"),
    EquallyUser("\\s*(?<username>.+?)\\s*"),
    UnequallyUser("\\s*(?<username>.+?)\\s+(?<amount>.+?)\\s*"),
    ShowBalance("\\s*show\\s+balance\\s+-u\\s+(?<username>.+?)\\s*"),
    SettleUp("\\s*settle-up\\s+-u\\s+(?<username>.+?)\\s+-m\\s+(?<inputmoney>.+?)\\s*"),
    GoToProfile("\\s*go\\s+to\\s+profile\\s+menu\\s*"),
    Logout("\\s*logout\\s*"),
    Exit("\\s*exit\\s*");
    private final String pattern ;
    DashboardCommands(String pattern) {
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input) {
        Matcher matcher = Pattern.compile(this.pattern).matcher(input);

        if (matcher.matches()) {
            return matcher;
        }
        return null;
    }
}
