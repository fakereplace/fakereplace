package org.fakereplace.util;

import javassist.bytecode.Bytecode;

/**
 * Interface that represents a conditional jump or goto in bytecode.
 * <p>
 * When the mark method is called the jump site is updated to point to the end
 * of the bytecode stream.
 * 
 * @see JumpUtils
 * 
 * 
 * @author Stuart Douglas
 * 
 */
public interface JumpMarker
{
   /**
    * Changes the jump instructions target to the next bytecode to be added to
    * the {@link Bytecode}
    */
   public void mark();
}
