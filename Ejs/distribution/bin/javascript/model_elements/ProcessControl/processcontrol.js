/**
 * @author Jesús Chacón <jeschaco@ucm.es>
 */

/**
 * A state-space model (A, B, C, D)
 * dx = Ax + Bu
 * y = Cx + Du
 */
class StateSpaceModel {
  constructor() {
  	this.A = [[0, 1],[0, 0]];
  	this.B = [[0], [1]];
   	this.C = [[0, 1]];
  	this.D = [[0]];
  }

  // Modify the system matrices
  setModel(A, B, C, D) {
  		this.A = A.slice();
  		this.B = B.slice();
  		this.C = C.slice();
  		this.D = D.slice();
  }

/*
 * Interface Continuous
 */

  // Returns the derivatives of the states of system as dx(t)=A*x(t)+B*u(t), for the given values of x and u.
  getRates(x, u) {
  	var nstates = this.getStateSize(),
  	    ninputs = this.getInputSize();
  	var dx = [];
  	for(var i=0; i<nstates; i++) {
  		dx[i] = 0;
  		for(var j=0; j<nstates; j++) {
    		dx[i] += this.A[i][j]*x[j];
  		}
  		for(var j=0; j<ninputs; j++) {
  		  dx[i] += this.B[i][j]*u[j];
  		}
  	}
    return dx;
  }

  getStateSize() {
    return (this.A != undefined) ? this.A.length : 0;
  }

  getInputSize() {
    return (this.B != undefined) ? this.B[0].length : 0;
  }

  getOutputSize() {
  	return (this.C != undefined) ? this.C.length : 0;
  }

  // Returns the output of the system as y(t)=C*x(t)+D*u(t), for the given values of x and u.
  getOutput(x, u) {
  	var nstates = this.getStateSize(),
  	    ninputs = this.getInputSize(),
  	    noutputs = this.getOutputSize();

  	var y = [];
  	for(var i=0; i<noutputs; i++) {
  		y[i] = 0;
  		for(var j=0; j<nstates; j++) {
  		  y[i] += this.C[i][j]*x[j];
 		  }
  		for(var j=0; j<ninputs; j++) {
  		  y[i] += this.D[i][j]*u[j];
  		}
  	}
  	return y;
  }
}

/**
 *	A continuous PID controller.
 *               ________
 *	setpoint ---|        |
 *	output   ---| PID(s) |---- control action
 *	tracking ---|________|
 */
class PIDController extends StateSpaceModel {
  constructor() {
    super();
    // default params (kp, ki, kd, n, ks)
    this.setParameters(1.0, 1.0, 1.0, 2.0, 1.0);
    // default range (uMin, uMax)
    this.setRange(0.0, 1.0);

    this.setpoint = 0.0;
    this.antiwindup = false;
    this.tracking = false;
  }

  // PID configuration
  setParameters(kp, ki, kd, n, ks) {
    //this.setN(n);
    //this.setKs(ks);
    this.setKp(kp);
    this.setKi(ki);
    this.setKd(kd);
	}

  setKs(ks) {
		this.ks = (ks > 0) ? ks : 0;
  }

  setN(n) {
		this.n = (n > 0) ? n : 0;
  }

  setKp(kp) {
 		this.kp = (kp > 0) ? kp : 0;
  }

  setKi(ki) {
		this.ki = (ki > 0) ? ki : 0;
  }

  setTi(ti) {
    this.setKi(this.kp/this.ti);
  }

  setKd(kd) {
		this.kd = (kd > 0) ? kd : 0;
		if(this.kd > 0) {
			this.setModel(
			  [[0, 0], [0, -this.n/this.kd]],
			  [[1, -1], [this.kd*this.n, -this.kd*this.n]],
			  [[this.ki, this.kd]],
			  [[this.kp + this.kd*this.n, -this.kp - this.kd*this.n]]);
		} else {
			this.setModel(
			  [[0, 0], [0, 0]],
			  [[1, -1], [0, 0]],
			  [[this.ki, 0]],
			  [[this.kp, -this.kp]]);
		}
  }

  setTd(td) {
    this.setKd(this.kp/this.td);
  }

  setAntiwindup(enabled) {
    this.antiwindup = enabled;
  }

  setTracking(enabled) {
    this.tracking = enabled;
  }

