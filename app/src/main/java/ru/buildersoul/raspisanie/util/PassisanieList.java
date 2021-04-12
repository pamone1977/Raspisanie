package ru.buildersoul.raspisanie.util;

import androidx.annotation.NonNull;

public class PassisanieList
{
    public String time_start, time_end, dic, type_zan, fio, korpys, ayditoria, nedel;

    public PassisanieList(String time_start, String time_end, String dic, String type_zan, String fio, String korpys, String ayditoria, String nedel)
    {
        this.time_start = time_start;
        this.time_end = time_end;
        this.dic = dic;
        this.type_zan = type_zan;
        this.fio = fio;
        this.korpys = korpys;
        this.ayditoria = ayditoria;
        this.nedel = nedel;
    }

    @NonNull
    @Override
    public String toString() {
        return "<p><h5>"+(nedel.equalsIgnoreCase("0") ? "Без недели" : nedel.equalsIgnoreCase("-1") ? "Верхняя неделя" : "Нижняя неделя")+"</h5><small><strong>"+time_start + "-" + time_end + "</strong></small><br><i>" + dic + "</i> (" +type_zan+ ")<br>" + fio + " ауд." +korpys+"/"+ayditoria+ "<br></p><br>-----------------------------------------------";
    }
}
