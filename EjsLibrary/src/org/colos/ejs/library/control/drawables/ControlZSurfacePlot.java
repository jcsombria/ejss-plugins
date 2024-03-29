/*
 * The control.drawables package contains subclasses of
 * control.ControlElement that create Drawables2D for inclusion in
 * a DrawingPanel
 * Copyright (c) Jan 2004 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.drawables;

import org.opensourcephysics.display2d.Plot2D;

public class ControlZSurfacePlot extends ControlScalarField {

  @Override
  protected org.opensourcephysics.display.Drawable createDrawable () {
    plotType=Plot2D.SURFACE_PLOT;
    return super.doCreateTheDrawable();
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("colormode"))     return "PlotMode|int";
    return super.getPropertyInfo(_property);
  }

}
