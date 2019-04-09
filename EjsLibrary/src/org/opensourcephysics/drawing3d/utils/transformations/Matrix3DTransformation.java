/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils.transformations;

import org.opensourcephysics.drawing3d.Element;

/**
 * XAxisRotation implements a 3D rotation around the X axis.
 */
public class Matrix3DTransformation extends org.opensourcephysics.numerics.Matrix3DTransformation {

  protected Element mElement;

  public Matrix3DTransformation() {
    super(null);
  }

  /**
   * Sets the element the rotation applies to
   * @param element
   */
  public void setElement(Element element) {
    this.mElement = element;
  }
  
  @Override
  public double[] setOrigin(double[] origin) {
    if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    return super.setOrigin(origin);
  }

  @Override
  public void setOrigin(double ox, double oy, double oz) {
    if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
    super.setOrigin(ox,oy,oz);
  }
    
  public boolean setMatrix(double[][] newMatrix) {
    if (super.setMatrix(newMatrix)) {
      if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
      return true;
    }
    return false;
  }

  @Override
  public boolean setMatrix(double[] newMatrix) {
    if (super.setMatrix(newMatrix)) {
      if (mElement!=null) mElement.addChange(Element.CHANGE_TRANSFORMATION);
      return true;
    }
    return false;
  }
  
  @Override
  public Object clone() {
    Matrix3DTransformation m = new Matrix3DTransformation();
    m.origin = origin.clone();
    m.setMatrix(this.matrix);
    if (inverseMatrix==null) {
      return m;
    }
    m.inverseMatrix = new double[3][3]; // inverse exists so clone it too
    for(int i = 0; i<inverseMatrix.length; i++) { // loop over the rows
      System.arraycopy(inverseMatrix[i], 0, m.inverseMatrix[i], 0, inverseMatrix[i].length);
    }
    return m;
  }


}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
