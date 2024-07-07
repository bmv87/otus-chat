package ru.otus.chat.server.db.entities;

public enum RoleEnum {
    USER("Пользователь"),
    ADMIN("Администратор");

    private final String description;

    RoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
