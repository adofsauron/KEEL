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

package keel.Algorithms.ImbalancedClassification.CSMethods.MLPerceptronBackpropCS;

import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

import keel.Dataset.*;

/**
 * <p>
 * Class representing a neural network
 * </p>
 * @author Written by Nicolas Garcia Pedrajas (University of Cordoba) 27/02/2007
 * @author Modified by Victoria Lopez Morales (University of Granada) 23/05/2010
 * @author Modified by Victoria Lopez Morales (University of Granada) 28/09/2010
 * @version 0.1
 * @since JDK1.5
 */
public class Network {

    /** Number of layers */
    public int Nlayers;

    /** Number of units in each layer */
    public int Ninputs, Noutputs, Nhidden[];

    /** Matrix of weights */
    public double w[][][];

    /** Momentum term */
    public double momentum[][][];

    /** Delta weights */
    public double delta[][];

    /** Output of each node */
    public double activation[][];

    /** Transfer function parameters */
    public final double a = 1.7165, b_log = 1.5000, b_htan = 0.6666;

    /** Transfer function of each layer (LOG | HTAN | LINEAR)*/
    public String transfer[];

    /**
     * Empty Constructor.
     */
    public Network() {

    }

    /**
     * <p>
     * Constructor
     * </p>
     * @param global Global Definition parameters
     */
    public Network(Parameters global) {
        double range;

        transfer = new String[global.Nhidden_layers + 1];
        for (int i = 0; i < global.Nhidden_layers + 1; i++) {
            transfer[i] = global.transfer[i];
        }
        Ninputs = global.Ninputs;
        Noutputs = global.Noutputs;
        Nlayers = global.Nhidden_layers + 2;
        Nhidden = new int[Nlayers];
        w = new double[Nlayers - 1][][];
        delta = new double[Nlayers][];
        activation = new double[Nlayers][];
        momentum = new double[Nlayers - 1][][];
        Nhidden[0] = Ninputs;
        delta[0] = new double[Nhidden[0]];
        activation[0] = new double[Nhidden[0]];
        for (int i = 1; i < Nlayers; i++) {
            Nhidden[i] = global.Nhidden[i - 1];
            w[i - 1] = new double[Nhidden[i]][Nhidden[i - 1]];
            momentum[i - 1] = new double[Nhidden[i]][Nhidden[i - 1]];
            delta[i] = new double[Nhidden[i]];
            activation[i] = new double[Nhidden[i]];
        }
        Nhidden[Nlayers - 1] = Noutputs;

        // Initialize network weights
        for (int k = 0; k < Nlayers - 1; k++) {
            range = Math.sqrt(3.0) / Nhidden[k];
            for (int i = 0; i < Nhidden[k + 1]; i++) {
                for (int j = 0; j < Nhidden[k]; j++) {
                    w[k][i][j] = MLPerceptronBackpropCS.frandom( -range, range);

                }
            }
        }

    }

    /**
     * <p>
     * Train Network using cross validation
     * </p>
     * @param global Global Definition parameters
     * @param data Input data
     */
    public void TrainNetworkWithCrossvalidation(Parameters global, Data data) {

        double old_error, new_error = 0.0;

        if (global.problem.compareToIgnoreCase("Classification") == 0) {
            new_error = TestNetworkInClassification(global, data.validation,
                    global.n_val_patterns);
        } else if (global.problem.compareToIgnoreCase("Regression") == 0) {
            new_error = TestNetworkInRegression(global, data.validation,
                                                global.n_val_patterns);
        } else {
            System.err.println("Type of problem incorrectly defined");
            System.exit(1);
        }

        do {
            if (global.bp_type.compareToIgnoreCase("BPstd") == 0) {

                // Train cycles
                BackPropagation(global, global.cycles, data.train,
                                global.n_train_patterns);
            }
            /* else {
               BackPropagationErrorMax(global, global.cycles, data.train,
                                       global.n_train_patterns, sample);

             }*/
            old_error = new_error;

            if (global.problem.compareToIgnoreCase("Classification") == 0) {
                new_error = TestNetworkInClassification(global, data.validation,
                        global.n_val_patterns);
            } else if (global.problem.compareToIgnoreCase("Regression") == 0) {
                new_error = TestNetworkInRegression(global, data.validation,
                        global.n_val_patterns);
            }
        } while (new_error <= (1.0 - global.improve) * old_error);

    }

