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

/*
 * UniformFrequencyDiscretizer.java
 *
 */

/**
 *
 */

package keel.Algorithms.Genetic_Rule_Learning.MPLCS.Assistant.Discretizers.UniformFrequency_Discretizer;

import java.util.*;
import keel.Algorithms.Genetic_Rule_Learning.MPLCS.Assistant.Discretizers.Basic.*;
import keel.Algorithms.Genetic_Rule_Learning.MPLCS.Assistant.Globals.*;

public class UniformFrequencyDiscretizer extends Discretizer {
	double numInt;

	public UniformFrequencyDiscretizer(int _numInt) {
		numInt=_numInt;
	}

	protected Vector discretizeAttribute(int attribute,int []values,int begin,int end) {
		double quota=(end-begin+1)/numInt;
		double dBound=0.0;
		int i;
		int oldBound=0;

		Vector cp=new Vector();

		for(i=0;i<numInt-1;i++) {
			dBound+=quota;
			int iBound=(int)Math.round(dBound);
			if(iBound<=oldBound) continue;
			if(realValues[attribute][values[iBound-1]]!=realValues[attribute][values[iBound]]) {
				double cutPoint=(realValues[attribute][values[iBound-1]]+realValues[attribute][values[iBound]])/2.0;
				cp.addElement(new Double(cutPoint));
			} else {
				double val=realValues[attribute][values[iBound]];
				int numFW=1;
				while(iBound+numFW<=end && realValues[attribute][values[iBound+numFW]]==val) numFW++;
				if(iBound+numFW>end) numFW=end-begin+2;
				int numBW=1;
				while(iBound-numBW>oldBound && realValues[attribute][values[iBound-numBW]]==val) numBW++;
				if(iBound-numBW==oldBound) numBW=end-begin+2;

				if(numFW<numBW) {
					iBound+=numFW;
				} else if(numBW<numFW) {
					iBound-=numBW;
				} else {
					if(numFW==end-begin+2) {
						return cp;
					}
					if(Rand.getReal()<0.5) {
						iBound+=numFW;
					} else {
						iBound-=numBW;
						iBound++;
					}
				}
				double cutPoint=(realValues[attribute][values[iBound-1]]+realValues[attribute][values[iBound]])/2.0;
				cp.addElement(new Double(cutPoint));
			}
			oldBound=iBound;
		}

		return cp;
	}
}

