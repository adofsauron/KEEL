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

package keel.Algorithms.Neural_Networks.NNEP_Common.util.random;

import java.util.Random;

import net.sf.jclec.util.random.AbstractRandGen;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>
 * @author Written by Pedro Antonio Gutierrez Penya, Aaron Ruiz Mora (University of Cordoba) 17/07/2007
 * @version 0.1
 * @since JDK1.5
 * </p>
 */

public class RanNnep extends AbstractRandGen
{
	/**
	 * <p>
	 * RandNnep is a random number generator proposed in 
	 * <code>
	 * "Turbo C. Programacion Avanzada" Herbert Schildt. 
	 * Borland-Osborne/McGraw-Hill (1990).
	 * Capitulo 9: Generadores de numeros aleatorios y simulaciones.
	 * ISBN:84-7615-508-5
	 * </code>
	 * </p>
	 */
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	private static final long serialVersionUID = 57064113894878138L;
	

	///////////////////////////////////////////////////////////////////
	// ----------------------------------------------------- Attributes
	///////////////////////////////////////////////////////////////////

	/** First property used in raw generation*/
	int a;

	/** Second property used in raw generation*/
	int b;
		
	/** Auxiliary randon generator*/
	Random rand = new Random(3816L);
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * <p>
	 * Empty constructor
	 * </p>
	 */

	public RanNnep() 
	{
		super();
	}

	/**
	 * <p>
	 * Default constructor (used by RandGenFactory)
	 * </p>
	 * @param seed Value used to set-up this random generator
	 */
	public RanNnep(int seed) 
	{
		// Call super constructor
		super();
		// Initialize
		a=100001;
		b=1;
		for(int i=0; i<seed%999; i++)
			raw();
	}

	////////////////////////////////////////////////////////////////////
	// ----------------------------- Overwriting Rangen abstract methods
	////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Return a double value in  the range [0,1]. This method will be
     * defined to make a working IRandGen.
     * </p>
     * @return a random double in the range [0,1]
     */
	public final double raw() 
	{
		double f = rand.nextDouble();
		if(f>.5)
			return rawA();
		else
			return rawB();
	}
	
    /////////////////////////////////////////////////////////////////
    // ---------------------------- Implementing IConfigure interface
    /////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Configuration settings for Randnnep random generator are:
     * 
     * <ul>
     * <li>
     * <code>a (int, default = 12345)</code>
     * </li><li>
     * <code>b (int, default = 67890</code>
     * </li>
     * </ul>
     * </p>
     * @param settings Configuration object to read the properties of
     */
	public void configure(Configuration settings) 
	{
		a=100001;
		b=1;
		// Get seed
		int seed = settings.getInt("seed", 7897);
		// Initialize
		for(int i=0; i<seed%999; i++)
			raw();
	}
	
	////////////////////////////////////////////////////////////////////
	// ------------------------------ Overwrite java.lang.Object methods
	////////////////////////////////////////////////////////////////////
	
    /**
     * <p>
     * Returns a String representation of the random generator
     * </p>
     */
    public String toString() 
    {
        ToStringBuilder tsb = new ToStringBuilder(this);
        // Append a value
        tsb.append("a", a);
        // Append b value
        tsb.append("b", b);
        // Return generated string
        return tsb.toString();
    }
    
    /**
     * <p>
     * Returns a hashcode identifier of the random generator
     * </p>
     * @return int Hashcode identifier
     */
    public int hashCode() 
    {
        HashCodeBuilder hcb = new HashCodeBuilder(41, 43);
        // Append a value
        hcb.append(a);
        // Append b value
        hcb.append(b);
        // Return hashcode
        return hcb.toHashCode();
    }
    
    /**
     * <p>
     * Compares if two random generators are equal
     * </p>
     * @return true if the generators are equals
     */
	public boolean equals(Object other)
	{
		if (other instanceof RanNnep) {
			RanNnep o = (RanNnep) other;
			EqualsBuilder eb = new EqualsBuilder();
			eb.append(this.a, o.a);
			eb.append(this.b, o.b);
			return eb.isEquals();
		}
		else {
			return false;
		}
	}
	
	////////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Private methods
	////////////////////////////////////////////////////////////////////

	/**
	 * <p>
	 * Returns a random number between 0 and 1 using "a" property
	 * </p>
	 * @return double Random number using "a" property
	 */
	protected double rawA(){
		a = (a*125) % 2796203;
		
		return (double) a/2796203;
	}
	
	/**
	 * <p>
	 * Returns a random number between 0 and 1 using "b" property
	 * </p>
	 * @return double Random number using "b" property
	 */
	protected double rawB(){
		b = (b*32719+3) % 32749;
		
		return (double) b/32749;
	}
}

