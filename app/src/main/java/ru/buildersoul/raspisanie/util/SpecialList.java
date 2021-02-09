package ru.buildersoul.raspisanie.util;

import androidx.annotation.NonNull;

public class SpecialList
{
    public String name, id, obrag_programm;

    public SpecialList(String id, String name, String obrag_programm)
    {
        this.id = id;
        this.name = name;
        this.obrag_programm = obrag_programm;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " " + obrag_programm;
    }
}
