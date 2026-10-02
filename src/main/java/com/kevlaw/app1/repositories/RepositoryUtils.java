package com.kevlaw.app1.repositories;

public class RepositoryUtils {

    public static boolean isConstraintViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
                return true;
            }
        }
        return false;
    }
}
