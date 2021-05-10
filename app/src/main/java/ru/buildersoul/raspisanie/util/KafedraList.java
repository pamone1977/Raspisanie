package ru.buildersoul.raspisanie.util;

import androidx.annotation.NonNull;

public class KafedraList
{
    // Содержится имя и id Кафедры
    public String name, id;

    public KafedraList(String id, String name)
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
