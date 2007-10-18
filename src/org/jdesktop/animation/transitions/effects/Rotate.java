/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.animation.transitions.effects;

import java.awt.Graphics2D;
import javax.swing.JComponent;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.transitions.ComponentState;
import org.jdesktop.animation.transitions.Effect;

/**
 * This Effect rotates an image through a given number of degrees
 * during the animated transition.  It subclasses ComponentImageEffect to
 * use an image for redrawing the component instead of re-rendering
 * the component each time.
 * 
 * 
 * @author Chet Haase
 */
public class Rotate extends Effect {

    /** The x coordinate of the center location of the component. */
    private final int xCenter;
    /** The y coordinate of the center location of the component. */
    private final int yCenter;
    /** The total number of radians to sweep through during the transition. */
    private final double endRadians;
    
    private double radians;
    
    private PropertySetter ps;

        
    public void setRadians(double radians) {
        this.radians = radians;
    }

    /**
     * Construct a Rotate effect for a given component with the number of 
     * degrees you wish to rotate through during the transition.  This 
     * constructor will result in an effect that rotates around the center
     * of the component
     */
    public Rotate(int degrees, JComponent component) {
        this(degrees, component.getWidth() / 2, component.getHeight() / 2);
    }
    
    /**
     * Construct a Rotate effect for a given component with the number of 
     * degrees you wish to rotate through during the transition.  This 
     * constructor will result in an effect that rotates around the
     * point (xCenter, yCenter)
     */
    public Rotate(int degrees, int xCenter, int yCenter) {
        this.endRadians = Math.toRadians(degrees);
	this.xCenter = xCenter;
	this.yCenter = yCenter;
    }
    
    /** 
     * Construct a Rotate effect for a given component with the number
     * of degrees you wish to rotate through during the transition and the
     * center of rotation to use.
     */
    public Rotate(ComponentState start, ComponentState end,
                  int degrees, int xCenter, int yCenter)
    {
        this(degrees, xCenter, yCenter);
	setComponentStates(start, end);
    }

    public void init(Animator animator, Effect parentEffect) {
        ps = new PropertySetter(this, "radians", 0.0, endRadians);
        animator.addTarget(ps);
        super.init(animator, null);
    }        
    
    @Override
    public void cleanup(Animator animator) {
        animator.removeTarget(ps);
    }

    @Override
    public void setup(Graphics2D g2d) {
        // translate back and forth to rotate around the right point
	g2d.translate(xCenter, yCenter);
	g2d.rotate(radians);
	g2d.translate(-xCenter, -yCenter);
        super.setup(g2d);        
    }    
}
