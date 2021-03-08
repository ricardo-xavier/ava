package com.example.tutorial.plugins;

import java.io.PrintStream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CalculoTempo {

    private List<Tempo> tempos;

    public static void main(String[] args) throws Exception {
        System.out.println("CalculaTempo " + args[0] + " " + args[1]);
        CalculoTempo calculo = new CalculoTempo();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        int minutos = calculo.calculaTempo(System.out,
            df.parse(args[0]), df.parse(args[1]), "08:00", "12:00", "14:00", "18:00", "25/12", "teste.plugin");
        System.out.printf("minutos = %d\n", minutos);
        List<Tempo> tempos = calculo.getTempos();
        System.out.println(tempos.size());
        for (Tempo tempo : tempos) {
            System.out.printf("%s %d\n", tempo.getInicio().toString(), tempo.getMinutos());
        }

        int tempoJaRegistrado = 0;
        for (int t=0; t<tempos.size(); t++) {

            Tempo tempo = tempos.get(t);
            System.out.printf("tempo %d/%d = %d - %d%n", t, tempos.size(), tempo.getMinutos(), tempoJaRegistrado);

            if (t > 0) {
                System.out.println("Iniciando registro: " + tempo.getInicio());
            }

            if (tempo.getMinutos() == 0) {
                tempo.setMinutos(1);
            }
            // o tempo eh cumulativo
            int tempoRegistrar = tempo.getMinutos() - tempoJaRegistrado;
            tempoJaRegistrado += tempoRegistrar;
            System.out.println("Finalizando registro:" + tempoRegistrar + "m " + tempo.getInicio());

        }
    }
	
	private boolean mesmoDia(Calendar c1, Date d2) {
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
	}

	private int calculaTempo(String h1, String h2) {
		int m1 = Integer.parseInt(h1.substring(0, 2)) * 60 + Integer.parseInt(h1.substring(3));
		int m2 = Integer.parseInt(h2.substring(0, 2)) * 60 + Integer.parseInt(h2.substring(3));
		return m2 - m1;
	}

	private int calculaTempo(String h1, String h2, String iniManha, String fimManha, String iniTarde, String fimTarde) {
		
		// ajusta horario de almoco
		int almoco = 0;
		String salvaH1 = h1;
		if (h2.compareTo(iniManha) <= 0) {
            return 0;
        }
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
	
	public int calculaTempo(PrintStream log, Date d1, Date d2, 
			String iniManha, String fimManha, String iniTarde, String fimTarde,
			String feriados, String usuario) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		
		DateFormat df = new SimpleDateFormat("dd/MM");
		DateFormat hf = new SimpleDateFormat("HH:mm");
		
		String h1 = hf.format(d1);
		String h2 = hf.format(d2);
		int minutos = 0;
        tempos = new ArrayList<Tempo>();

        DateFormat dfdmy = new SimpleDateFormat("dd/MM/yyyy");
		
		while (true) {
			
			//System.out.println(cal.getTime());
			
			// ignora sabado
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && !usuario.equals("teste.plugin")) {
				if (mesmoDia(cal, d2)) {
					break;
				}				
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}			
			
			// ignora domingo
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && !usuario.equals("teste.plugin")) {
				if (mesmoDia(cal, d2)) {
					break;
				}				
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			// ignora feriados
			String diaMes = df.format(cal.getTime());
			if (feriados.contains(diaMes) && !usuario.equals("teste.plugin")) {
				if (mesmoDia(cal, d2)) {
					break;
				}
				cal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}

            int m=0;
			if (mesmoDia(cal, d1)) {
				// dia inicial
				if (mesmoDia(cal, d2)) {
					// inicio e fim no mesmo dia
					m = calculaTempo(h1, h2, iniManha, fimManha, iniTarde, fimTarde);
					minutos += m;
                    log.println("            " + cal.getTime() + " " + h1 + " " + h2 + " " + minutos);
                    if (m > 0) {
                        tempos.add(new Tempo(cal, h1, h2, minutos));
                    }
					break;
				} else {
					m = calculaTempo(h1, fimTarde, iniManha, fimManha, iniTarde, fimTarde);
					minutos += m;
                    log.println("            " + cal.getTime() + " " + h1 + " " + fimTarde + " " + minutos);
                    if (m > 0) {
                        tempos.add(new Tempo(cal, h1, fimTarde, minutos));
                    }
				}
				
			} else {
				if (mesmoDia(cal, d2)) {
					// dia final
					m = calculaTempo(iniManha, h2, iniManha, fimManha, iniTarde, fimTarde);
					minutos += m;
                    log.println("            " + cal.getTime() + " " + iniManha + " " + h2 + " " + minutos);
                    if (m > 0) {
                        tempos.add(new Tempo(cal, iniManha, h2, minutos));
                    }
					break;
				} else {
					// dia intermediario
					m = calculaTempo(iniManha, fimTarde, iniManha, fimManha, iniTarde, fimTarde);
					minutos += m;
                    log.println("            " + cal.getTime() + " " + iniManha + " " + fimTarde + " " + minutos);
                    if (m > 0) {
                        tempos.add(new Tempo(cal, iniManha, fimTarde, minutos));
                    }
				}
			}
			
			// soma um dia
			cal.add(Calendar.DAY_OF_MONTH, 1);
			
		}

		return minutos;
	}

    public List<Tempo> getTempos() {
        return tempos;
    }

}
