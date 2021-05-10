package ru.buildersoul.raspisanie.util;

import androidx.annotation.NonNull;

public class GroupList
{
    // Содержится  имя и id Группы
    public String name, id;

    public GroupList(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}