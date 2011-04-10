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
