//  GAAlgorithm.java
//
//  Author:
//		 Felipe S. Cordeiro
//       Based in JMetal for:
//		 Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package methods.geneticAlgorithm;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.Comparator;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.comparators.FitnessComparator;

/**
 * Class implementing a generational genetic algorithm
 */
public class GAAlgorithm extends Algorithm {

  int copyBest;//number of individuos for reprodution
  double minConvergence;
  String dataset;
  int generations;
  /**
   *
   * Constructor
   * Create a new GGA instance.
   * @param problem Problem to solve.
   */
  public GAAlgorithm(String dataset, Problem problem, int numberCopyBest, double convergence, int generations){
    super(problem) ;
    this.copyBest = numberCopyBest;
    this.minConvergence = convergence;
    this.dataset = dataset;
    this.generations = generations;
  } // GGA

  /**
   * Execute the GGA algorithm
   * @throws JMException
   */
  public SolutionSet execute() throws JMException, ClassNotFoundException {
    int populationSize ;
    int maxEvaluations ;
    int evaluations    ;
    double currentConvergence = 0;
    double lastConvergence = 0;
    double convergence = 100;
    int generation = 0;

    SolutionSet population          ;
    SolutionSet offspringPopulation ;

    Operator    mutationOperator  ;
    Operator    crossoverOperator ;
    Operator    selectionOperator ;

    Comparator  comparator        ;
    comparator = new FitnessComparator() ; // Single objective comparator

    // Read the params
    populationSize = ((Integer)this.getInputParameter("populationSize")).intValue();
    maxEvaluations = ((Integer)this.getInputParameter("maxEvaluations")).intValue();
    // Initialize the variables
    population          = new SolutionSet(populationSize) ;
    offspringPopulation = new SolutionSet(populationSize) ;

    evaluations  = 0;

    // Read the operators
    mutationOperator  = this.operators_.get("mutation");
    crossoverOperator = this.operators_.get("crossover");
    selectionOperator = this.operators_.get("selection");
    generation++;
    // Create the initial population
    Solution newIndividual;
    //System.out.println("new generation: " + generation);
    for (int i = 0; i < populationSize; i++) {
      newIndividual = new Solution(problem_);
      problem_.evaluate(newIndividual);
      currentConvergence += newIndividual.getFitness();
      evaluations++;
      population.add(newIndividual);
    } //for
    currentConvergence /= populationSize;

    // Sort population
    population.sort(comparator) ;
    //writeFitnessMeasures(population.get(populationSize -1).getFitness(), generation);
    generation++;
    while (generation < this.generations){
      lastConvergence = currentConvergence;
      currentConvergence = 0;
      //System.out.println("new generation: " + generation);

      // Copy the bests individuals to the offspring population
      int bestSize =copyBest;//number even  
      for(int x =0; x < bestSize; x++){
        offspringPopulation.add(new Solution(population.get((populationSize - 1) - x)));
      }
      // Reproductive cycle
      for (int i = 0 ; i < ((populationSize - bestSize)/ 2) ; i ++) {
        // Selection
        Solution [] parents = new Solution[2];

        parents[0] = (Solution)selectionOperator.execute(population);
        parents[1] = (Solution)selectionOperator.execute(population);

        // Crossover
        Solution [] offspring = (Solution []) crossoverOperator.execute(parents);

        // Mutation
        mutationOperator.execute(offspring[0]);
        mutationOperator.execute(offspring[1]);

        // Evaluation of the new individual
        problem_.evaluate(offspring[0]);
        problem_.evaluate(offspring[1]);

        evaluations +=2;

        // Replacement: the two new individuals are inserted in the offspring
        //                population
        offspringPopulation.add(offspring[0]) ;
        offspringPopulation.add(offspring[1]) ;

      } // for

      // The offspring population becomes the new current population
      population.clear();
      for (int i = 0; i < populationSize; i++) {
        population.add(offspringPopulation.get(i)) ;
        currentConvergence += offspringPopulation.get(i).getFitness();
      }
      currentConvergence /= populationSize;
      convergence = Math.abs(currentConvergence - lastConvergence);
      //System.out.println("convergência: "+convergence + " min " + minConvergence);
      offspringPopulation.clear();

      //for (int i = 0; i < population.size(); i++)
      //System.out.println("fit " + i + ": " + population.get(i).getFitness());

      population.sort(comparator);
      //writeFitnessMeasures(population.get(populationSize - 1).getFitness(), generation);
      generation++;
    } // while

    // Return a population with the best individual
    SolutionSet resultPopulation = new SolutionSet() ;
    population.sort(comparator);
    resultPopulation = population;

    //System.out.println("Evaluations: " + evaluations ) ;
    return resultPopulation ;
  } // execute


  public void writeFitnessMeasures(Double measure1, int generation){
    try {
      DecimalFormat df = new DecimalFormat("0.####");
      FileOutputStream fos   = new FileOutputStream(this.dataset + "/results/SolutionSetGA/"  + "bestFitnessGeneration.txt", true)     ;
      OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
      BufferedWriter bw      = new BufferedWriter(osw)        ;
      bw.write(generation + " = " + df.format(measure1) +"\n");
      bw.close();
    }catch (IOException e) {
      Configuration.logger_.severe("Error acceding to the file");
      e.printStackTrace();

    } // printVariablesToFile
  }
} // GAAlgorithm