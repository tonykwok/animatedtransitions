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

import java.awt.Graphics;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * This is the component where the transition animations actually run.
 * During a transition, this layer becomes visible in the TransitionPanel.
 * Regular repaint() events occur on the TransitionPanel, which trickle down
 * to paint() events here.  That method, in turn, calls paint() on
 * the AnimationManager to handle rendering the various elements of the
 * animation into the Graphics object.
 *
 * @author Chet Haase
 */
class AnimationLayer extends JComponent {
    
    private Point componentLocation = new Point();
    private final ScreenTransition screenTransition;
    
    public AnimationLayer(ScreenTransition screenTransition) {
        setOpaque(false);        
        this.screenTransition = screenTransition;
    }
    
    /**
     * Called from TransitionPanel to setup the correct location to
     * copy the animation to in the glass pane
     */
    public void setupBackground(JComponent targetComponent) {
        componentLocation.setLocation(0, 0);
        componentLocation =
            SwingUtilities.convertPoint(
                targetComponent, componentLocation,
                targetComponent.getRootPane().getGlassPane());
    }
    
    /**
     * Called during normal Swing repaint process on the TransitionPanel.
     * This simply copies the transitionImage from ScreenTransition into
     * the appropriate location in the glass pane.
     */
    @Override
    public void paintComponent(Graphics g) {
        g.translate(componentLocation.x, componentLocation.y);
        g.drawImage(screenTransition.getTransitionImage(), 0, 0, null);
    }
}