    /**
     * <p>
     * Train network without cross validation
     * </p>
     * @param global Global Definition parameters
     * @param data Input data
     * @param npatterns Number of patterns
     */
    public void TrainNetwork(Parameters global, double data[][], int npatterns) {

        if (global.bp_type.compareToIgnoreCase("BPstd") == 0) {
            // Train cycles
            BackPropagation(global, global.cycles, data,
                            npatterns);
        }
        /* else {
           BackPropagationErrorMax(global, global.cycles, data,
                                   npatterns, sample);

         }*/

    }

    /**
     * <p>
     * Test network in classification
     * </p>
     * @param global Global Definition parameters
     * @param data Input data
     * @param npatterns Number of patterns
     * @return Number of correctly classified per cent fitness
     */
    public double TestNetworkInClassification(Parameters global, double data[][],
                                              int npatterns) {
        double ok = 0.0;
        double fitness;

        for (int i = 0; i < npatterns; i++) {
            // Obtain network output
            GenerateOutput(data[i]);

            // Classify pattern
            int max_index = 0;
            for (int j = 1; j < Noutputs; j++) {
                if (activation[Nlayers - 1][max_index] <
                    activation[Nlayers - 1][j]) {
                    max_index = j;

                }
            }
            // Obtain class
            int Class = 0;
            for (int j = 1; j < Noutputs; j++) {
                if (data[i][Class + Ninputs] < data[i][j + Ninputs]) {
                    Class = j;

                    // Test if correctly classified
                }
            }
            if (Class == max_index) {
                ok++;
            }
        }

        fitness = ok / npatterns;

        return fitness;

    }
    
    /**
     * <p>
     * Test network in regression
     * </p>
     * @param global Global Definition parameters
     * @param data Input data
     * @param npatterns no of patterns
     * @return RMS error fitness
     */
    public double TestNetworkInRegression(Parameters global, double data[][],
                                          int npatterns) {
        double fitness, RMS = 0.0, error;

        for (int i = 0; i < npatterns; i++) {
            // Obtain network output
            GenerateOutput(data[i]);

            // Obtain RMS error
            error = 0.0;
            for (int j = 0; j < Noutputs; j++) {
                error +=
                        Math.pow(activation[Nlayers - 1][j] - data[i][Ninputs +
                                 j], 2.0);

                RMS += Math.sqrt(error);
            }
        }

        fitness = RMS / (npatterns * Noutputs);

        return fitness;
    }