  setUMax(uMax) {
    this.uMax = uMax;
  }

  setUMin(uMin) {
    this.uMin = uMin;
  }

  setRange(uMin, uMax) {
  	this.uMin = uMin;
  	this.uMax = uMax;
  }

  // Compute the derivative of the state.
  getRates(x, u) {
    var dx = super.getRates(x, [u[0], u[1]]), y = super.getOutput(x, [u[0], u[1]]);

  	if(this.antiwindup) {
  		var v = this.coerce(y[0]);
  		dx[0] += this.ks*(v - y[0]);
  	}
  	if(this.tracking) {
  		if(u != undefined && u.length > 2) {
  		  dx[0] += this.ks*(u[2] - y[0]);
  		}
  	}
  	return dx;
  }

  coerce(u) {
  	return (u < this.uMin) ? this.uMin : (u > this.uMax) ? this.uMax : u;
  }
}

/**
 *	A state-feedback  controller.
 */
class StateFeedbackController {
  constructor() {
    this.setGains([1]);
  }

  setGains(K) {
		if(K == undefined) return;
		this.K = K;
	}

  getOutput(x, u) {
		var n = u.length;
		var y = 0;
		for(var i=0; i<n; i++) {
			y += this.K[i]*u[i];
		}
		return [y];
	}
}

/**
 *	A discrete PID controller with antiwindup and bumpless transfer.
 *               ________
 *	setpoint ---|        |
 *	output   ---| PID(s) |---- control action
 *	tracking ---|________|
 */
class PID extends StateSpaceModel {
  constructor() {
    super();
    // default params (kp, ki, kd, n, ks)
    this.setParameters(1.0, 1.0, 1.0, 2.0, 1.0);
    // default range (uMin, uMax)
    this.setRange(0.0, 1.0);

    this.setpoint = 0.0;
    this.e_prev = 0.0;
    this.I = 0.0;
    this.dt = 1;
    this.antiwindup = false;
    this.tracking = false;
  }

  // PID configuration
  setParameters(kp, ki, kd, n, ks) {
    //this.setN(n);
    //this.setKs(ks);
    this.setKp(kp);
    this.setKi(ki);
    this.setKd(kd);
	}

  setKp(kp) {
 		this.kp = (kp > 0) ? kp : 0;
  }

  setKi(ki) {
		this.ki = (ki > 0) ? ki : 0;
  }

  setTi(ti) {
    this.setKi(this.kp/this.ti);
  }

  setKd(kd) {
		this.kd = (kd > 0) ? kd : 0;
  }

  setTd(td) {
    this.setKd(1/this.td);
  }

  setAntiwindup(enabled) {
    this.antiwindup = enabled;
  }

  setTracking(enabled) {
    this.tracking = enabled;
  }

  setUMax(uMax) {
    this.uMax = uMax;
  }

  setUMin(uMin) {
    this.uMin = uMin;
  }

  setPeriod(dt) {
    this.dt = (dt > 0) ? dt : this.dt;
  }

  setRange(uMin, uMax) {
  	this.uMin = uMin;
  	this.uMax = uMax;
  }

  // Update the state of the controller
  // u is a vector that contains the state update:
  //  0 -> y is the
  //  1 -> sp is the setpoint
  //  2 -> u_sat is the actual control signal to implement the antiwindup in
  //       control mode, or the bumpless transfer in manual mode
  update(y, setpoint, u_ext) {
    this.y = y;
    this.sp = setpoint;
    this.u_ext = u_ext;

    var e = this.sp - y;
    var P = this.kp * e;
    var I_prev = this.I;
    var I = I_prev + this.ki * 0.5*(e + this.e_prev)*this.dt;
    var D = this.kd * (e - this.e_prev) / this.dt;
    var u = P + I + D;

    if(this.antiwindup) {
  		if(u_ext != undefined) {
        var du = (u - u_ext);
        if(e * du > 0) {
          I = u_ext - P - D;
        }
  		}
  	}

    if(this.tracking) {
      if(u_ext != undefined) {
        I = u_ext - P - D;
      }
  	}

    this.e_prev = e;
    this.I = I;
  	return u;
  }

  coerce(u) {
  	return (u < this.uMin) ? this.uMin : (u > this.uMax) ? this.uMax : u;
  }
}
