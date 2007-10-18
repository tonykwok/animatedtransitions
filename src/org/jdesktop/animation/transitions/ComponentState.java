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

package org.jdesktop.animation.transitions;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.lang.reflect.Method;
import javax.swing.JComponent;

/**
 * The structure stores the state of a given transition component that
 * will be used during the transition, which includes the position, the
 * size, and the image snapshot of the component.
 *
 * @author Chet Haase
 */
public class ComponentState {
    
    /** The component. */
    private JComponent component;
    /** The x location of the component. */
    private int x;
    /** The y location of the component. */
    private int y;
    /** The width of the component. */
    private int width;
    /** The height of the component. */
    private int height;
    
    /**
     * The image snapshot of the component in this state; this may be used
     * later by effects which use images to render the transitioning
     * component.
     */
    private Image componentSnapshot;
    
    ComponentState() {}
    
    /**
     * Constructor, which takes the given component and derives the state
     * information needed (location, size, and image snapshot)
     */
    public ComponentState(JComponent component) {
        this.component = component;
	this.x = component.getX();
	this.y = component.getY();
	this.width = component.getWidth();
	this.height = component.getHeight();
        componentSnapshot = createSnapshot(component);
    }

    /**
     * Create an image snapshot of the component in its current state.  This
     * may be used in an Effect to render the transitioning component with
     * an image.
     */
    private Image createSnapshot(JComponent component) {
	GraphicsConfiguration gc = component.getGraphicsConfiguration();
        if (gc == null) { // component may have null gc, get default
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();
        }
        if (width != 0 && height != 0) {
            Image snapshot = gc.createCompatibleImage(width, height, 
                    component.isOpaque() ? Transparency.OPAQUE : 
                                           Transparency.TRANSLUCENT);
            Graphics2D gImg = (Graphics2D)snapshot.getGraphics();
            paintSingleBuffered(component, gImg);
            gImg.dispose();
            return snapshot;
        } else {
            // component with zero width or height cannot produce
            // an image, and one will not be needed anyway since it
            // is not visible in this state
            return null;
        }
    }
    
    public int getX() {
	return x;
    }
    
    public int getY() {
	return y;
    }
    
    public int getWidth() {
	return width;
    }
    
    public int getHeight() {
	return height;
    }
    
    public JComponent getComponent() {
        return component;
    }
    
    public Image getSnapshot() {
        if (componentSnapshot == null) {
            componentSnapshot = createSnapshot(component);
        }
        return componentSnapshot;
    }

    /**
     * The remaining methods exist solely support the static
     * paintSingleBuffered() method.
     */
    
    private static final Integer ANCESTOR_USING_BUFFER = 1;
    private static Method JCOMPONENT_SET_FLAG_METHOD;

    static {
        Method[] methods = JComponent.class.getDeclaredMethods();
        for (Method method : methods) {
            if ("setFlag".equals(method.getName())) {
                JCOMPONENT_SET_FLAG_METHOD = method;
                JCOMPONENT_SET_FLAG_METHOD.setAccessible(true);
                break;
            }
        }
    }

    private static void setSingleBuffered(JComponent component,
                                          boolean singleBuffered)
    {
        // This is a hack to trick Swing to paint directly to the passed in
        // Graphics instead of the double buffer first, then the Graphics.
        // The same thing can be done by turning off double buffering
        // at the RepaintManager level, but this might punt the bufferPerWindow
        // approach on Mustang, and we don't want that...
        try {
            JCOMPONENT_SET_FLAG_METHOD.invoke(component,
                                              ANCESTOR_USING_BUFFER,
                                              singleBuffered);
        } catch (Exception e) {
            System.err.println("error invoking: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Paints the given JComponent in single-buffered mode, which is
     * needed to avoid rendering artifacts when capturing a non-opaque Swing
     * component hierarchy into an offscreen image.
     *
     * @param component the JComponent (and its children) to be painted
     * @param g the Graphics into which component will be painted
     */
    public static void paintSingleBuffered(JComponent component,
                                                    Graphics g)
    {
        setSingleBuffered(component, true);
        component.paint(g);
        setSingleBuffered(component, false);
    }

    /**
     * This variation paints the component including whatever is behind it;
     * this handles the case where the component is not opaque.
     * This is useful/required for taking a snapshot of the transition container
     * background, for example.
     */
    public static void paintHierarchySingleBuffered(JComponent component,
                                                             Graphics g)
    {
        // Walk the parent hierarchy to get the topmost JComponent parent
        // Calculate the relative XY location of the original component as we go
        int x = 0, y = 0;
        int w = component.getWidth();
        int h = component.getHeight();
        JComponent topmost = component;
        JComponent prevTopmost = component;
        // We can stop when the current container is opaque or the 
        // top level JComponent (probably redundant checks; a non-opaque
        // contentPane would cause artifacts)
        while (!topmost.isOpaque() &&
                topmost.getParent() != null && 
                topmost.getParent() instanceof JComponent) {
            topmost = (JComponent)topmost.getParent();
            x += prevTopmost.getX();
            y += prevTopmost.getY();
            prevTopmost = topmost;
        }
        setSingleBuffered(topmost, true);
        // Only want to paint the area of the original component
        g.setClip(0, 0, w, h);
        g.translate(-x, -y);
        topmost.paint(g);
        setSingleBuffered(topmost, false);
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ComponentState) {
            ComponentState other = (ComponentState)obj;
            if (this.x == other.x && this.y == other.y &&
                    this.width == other.width && this.height == other.height &&
                    this.component == other.component) {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode() {
        int result = 17;
        result = 37 * result + x;
        result = 37 * result + y;
        result = 37 * result + width;
        result = 37 * result + height;
        result = 37 * result + component.hashCode();
        return result;
    }
    
    public String toString() {
        return "ComponentState: x, y, w, h, component = " +
                x + ", " + y + ", " + width + ", " + height + ", " + 
                component;
    }
}
