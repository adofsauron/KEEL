/***********************************************************************

	This file is part of KEEL-software, the Data Mining tool for regression, 
	classification, clustering, pattern mining and so on.

	Copyright (C) 2004-2010
	
	F. Herrera (herrera@decsai.ugr.es)
    L. S�nchez (luciano@uniovi.es)
    J. Alcal�-Fdez (jalcala@decsai.ugr.es)
    S. Garc�a (sglopez@ujaen.es)
    A. Fern�ndez (alberto.fernandez@ujaen.es)
    J. Luengo (julianlm@decsai.ugr.es)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see http://www.gnu.org/licenses/
  
**********************************************************************/

package keel.Algorithms.Rule_Learning.AQ;

import java.util.StringTokenizer;
import org.core.Files;

/**
 * <p>Title: AQ Main Program</p>
 * <p>Description: This is the main class, which is executed when we launch the program</p>
 * @author Written by Alberto Fern�ndez (University of Granada) 11/26/2004
 * @version 1.0
 * @since JDK1.4
 */
public class Main {

    private String ficheroTrain;
    private String ficheroEval;
    private String ficheroTest;
    private String ficheroSalidatr;
    private String ficheroSalidatst;
    private String ficheroSalida;
    private long semilla;
    private int tamEstrella;
    private int eficacia;
    //private double w;

    /** Default builder */
    public Main() {
    }

    /**
     * It obtains all the neccesary information of the parameter file<br/>
     * First, it reads the names of the training and tests data-set files<br/>
     * Then, it reads the output files<br/>
     * Finally, it reads the algorithm parameters, such as the seed or the number of iterations<br/>
     *
     * @param nomFichero Name of the parameter file
     *
     */
    private void preparaArgumentos(String nomFichero) {
        StringTokenizer linea, datos;
        String fichero = Files.readFile(nomFichero);
        String una_linea;
        linea = new StringTokenizer(fichero, "\n\r");
        linea.nextToken(); //Algorithm name
        una_linea = linea.nextToken();
        datos = new StringTokenizer(una_linea, " = \" ");
        datos.nextToken(); //inputData
        ficheroTrain = datos.nextToken();
        ficheroEval = datos.nextToken(); //evaluation file
        ficheroTest = datos.nextToken();
        una_linea = linea.nextToken();
        datos = new StringTokenizer(una_linea, " = \" ");
        datos.nextToken(); //outputData
        ficheroSalidatr = datos.nextToken();
        ficheroSalidatst = datos.nextToken();
        ficheroSalida = datos.nextToken();
        una_linea = linea.nextToken();
        datos = new StringTokenizer(una_linea, " = \" ");
        datos.nextToken(); //seed
        semilla = Long.parseLong(datos.nextToken());
        una_linea = linea.nextToken();
        datos = new StringTokenizer(una_linea, " = \" ");
        datos.nextToken();
        tamEstrella = Integer.parseInt(datos.nextToken());
        una_linea = linea.nextToken();
        datos = new StringTokenizer(una_linea, " = \" ");
        datos.nextToken();
        String eficaciaAux = datos.nextToken();
        eficacia = 0;
        if (eficaciaAux.compareTo("YES") == 0) {
            eficacia = 1;
        }
    };

    /**
     * It launches the AQ program
     */
    private void execute() {
        AQ mi_aq = new AQ(ficheroTrain, ficheroEval, ficheroTest,
                          ficheroSalidatr, ficheroSalidatst, ficheroSalida,
                          semilla, tamEstrella, eficacia);
        if (mi_aq.everythingOK()) {
            mi_aq.execute();
        }
    }

    /**
     * Main program
     * @param args It contains the name of the parameter file<br/>
     * Format:<br/>
     * <em>algorith = &lt;algorithm name&gt;</em><br/>
     * <em>inputData = "&lt;training file&gt;" "&lt;validation file&gt;" "&lt;test file&gt;"</em> ...<br/>
     * <em>outputData = "&lt;training file&gt;" "&lt;test file&gt;"</em> ...<br/>
     * <br/>
     * <em>seed = value</em><br/>
     * <em>&lt;Description1&gt; = &lt;value1&gt;</em><br/>
     * <em>&lt;Description2&gt; = &lt;value2&gt;</em> ...<br/>
     */
    public static void main(String[] args) {
        long t_ini = System.currentTimeMillis();
        Main ppal = new Main();
        ppal.preparaArgumentos(args[0]);
        ppal.execute();
        long t_fin = System.currentTimeMillis();
        long t_exec = t_fin - t_ini;
        long hours = t_exec / 3600000;
        long rest = t_exec % 3600000;
        long minutes = rest / 60000;
        rest %= 60000;
        long seconds = rest / 1000;
        rest %= 1000;
        System.out.println("Execution Time: " + hours + ":" + minutes + ":" +
                           seconds + "." + rest);
    }

}

