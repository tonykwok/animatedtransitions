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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;
import org.jdesktop.animation.transitions.ComponentState;
import org.jdesktop.animation.transitions.Effect;

/**
 * Effect that moves a component from its position in the start
 * state to its position in the end state, based on linear interpolation
 * between the two points during the time of the animated transition.
 * 
 * The class extends ComponentImageEffect to use a simple image copy
 * for rendering the component rather than re-rendering the actual
 * component during each frame.
 * 
 * 
 * @author Chet Haase
 */
public class Move extends Effect {
    
    private PropertySetter ps;
    
    public Move() {}
    
    /**
     * REMIND: docs
     */
    public Move(ComponentState start, ComponentState end) {
	setComponentStates(start, end);
    }
    
    public void init(Animator animator, Effect parentEffect) {
        Effect targetEffect = (parentEffect == null) ? this : parentEffect;
        ps = new PropertySetter(targetEffect, "location", 
                new Point(getStart().getX(), getStart().getY()),
                new Point(getEnd().getX(), getEnd().getY()));
        animator.addTarget(ps);
        super.init(animator, null);
    }
    
    @Override
    public void cleanup(Animator animator) {
        animator.removeTarget(ps);
    }
}
