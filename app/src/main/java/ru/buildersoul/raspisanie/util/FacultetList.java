package ru.buildersoul.raspisanie.util;

import androidx.annotation.NonNull;

public class FacultetList
{
    // Содержится факультет имя и id
    public String name, id;

    public FacultetList(String id, String name)
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
