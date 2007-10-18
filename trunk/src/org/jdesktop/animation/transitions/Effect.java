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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import org.jdesktop.animation.timing.Animator;

/**
 * This is the base class for all effects that are used during 
 * screen transitions.
 *
 * Subclasses of this base class may override the <code>init()</code>, 
 * <code>setup()</code> and <code>paint()</code> methods to achieve the
 * desired effect.
 *
 * @author Chet Haase
 */
public abstract class Effect {

    /** Information about the start state used by this effect. */
    private ComponentState start;
    /** Information about the end state used by this effect. */
    private ComponentState end;
    /** Flag to indicate whether effect needs to re-render Component */
    private boolean renderComponent = false;
    /**
     * The image that will be used during the transition, for effects that
     * opt to not re-render the components directly.  The image will be
     * set when the start and end states are set.
     */
    private Image componentImage;
    /** Current x location. */
    private int x;
    /** Current y location. */
    private int y;
    /** Current width. */
    private int width;
    /** Current height. */
    private int height;
    
    private Rectangle bounds = new Rectangle();
    private Point location = new Point();

    public void setBounds(int x, int y, int width, int height) {
        this.bounds.x = this.location.x = this.x = x;
        this.bounds.y = this.location.y = this.y = y;
        setWidth(width);
        setHeight(height);
    }

    public void setBounds(Rectangle bounds) {
        setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    public void setLocation(Point location) {
        this.location.x = this.bounds.x = this.x = location.x;
        this.location.y = this.bounds.y = this.y = location.y;
    }
    
    public void setX(int x) {
        this.location.x = this.bounds.x = this.x = x;
    }
    
    public void setY(int y) {
        this.location.y = this.bounds.y = this.y = y;
    }
    
    public void setWidth(int width) {
        this.bounds.width = this.width = width;
    }
    
    public void setHeight(int height) {
        this.bounds.height = this.height = height;
    }
    
    protected JComponent getComponent() {
        if (start != null) {
            return start.getComponent();
        } else if (end != null) {
            return end.getComponent();
        }
        // Should not get here
        return null;
    }

    public void init(Animator animator, Effect parentEffect) {
        bounds = new Rectangle();
        if (start != null) {
            setBounds(start.getX(), start.getY(), start.getWidth(), start.getHeight());
        } else {
            setBounds(end.getX(), end.getY(), end.getWidth(), end.getHeight());
        }
        if (componentImage != null &&
                ((start != null && 
                 (start.getWidth() != componentImage.getWidth(null))) ||
                ((end != null && 
                 (end.getWidth() != componentImage.getWidth(null)))))) {
            componentImage.flush();
            componentImage = null;
        }
    }
    
    /**
     * Effect subclasses that create temporary objects for the transition
     * (such as in the <code>init()</code> method) should override this
     * method and clean up those resources. For example, TimingTarget
     * e.g., PropertySetter) objects added to Animator should be removed
     * after the transition to avoid leaking resources that may otherwise
     * be retained by those objects.
     */
    public void cleanup(Animator animator) {}
    
    public void setRenderComponent(boolean renderComponent) {
        this.renderComponent = renderComponent;
    }

    public boolean getRenderComponent() {
        return renderComponent;
    }

    /**
     * Sets both the start and end states of this Effect.
     */
    public void setComponentStates(ComponentState start, ComponentState end) {
	this.start = start;
	this.end = end;
    }
    
    /**
     * Sets the start state of this Effect.
     */
    public void setStart(ComponentState start) {
	this.start = start;
    }
    
    public ComponentState getStart() {
        return start;
    }
    
    /**
     * Sets the end state of this Effect.
     */
    public void setEnd(ComponentState end) {
	this.end = end;
    }

    public ComponentState getEnd() {
        return end;
    }
    
    public Image getComponentImage() {
        return componentImage;
    }
    
    protected void setComponentImage(Image componentImage) {
        this.componentImage = componentImage;
    }

    private void createComponentImage() {
        if (start != null && end == null) {
            componentImage = start.getSnapshot();
        } else if (start == null && end != null) {
            componentImage = end.getSnapshot();
        } else if (start.getWidth() != end.getWidth() || 
                start.getHeight() != end.getHeight()) {
            // This block grabs the targetImage
            // that best represents the component; the larger the better.
            float widthFraction = (float)end.getWidth() / start.getWidth();
            float heightFraction = (float)end.getHeight() / start.getHeight();
            if (Math.abs(widthFraction - 1.0f) > Math.abs(heightFraction - 1.0f)) {
                // difference greater in width
                if (widthFraction < 1.0f) {
                    // start size larger then end size
                    componentImage = start.getSnapshot();
                } else {
                    componentImage = end.getSnapshot();
                }
            } else {
                // different greater in height
                if (heightFraction < 1.0f) {
                    // start size larger than end size
                    componentImage = start.getSnapshot();
                } else {
                    componentImage = end.getSnapshot();
                }
            }
        } else {
            componentImage = start.getSnapshot();
        }
    }

    /**
     * This method is called during each frame of the transition animation,
     * prior to the call to <code>paint()</code>.
     * Subclasses will implement this method to set up the Graphic state,
     * or other related state, that will be used in the following call to
     * the <code>paint()</code> method.  Note that changes to the 
     * <code>Graphics2D</code> object here will still be present in the
     * <code>Graphics2D</code> object that is passed into the 
     * <code>paint()</code> method, so this is a good time to set up things
     * such as transform state.
     * @param g2d the Graphics2D destination for this rendering
     */
    public void setup(Graphics2D g2d) {
        if (!renderComponent && componentImage == null) {
            createComponentImage();
        }
    }
    
    /**
     * This method is called during each frame of the transition animation,
     * after the call to <code>setup()</code>. 
     * Subclasses may implement this method to perform whatever rendering
     * is necessary to paint the transitioning component into the 
     * <code>Graphics2D</code> object with the desired effect.
     * Most subclasses may elect to not override this at all, as the Effect
     * version of the method already handles the basic painting operation.
     * Only subclasses that need facilities beyond the basic drawing of
     * the component or an image representation of the component should
     * consider overriding.
     * @param g2d The Graphics2D destination for this rendering.  Note that
     * the state of this Graphics2D object is affected by the previous call
     * to <code>setup</code> so there may be no more need to perturb the 
     * graphics state further. Functionality in thihs method should focus,
     * instead, on the rendering details instead of the graphics state.
     */
    public void paint(Graphics2D g2d) {
        if (!renderComponent && (componentImage != null)) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(componentImage, 0, 0, width, height, null);
        } else {
            getComponent().setBounds(bounds);
            getComponent().validate();
            ComponentState.paintSingleBuffered(getComponent(), g2d);
        }
    }
    
    void render(Graphics2D g2d) {
        g2d.translate(location.x, location.y);
        setup(g2d);        
        paint(g2d);
    }
}
