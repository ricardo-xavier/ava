package com.example.tutorial.plugins;

import java.util.Calendar;
import java.util.Date;

public class Tempo {

    private Date inicio;
    private int minutos;

    public Tempo(Calendar cal, String h1, String h2, int minutos) {
        int h = Integer.parseInt(h1.substring(0, 2));
        int m = Integer.parseInt(h1.substring(3, 5));
        inicio = cal.getTime();
        inicio.setHours(h);
        inicio.setMinutes(m);
        this.minutos = minutos;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public int getMinutos() {
        return minutos;
    }

    public void setMinutos(int minutos) {
        this.minutos = minutos;
    }
    
}
