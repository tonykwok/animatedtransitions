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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import org.jdesktop.animation.timing.Animator;

import org.jdesktop.animation.transitions.Effect;
import org.jdesktop.animation.transitions.ComponentState;
import org.jdesktop.animation.transitions.effects.CompositeEffect;
import org.jdesktop.animation.transitions.EffectsManager;
import org.jdesktop.animation.transitions.effects.FadeIn;
import org.jdesktop.animation.transitions.effects.FadeOut;
import org.jdesktop.animation.transitions.effects.Move;
import org.jdesktop.animation.transitions.effects.Scale;
import org.jdesktop.animation.transitions.effects.Unchanging;

/**
 * This class holds the start and/or end states for a JComponent.  It also
 * determine (at <code>init()</code> time) the Effect to use during the
 * upcoming transition and calls the appropriate Effect during the
 * <code>paint()</code> method to cause the correct rendering of the
 * component during the transition.
 *
 * @author Chet Haase
 */
class AnimationState {
    
    /**
     * The component for this AnimationState; there is one component per
     * state, with either a start, an end, or both states.
     */
    private JComponent component;
    
    /**
     * Start/end states for this AnimationState; these may be set to a non-null
     * value or not, depending on whether the component exists in the
     * respective screen of the transition.
     */
    private ComponentState start, end;
    
    /**
     * Effect used to transition between the start and end states for this
     * AnimationState.  This effect is set during the init() method just
     * prior to running the transition.
     */
    private Effect effect;
    
    AnimationState() {}
    
    AnimationState(ComponentState state, boolean isStart) {
        this.component = state.getComponent();
        if (isStart) {
            start = state;
        } else {
            end = state;
        }
    }
    
    /**
     * Constructs a new AnimationState with either the start
     * or end state for the component.
     */
    AnimationState(JComponent component, boolean isStart) {
        this.component = component;
        ComponentState compState = new ComponentState(component);
        if (isStart) {
            start = compState;
        } else {
            end = compState;
        }
    }
    
    void setStart(ComponentState compState) {
        start = compState;
    }

    void setEnd(ComponentState compState) {
        end = compState;
    }
    
    ComponentState getStart() {
        return start;
    }
    
    ComponentState getEnd() {
        return end;
    }
    
    Component getComponent() {
        return component;
    }

    /**
     * Called just prior to running the transition.  This method examines the
     * start and end states as well as the Effect repository to 
     * determine the appropriate Effect to use during the transition for
     * this AnimationState.  If there is an existing custom effect defined
     * for the component for this type of transition, we will use that
     * effect, otherwise we will default to the appropriate effect (fading
     * in, fading out, or moving/resizing).
     */
    void init(Animator animator) {
        if (start == null) {
            effect = EffectsManager.getEffect(component,
                EffectsManager.TransitionType.APPEARING);
            if (effect == null) {
                effect = new FadeIn(end);
            } else {
                effect.setEnd(end);
            }
        } else if (end == null) {
            effect = EffectsManager.getEffect(component,
                EffectsManager.TransitionType.DISAPPEARING);
            if (effect == null) {
                effect = new FadeOut(start);
            } else {
                effect.setStart(start);
            }
        } else {
            effect = EffectsManager.getEffect(component,
                EffectsManager.TransitionType.CHANGING);
            if (effect == null) {
                // No custom effect; use move/scale combinations
                // as appropriate
                boolean move = false, scale= false;
                if (start.getX() != end.getX() || start.getY() != end.getY()) {
                    move = true;
                }
                if (start.getWidth() != end.getWidth() ||
                        start.getHeight() != end.getHeight()) {
                    scale = true;
                }
                if (move) {
                    if (scale) {
                        // move/scale
                        effect = new Move(start, end);
                        Effect scaleEffect = new Scale(start, end);
                        effect = new CompositeEffect(effect);
                        ((CompositeEffect)effect).addEffect(scaleEffect);
                    } else {
                        // just move
                        effect = new Move(start, end);
                    }
                } else {
                    if (scale) {
                        // just scale
                        effect = new Scale(start, end);
                    } else {
                        // Noop
                        effect = new Unchanging(start, end);
                    }
                }
            } else {
                // Custom effect; set it up for this transition
                effect.setStart(start);
                effect.setEnd(end);
            }
        }
        effect.init(animator, null);
    }
    
    void cleanup(Animator animator) {
        effect.cleanup(animator);
    }
    
    /**
     * Render this AnimationState into the given Graphics object with the
     * given elapsed fraction for the transition.  This is done by calling
     * into the effect to first set up the Graphics object
     * then to do the actual rendering using that Graphics object.
     */
    void paint(Graphics g) {
        if (effect != null) {
            Graphics2D g2d = (Graphics2D)g.create();
            effect.render(g2d);
            g2d.dispose();
        }
    }
}

