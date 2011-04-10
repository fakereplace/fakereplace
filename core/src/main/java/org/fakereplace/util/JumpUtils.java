/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.util;

import javassist.bytecode.Bytecode;

/**
 * Utilities for writiting conditional statements in bytecode
 *
 * @author Stuart Douglas
 */
public class JumpUtils {
    /**
     * After writing the instruction that requires a branch offset (e.g. GOTO )
     * to the {@link Bytecode} call this method. This will write two zero bytes
     * to the stream. When you have reached the position in the bytecode that you
     * want the jump to end at call {@link JumpMarker#mark()}, this will update
     * the branch offset to point to the next bytecode instruction that is added
     *
     * @return The JumpMarker that is used to set the conditionals end point
     */
    public static JumpMarker addJumpInstruction(Bytecode code) {
        return new JumpMarkerImpl(code);
    }

    private static class JumpMarkerImpl implements JumpMarker {
        private final Bytecode code;
        private int position;

        public JumpMarkerImpl(Bytecode code) {
            this.code = code;
            this.position = code.currentPc() - 1;
            code.addIndex(0);
        }

        public void mark() {
            code.write16bit(position + 1, code.currentPc() - position);
        }
    }
}
