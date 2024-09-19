package com.example.notesapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z]).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean validate(final String password) {
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
