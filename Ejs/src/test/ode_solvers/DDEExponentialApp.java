package test.ode_solvers;

import java.awt.Color;

import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.numerics.ODE;
import org.opensourcephysics.numerics.ode_interpolation.StateHistory;
import org.opensourcephysics.numerics.ode_solvers.DelayDifferentialEquation;
import org.opensourcephysics.numerics.ode_solvers.InterpolatorEventSolver;
import org.opensourcephysics.numerics.ode_solvers.rk.*;

/**
 * Example of direct use of an InterpolatorEventSolver
 * 
 * @author Francisco Esquembre
 * @author Rafael Chicón
 * @version 1.0 December 2013
 */
public class DDEExponentialApp {

  static public void main (String[] args) {

    // ODE
    ODE ode = new DDEExponential();
    double initTime = 0, maxTime = 8;

    // Solver and its parameters
    InterpolatorEventSolver solver = new InterpolatorEventSolver(new CashKarp45(), ode);

    double stepSize = 0.01; // The initial step size (used by fixed step methods)
    double plotStepSize = 0.01; // The step size for plotting the solution
    double absTol = 1.0e-6, relTol = 1.0e-3; // The tolerance for adaptive methods

    // Initialize and customize the solver
    solver.initialize(stepSize);       // This step size affects the solver internal step size
    solver.setStepSize(plotStepSize);  // This step size is the reading step size
    solver.setTolerances(absTol,relTol);
    //    eventSolver.setDDEIterations(500);
    solver.setHistoryLength(Double.POSITIVE_INFINITY); // Recall all past values

    // main loop for solving the ODE
    double[] state = ode.getState();
    int timeIndex = state.length-1;

    // Solve for the whole [initTime,maxTime] interval at once
    while (solver.getCurrentTime()<maxTime) {
      try{
        solver.step();
        if (solver.getErrorCode()!=InterpolatorEventSolver.ERROR.NO_ERROR) {
          System.err.println ("Error when advancing the solution from "+solver.getCurrentTime()); //$NON-NLS-1$
          break;
        }
      } catch(Exception exc) {
        exc.printStackTrace();
        break;
      }
    }

    // Compute max error at each plot point
    StateHistory history = solver.getStateHistory();

    { // plot the error
      double maxError = 0;
      double maxRelError = 0;
      double[] interpolated = new double[ode.getState().length]; 
      double time = initTime;
      while (time<=maxTime) {
        history.interpolate(time, interpolated);   
        double error = 0, relError = 0;   
        for (int k=0; k<timeIndex; k++) {
          double errorK = Math.abs(solution(interpolated[timeIndex]) - interpolated[k]);
          error = Math.max(error,errorK);
          if (Math.abs(interpolated[k])>InterpolatorEventSolver.EPSILON) relError = Math.max(relError, error/Math.abs(interpolated[k]));
        }
        System.out.println("Time = " + time + ", value = "+interpolated[0]+ ", Error(t) = " + error + " Relative error(t) = "+relError);
        maxError = Math.max(maxError, error);
        maxRelError = Math.max(maxRelError, relError);
        time += plotStepSize;
      } 
      System.out.println("Max error = " + maxError + " Max relative error = "+maxRelError);
    }

    
    {  // Plot the graphs
      // Prepare the graphics
      Dataset stripChart = new Dataset(Color.BLUE, Color.BLUE, true);
      stripChart.setMarkerShape(Dataset.NO_MARKER);
      Dataset solutionStripChart = new Dataset(Color.RED, Color.RED, true);
      solutionStripChart.setMarkerShape(Dataset.NO_MARKER);

      double time = initTime;
      double[] interpolated = new double[ode.getState().length]; 
      while (time<=maxTime) {
        history.interpolate(time, interpolated);   
        stripChart.append(time, interpolated[0]);
        solutionStripChart.append(time, solution(time));
        time += plotStepSize;
      }

      PlottingPanel plottingPanel = new PlottingPanel("time", "state", "ODE Test"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      DrawingFrame plottingFrame = new DrawingFrame("ODE Test", plottingPanel); //$NON-NLS-1$
      plottingFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
      plottingPanel.addDrawable(solutionStripChart); // This one is drawn first
      plottingPanel.addDrawable(stripChart);

      plottingPanel.render();
      plottingFrame.setLocation(0, 0);
      plottingFrame.setSize(700,700);
      plottingFrame.setVisible(true);
    }

  }

  static private double solution(double time) {
    return Math.sin(time)*Math.exp(-time);
  }

  static class DDEExponential implements DelayDifferentialEquation {
    static private double sAlpha = Math.exp(-Math.PI/2);

    private double[] mState = {0, 0};
    private StateHistory mHistory;

    // Implementation of ODE

    public double[] getState() { return mState; }

    public void getRate(double[] state, double[] rate){    
      rate[0] = - state[0] - sAlpha*mHistory.interpolate(state[1]-Math.PI/2, 0);
      rate[1] = 1; // time
    }

    // Implementation of DelayDifferentialEquation

    public void setStateHistory(StateHistory memory) { mHistory = memory; }

    public double getMaximumDelay() { return 1; }

    public double[] getDelays(double[] state) { return new double[] { 1 }; }

    public void getInitialCondition(double time, double state[]){
      state[0] = Math.sin(time)*Math.exp(-time);
      state[1] = time;
    }

    public double[] getInitialConditionDiscontinuities(){
      return null;
    }

  } // End of DelayDifferentialEquation

}