    /**
     * <p>
     * Back Propagation algorithm
     * </p>
     * @param global Global definition parameters
     * @param cycles Number of cycles
     * @param data Training data
     * @param npatterns Number of patterns
     */
    private void BackPropagation(Parameters global, int cycles, double data[][],
                                 int npatterns) {
        int pattern;
        double change;

        double[] error = new double[Noutputs];

        // Momentum set to 0
        for (int k = 0; k < Nlayers - 1; k++) {
            for (int i = 0; i < Nhidden[k + 1]; i++) {
                for (int j = 0; j < Nhidden[k]; j++) {
                    momentum[k][i][j] = 0.0;

                }
            }
        }
        for (int iter = 0; iter < cycles; iter++) {
            // Choose a random pattern
            pattern = MLPerceptronBackpropCS.irandom(0, npatterns);

            // Forward pass
            GenerateOutput(data[pattern]);

            // Obtain error for output nodes
            for (int i = 0; i < Noutputs; i++) {
                error[i] = data[pattern][Ninputs + i] - activation[Nlayers -
                           1][i];
            }

            // Compute deltas for output
            for (int i = 0; i < Noutputs; i++) {
                if (transfer[Nlayers - 2].compareToIgnoreCase("Log") == 0) {
                    delta[Nlayers -
                            1][i] = error[i] * b_log * activation[Nlayers -
                                    1][i] *
                                    (1.0 - activation[Nlayers - 1][i] / a);
                } else if (transfer[Nlayers - 2].compareToIgnoreCase("Htan") ==
                           0) {
                    delta[Nlayers -
                            1][i] = error[i] * (b_htan / a) *
                                    (a - activation[Nlayers - 1][i]) *
                                    (a + activation[Nlayers - 1][i]);
                } else {
                    delta[Nlayers - 1][i] = error[i];

                }
            }

            // Compute deltas for hidden nodes
            for (int k = Nlayers - 2; k > 0; k--) {
                for (int i = 0; i < Nhidden[k]; i++) {
                    delta[k][i] = 0.0;
                    for (int j = 0; j < Nhidden[k + 1]; j++) {
                        delta[k][i] += delta[k + 1][j] * w[k][j][i];
                    }
                    if (transfer[k - 1].compareToIgnoreCase("Log") == 0) {
                        delta[k][i] *= b_log * activation[k][i] *
                                (1.0 - activation[k][i] / a);
                    } else if (transfer[k - 1].compareToIgnoreCase("Htan") == 0) {
                        delta[k][i] *= (b_htan / a) * (a - activation[k][i]) *
                                (a + activation[k][i]);
                    }
                }
            }

            // Update weights
            for (int k = Nlayers - 2; k >= 0; k--) {
                for (int i = 0; i < Nhidden[k + 1]; i++) {
                    for (int j = 0; j < Nhidden[k]; j++) {
                        change = global.eta * delta[k + 1][i] * activation[k][j] +
                                 global.alpha * momentum[k][i][j] -
                                 global.lambda * w[k][i][j];
                        w[k][i][j] += change;
                        momentum[k][i][j] = change;
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Generate output at activation using input
     * </p>
     * @param input Input data
     */
    public void GenerateOutput(double input[]) {

        for (int i = 1; i < Nlayers; i++) {
            for (int j = 0; j < Nhidden[i]; j++) {
                activation[i][j] = 0.0;

                // Inputs to first  layer
            }
        }
        for (int i = 0; i < Nhidden[0]; i++) {
            activation[0][i] = input[i];

            // Rest of layers
        }
        for (int k = 1; k < Nlayers; k++) {
            for (int i = 0; i < Nhidden[k]; i++) {
                activation[k][i] = 0.0;
                for (int j = 0; j < Nhidden[k - 1]; j++) {
                    activation[k][i] += activation[k - 1][j] * w[k - 1][i][j];
                }
                if (transfer[k - 1].compareToIgnoreCase("Log") == 0) {
                    activation[k][i] = logistic(activation[k][i]);
                } else if (transfer[k - 1].compareToIgnoreCase("Htan") == 0) {
                    activation[k][i] = htan(activation[k][i]);

                }
            }
        }
    }

    /**
     * <p>
     * Generate output using input
     * </p>
     * @param input Input data
     * @param output Output data
     */
    public void GenerateOutput(double input[], double output[]) {

        GenerateOutput(input);

        // Copy to output
        for (int i = 0; i < Noutputs; i++) {
            output[i] = activation[Nlayers - 1][i];

        }
    }

    /**
     * <p>
     * Return Logistic function
     * </p>
     * @param x Function argument
     * @return Logistic transformation of x
     */
    private double logistic(double x) {
        double sig = a / (1.0 + Math.exp( -b_log * x));
        return sig; // (sig < -1.0)? (-1.0):(sig> 1.0)? (1.0): sig;
    }

    /**
     * <p>
     * Return hyperbolic tangent function
     * </p>
     * @param x Function argument
     * @return Hyperbolic transformation of x
     */
    private double htan(double x) {

        double sig = (Math.exp(b_htan * x) - Math.exp( -b_htan * x)) /
                     (Math.exp(b_htan * x) + Math.exp( -b_htan * x));
        return a * sig;
    }

    /**
     * <p>
     * Save network weights to a file
     * </p>
     * @param file_name Output file name
     * @param append Append or overwrite flag
     */
    public void SaveNetwork(String file_name, boolean append) {

        // Open file stream
        // Training data
        try {
            FileOutputStream file = new FileOutputStream(file_name, append);
            DataOutputStream dataOut = new DataOutputStream(file);

            // Save network parameters
            dataOut.writeInt(Nlayers);
            for (int i = 0; i < Nlayers; i++) {
                dataOut.writeInt(Nhidden[i]);
            }

            for (int i = 0; i < Nlayers - 1; i++) {
                if (transfer[i].compareToIgnoreCase("Log") == 0) {
                    dataOut.writeInt(1);
                } else if (transfer[i].compareToIgnoreCase("Htan") == 0) {
                    dataOut.writeInt(2);
                } else {
                    dataOut.writeInt(3);
                }
            }

            // Save weights
            for (int k = 0; k < Nlayers - 1; k++) {
                for (int i = 0; i < Nhidden[k + 1]; i++) {
                    for (int j = 0; j < Nhidden[k]; j++) {
                        dataOut.writeDouble(w[k][i][j]);

                    }
                }
            }
            dataOut.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to create network file");
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("IO exception");
            System.exit(1);
        }

    }

    /**
     * <p>
     * Load network weights from a file
     * </p>
     * @param file_name Input file name
     */
    public void LoadNetwork(String file_name) {

        // Open file stream
        try {
            FileInputStream file = new FileInputStream(file_name);
            DataInputStream dataIn = new DataInputStream(file);

            // Load network parameters
            Nlayers = dataIn.readInt();
            for (int i = 0; i < Nlayers; i++) {
                Nhidden[i] = dataIn.readInt();
            }

            for (int i = 0; i < Nlayers - 1; i++) {
                int t = dataIn.readInt();
                if (t == 1) {
                    transfer[i] = "Log";
                } else if (t == 2) {
                    transfer[i] = "Htan";
                } else {
                    transfer[i] = "Lin";
                }
            }

            Ninputs = Nhidden[0];
            Noutputs = Nhidden[Nlayers - 1];

            // Load weights
            for (int k = 0; k < Nlayers - 1; k++) {
                for (int i = 0; i < Nhidden[k + 1]; i++) {
                    for (int j = 0; j < Nhidden[k]; j++) {
                        w[k][i][j] = dataIn.readDouble();

                    }
                }
            }

            dataIn.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to load network file");
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("IO exception");
            System.exit(1);
        }

    }

    /**
     * <p>
     * Print weights to screen. Not used
     * </p>
     */
    public void PrintWeights() {

        // Print all layers
        for (int k = 0; k < Nlayers - 1; k++) {
            System.out.println("Hidden[" + k + "] -> Hidden[" + (k + 1) + "]");
            System.out.println("Node\tWeights");
            for (int i = 0; i < Nhidden[k + 1]; i++) {
                System.out.print(i + 1 + "\t");
                for (int j = 0; j < Nhidden[k]; j++) {
                    System.out.print(w[k][i][j] + " ");
                }
                System.out.println();
            }
        }
    }

    /**
     * <p>
     * Check if a pattern is correctly classified
     * </p>
     * @param pattern Pattern to check
     * @return true if it is correctly classified. False otherwise
     */
    public boolean NetClassifyPattern(double pattern[]) {

        // Obtain network output
        GenerateOutput(pattern);

        // Classify pattern
        int max_index = 0;
        for (int j = 1; j < Noutputs; j++) {
            if (activation[Nlayers - 1][max_index] < activation[Nlayers - 1][j]) {
                max_index = j;

            }
        }
        // Obtain class
        int Class = 0;
        for (int j = 1; j < Noutputs; j++) {
            if (pattern[Class + Ninputs] < pattern[j + Ninputs]) {
                Class = j;

            }
        }
        // Test if correctly classified
        if (Class == max_index) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * <p>
     * Return the class where a pattern is classified
     * </p>
     * 
     * @param pattern	Pattern to check
     * @param positive_class Positive class in the imbalanced classification problem
     * @param positive_cost Cost of misclassifying an instance from the positive class
     * @param negative_cost Cost of misclassifying an instance from the negative class
     * @return Class index
     */
    public int NetGetClassOfPattern (double pattern[], int positive_class, double positive_cost, double negative_cost) {
        double sum = 0;
        double sum_negative = 0;
        double min_value = Double.MAX_VALUE;
    	boolean positive_activation = false;
    	
    	// Obtain network output
        GenerateOutput(pattern);

        // Normalize to an interval [0,1] according to the restrictions
        for (int j = 0; j < Noutputs; j++) {
        	if (activation[Nlayers - 1][j] >= 0) {
        		positive_activation = true;
        	}
        	else {
        		sum_negative += activation[Nlayers - 1][j];
        		if (activation[Nlayers - 1][j] < min_value) {
        			min_value = activation[Nlayers - 1][j];
        		}
        	}
        	sum += Math.abs(activation[Nlayers - 1][j]);
        }

    	// Creating a small data structure to sort the output values
    	final class OutputValue implements Comparable {
            private double real_output;
            private int associated_class;
            
            public OutputValue (double output, int my_class) {
            	real_output = output;
            	associated_class = my_class;
            }
            
            public double get_real_output () {
            	return real_output;
            }
            
            public void set_real_output (double value) {
            	real_output = value;
            }
            
            public int get_associated_class () {
            	return associated_class;
            }
            
            public int compareTo (Object otherOutputValue){
            	if( !(otherOutputValue instanceof OutputValue)) {
            		throw new ClassCastException("Invalid object");
           		}

           		double other_value = ((OutputValue)otherOutputValue).get_real_output();

           		if (this.get_real_output() > other_value)
            		return 1;
           		else if (this.get_real_output() < other_value)
            		return -1;
           		else
            		return 0;
            }
        }        
        
    	ArrayList <OutputValue> outputs_for_normalization;
    	outputs_for_normalization = new ArrayList <OutputValue> (Noutputs);
    	
    	if (positive_activation && (sum_negative == 0)) {
        	// All the values are positive
        	for (int j = 0; j < Noutputs; j++) {
        		outputs_for_normalization.add(new OutputValue(activation[Nlayers - 1][j]/sum, j));
            }
        }
        else { 
        	OutputValue tmp;
        	
        	if (positive_activation) {
        		int first_positive = -1;
        		double negative_extra = sum_negative/sum;
        		
        		// There are positive and negative values
        		for (int j = 0; j < Noutputs; j++) {
	            	outputs_for_normalization.add(new OutputValue(activation[Nlayers - 1][j], j));
	            }
        		
        		// Sort the values
	    		Collections.sort(outputs_for_normalization);
	    		
	    		// Find where the positive values start
	    		for (int j=1; (j < Noutputs) && (first_positive < 0); j++) {
	    			if (((OutputValue)outputs_for_normalization.get(j)).get_real_output() >= 0) {
	    				first_positive = j;
	    			}
	    			else {
	    				((OutputValue)outputs_for_normalization.get(j)).set_real_output(-((OutputValue)outputs_for_normalization.get(j)).get_real_output());
		    		}
	    		}
	    		
	    		if (first_positive == 1) {
	    			// If there is only a negative value
	        		for (int j = 0; j < Noutputs; j++) {
	        			((OutputValue)outputs_for_normalization.get(j)).set_real_output(((OutputValue)outputs_for_normalization.get(j)).get_real_output()-min_value);
	        			((OutputValue)outputs_for_normalization.get(j)).set_real_output(((OutputValue)outputs_for_normalization.get(j)).get_real_output()/sum);
			        }
				}
	    		else {
	    			// There are several negative values
		    		// Exchange the values for negatives
		    		for (int j=0; j < first_positive/2; j++) {
		    			tmp = (OutputValue)outputs_for_normalization.get(j);
		    			outputs_for_normalization.set(j, (OutputValue)outputs_for_normalization.get(Noutputs-1-j));
		    			outputs_for_normalization.set(Noutputs-1-j, tmp);
		    		}
	        		for (int j = 0; j < Noutputs; j++) {
	        			((OutputValue)outputs_for_normalization.get(j)).set_real_output(((OutputValue)outputs_for_normalization.get(j)).get_real_output()/sum);
		            }
		    		// Add the negative extra for positive values
		    		for (int j=first_positive; j < Noutputs; j++) {
		    			((OutputValue)outputs_for_normalization.get(j)).set_real_output(((OutputValue)outputs_for_normalization.get(j)).get_real_output()+negative_extra);
		    		}
				}
        	}
        	else {
	        	// All the values are negative
	        	for (int j = 0; j < Noutputs; j++) {
	            	outputs_for_normalization.add(new OutputValue(-activation[Nlayers - 1][j]/sum, j));
	            }
	    		
	    		// Sort the values
	    		Collections.sort(outputs_for_normalization);
	    		
	    		// Exchange the values
	    		for (int j=0; j < Noutputs/2; j++) {
	    			tmp = (OutputValue)outputs_for_normalization.get(j);
	    			outputs_for_normalization.set(j, (OutputValue)outputs_for_normalization.get(Noutputs-1-j));
	    			outputs_for_normalization.set(Noutputs-1-j, tmp);
	    		}
	    	}
        }
        
    	// For every output, multiply it with the sum of the costs of misclassifyng the corresponding class to other classes
    	for (int j = 0; j < Noutputs; j++) {
    		OutputValue tmp = (OutputValue)outputs_for_normalization.get(j);
    		
    		if (tmp.get_associated_class() == positive_class) {
    			tmp.set_real_output(tmp.get_real_output()*positive_cost);
    		}
    		else {
    			tmp.set_real_output(tmp.get_real_output()*negative_cost);
    		}
        }
    	// Normalize again, however, now all values are positive
    	sum = 0;
        for (int j = 0; j < Noutputs; j++) {
        	sum += ((OutputValue)outputs_for_normalization.get(j)).get_real_output();
        }
        for (int j = 0; j < Noutputs; j++) {
        	((OutputValue)outputs_for_normalization.get(j)).set_real_output((((OutputValue)outputs_for_normalization.get(j)).get_real_output())/sum);
        }
        
        // Classify pattern with the biggest output
        int Class = ((OutputValue)outputs_for_normalization.get(0)).get_associated_class();
        int pos_max = 0;
        for (int j = 1; j < Noutputs; j++) {
        	if (((OutputValue)outputs_for_normalization.get(pos_max)).get_real_output() < ((OutputValue)outputs_for_normalization.get(j)).get_real_output()) {
                Class = ((OutputValue)outputs_for_normalization.get(j)).get_associated_class();
                pos_max = j;
            }
        }

        return Class;
    }

    /**
     * <p>
     * Return the real class of a pattern
     * </p>
     * @param pattern Pattern to check
     * @return Class index
     */
    private int GetClassOfPattern(double pattern[]) {

        // Get class of pattern
        int max_index = 0;
        for (int j = 1; j < Noutputs; j++) {
            if (pattern[max_index] < pattern[j]) {
                max_index = j;

            }
        }
        return max_index;
    }

    /**
     * <p>
     * Save output data to file
     * </p>
     * @param file_name Output file name
     * @param data Input data
     * @param n Data matrix order (number of rows and columns)
     * @param problem Type of problem (CLASSIFICATION | REGRESSION )
     * @throws IOException
     */
    public void SaveOutputFile(String file_name, double data[][], int n,
                               String problem, int positive_class, double positive_cost, double negative_cost) {
        //String line;

        try {
            // Result file
            FileOutputStream file = new FileOutputStream(file_name);
            BufferedWriter f = new BufferedWriter(new OutputStreamWriter(file));

            // File header
            f.write("@relation " + Attributes.getRelationName() + "\n");
            f.write(Attributes.getInputAttributesHeader());
            f.write(Attributes.getOutputAttributesHeader());
            f.write(Attributes.getInputHeader() + "\n");
            f.write(Attributes.getOutputHeader() + "\n");
            f.write("@data\n");

            // For all patterns
            for (int i = 0; i < n; i++) {

                // Classification
                if (problem.compareToIgnoreCase("Classification") == 0) {
                		Attribute a = Attributes.getOutputAttribute(0);
										int tipo = a.getType();
                    // Obtain class
                    int Class = 0;
                    for (int j = 1; j < Noutputs; j++) {
                        if (data[i][Class + Ninputs] < data[i][j + Ninputs]) {
                            Class = j;

                        }
                    }

                    if(tipo!=Attribute.NOMINAL){
                    	f.write(Integer.toString(Class) + " ");
                    	f.write(Integer.toString(NetGetClassOfPattern(data[i], positive_class, positive_cost, negative_cost)));
                  	}
                  	else{
                  		f.write(a.getNominalValue(Class) + " ");
                    	f.write(a.getNominalValue(NetGetClassOfPattern(data[i], positive_class, positive_cost, negative_cost)));
                  	}
                    f.newLine();
                }
                // Regression
                else {
                    for (int j = 0; j < Noutputs; j++) {
                        f.write(Double.toString(data[i][Ninputs + j]) + " ");

                    }
                    GenerateOutput(data[i]);
                    for (int j = 0; j < Noutputs; j++) {
                        f.write(Double.toString(activation[Nlayers - 1][j]) +
                                " ");

                    }
                    f.newLine();
                }
            }
            f.close();
            file.close();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot created output file");
            System.exit( -1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit( -1);
        }

    }
    
}
