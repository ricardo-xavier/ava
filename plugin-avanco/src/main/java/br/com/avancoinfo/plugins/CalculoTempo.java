package com.example.tutorial.plugins;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalculoTempo {
	
	private static boolean mesmoDia(Calendar c1, Date d2) {
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
	}

	private static int calculaTempo(String h1, String h2) {
		int m1 = Integer.parseInt(h1.substring(0, 2)) * 60 + Integer.parseInt(h1.substring(3));
		int m2 = Integer.parseInt(h2.substring(0, 2)) * 60 + Integer.parseInt(h2.substring(3));
		return m2 - m1;
	}

	private static int calculaTempo(String h1, String h2, String iniManha, String fimManha, String iniTarde, String fimTarde) {
		
		// ajusta horario de almoco
		int almoco = 0;
		String salvaH1 = h1;
		if (h1.compareTo(fimManha) > 0 && h1.compareTo(iniTarde) < 0) {
			// comecou no horario do almoco
			almoco = calculaTempo(h1, iniTarde);
			h1 = iniTarde;
		}
		if (h2.compareTo(fimManha) > 0 && h2.compareTo(iniTarde) < 0) {
			// terminou no horario do almoco
			if (almoco > 0) {
				// comecou e terminou no horario do almoco
				return calculaTempo(salvaH1, h2);
			}
			almoco = calculaTempo(fimManha, h2);
			h2 = fimManha;
		}
		
		if (h1.compareTo(fimManha) <= 0) {
			// comecou de manha
			if (h2.compareTo(fimManha) <= 0) {
				// comecou e terminou de manha
				return almoco + calculaTempo(h1, h2);
			} else {
				// comecou de manha e terminou a tarde
				int manha = calculaTempo(h1, fimManha);
				int tarde = calculaTempo(iniTarde, h2);
				return almoco + manha + tarde;
			}
		}
		
		// comecou de tarde
		return almoco + calculaTempo(h1, h2);
	}
	
	public static int calculaTempo(Date d1, Date d2, 
			String iniManha, String fimManha, String iniTarde, String fimTarde,
			String feriados) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		
		DateFormat df = new SimpleDateFormat("dd/MM");
		DateFormat hf = new SimpleDateFormat("HH:mm");
		
		String h1 = hf.format(d1);
		String h2 = hf.format(d2);
		int minutos = 0;
		
		while (true) {
			
			//System.out.println(cal.getTime());
			
			// ignora sabado
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				if (mesmoDia(cal, d2)) {
					break;
				}				
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}			
			
			// ignora domingo
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				if (mesmoDia(cal, d2)) {
					break;
				}				
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			// ignora feriados
			String diaMes = df.format(cal.getTime());
			if (feriados.contains(diaMes)) {
				if (mesmoDia(cal, d2)) {
					break;
				}
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}

			if (mesmoDia(cal, d1)) {
				// dia inicial
				if (mesmoDia(cal, d2)) {
					// inicio e fim no mesmo dia
					minutos += calculaTempo(h1, h2, iniManha, fimManha, iniTarde, fimTarde);
					break;
				} else {
					minutos += calculaTempo(h1, fimTarde, iniManha, fimManha, iniTarde, fimTarde);
				}
				
			} else {
				if (mesmoDia(cal, d2)) {
					// dia final
					minutos += calculaTempo(iniManha, h2, iniManha, fimManha, iniTarde, fimTarde);
					break;
				} else {
					// dia intermediario
					minutos += calculaTempo(iniManha, fimTarde, iniManha, fimManha, iniTarde, fimTarde);
				}
			}
			
			// soma um dia
			cal.add(Calendar.DAY_OF_MONTH, 1);
			
		}

		return minutos;
	}
	

}
