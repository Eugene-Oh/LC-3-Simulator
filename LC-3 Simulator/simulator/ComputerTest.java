package simulator;

/**
 * Tests for the Computer Class.
 * @author Eugene Oh
 */

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ComputerTest {
    
    private Computer comp;
    private BitString mRegisters[];

    @BeforeEach
    void setUp() throws Exception {
        comp = new Computer();
        mRegisters = comp.getRegisters();
    }

    /*
     * Tests the condition code of the computer class.
     */
    @Test
    void testSetConditionCode() {
        BitString notInstr = new BitString();
        
        // NOT R4
        notInstr.setBits("1001100101111111".toCharArray());
        comp.loadWord(0, notInstr);

        BitString haltInstr = new BitString();
        haltInstr.setBits("1111000000100101".toCharArray());
        comp.loadWord(1, haltInstr); 
        comp.execute();
        
        // Should be a negative condition code "100" (which is 4 as an integer).
        assertEquals(comp.getConditionCode().getValue(), 4, "setConditionCode() has failed!");
        comp.reset();
    }
    
    /*
     * Tests the ADD instruction's register mode.
     */
    @Test
    void testExecuteAddRegisterMode() {       
        BitString addInstr = new BitString();
        
        // R1 = R2 + R3
        addInstr.setBits("0001001010000011".toCharArray());
        comp.loadWord(0, addInstr);

        BitString haltInstr = new BitString();
        haltInstr.setBits("1111000000100101".toCharArray());
        comp.loadWord(1, haltInstr); 
        comp.execute();
        assertEquals(mRegisters[1].getValue(), 5, "ExecuteAdd() has failed!");
        comp.reset();
    }
    
    /*
     * Tests the ADD instruction's immediate mode mode.
     */
    @Test
    void testExecuteAddImmediateMode() {       
        BitString addInstr = new BitString();
        
        // R1 = R2 + 3
        addInstr.setBits("0001001010100011".toCharArray());
        comp.loadWord(0, addInstr);

        BitString haltInstr = new BitString();
        haltInstr.setBits("1111000000100101".toCharArray());
        comp.loadWord(1, haltInstr); 
        comp.execute();
        assertEquals(mRegisters[1].getValue(), 5, "ExecuteAdd() has failed!");
        comp.reset();
    }
    
    /*
     * Tests the AND instruction's register mode.
     */
    @Test
    void testExecuteAndRegisterMode() {       
        BitString andInstr = new BitString();

        // R2 AND R3 into R1
        andInstr.setBits("0101001010000011".toCharArray());
        comp.loadWord(0, andInstr);

        BitString haltInstr = new BitString();
        haltInstr.setBits("1111000000100101".toCharArray());
        comp.loadWord(1, haltInstr); 
        comp.execute();
        assertEquals(mRegisters[1].getValue(), 2, "ExecuteAdd() has failed!");
        comp.reset();
    }
    
    /*
     * Tests the AND instruction's immediate mode.
     */
    @Test
    void testExecuteAndImmediateMode() {       
        BitString andInstr = new BitString();

        // R2 AND R3 into R1
        andInstr.setBits("0101001010100011".toCharArray());
        comp.loadWord(0, andInstr);

        BitString haltInstr = new BitString();
        haltInstr.setBits("1111000000100101".toCharArray());
        comp.loadWord(1, haltInstr); 
        comp.execute();
        assertEquals(mRegisters[1].getValue(), 2, "ExecuteAdd() has failed!");
        comp.reset();
    }
    
    /*
     * Tests the setMemory method.
     */
    @Test
    void testSetAndGetMemory() {
        comp.setMemory(49, 4);
        assertEquals(comp.getMemory(49).getValue2sComp(), 4, "getMemory() or setMemory() has failed!");
        comp.reset();
    }
       
    /*
     * Tests the LD instruction.
     */
    @Test
    void testExecuteLoad() {
        BitString loadInstr = new BitString();
        loadInstr.setBits("0010001000000100".toCharArray());
        comp.loadWord(0, loadInstr);
        BitString data = new BitString();
        data.setValue2sComp(10);
        comp.loadWord(5, data);
        BitString haltInstr = new BitString();
        haltInstr.setBits("1111000000100101".toCharArray());
        comp.loadWord(1, haltInstr);
        comp.execute();
        assertEquals(mRegisters[1].getValue(), 10, "executeLoad() has failed!");
    }
    
    /*
     * Tests the BR instruction.
     */
    @Test
    void testExecuteBranch() {
        // R1 = R1 + 1
        BitString addInstr = new BitString();
        addInstr.setBits("0001001001100001".toCharArray());       
        // BRnzp
        BitString branchInstr = new BitString();
        branchInstr.setBits("0000111000000001".toCharArray());
        // HALT
        BitString haltInstr = new BitString();
        haltInstr.setBits("1111000000100101".toCharArray());
        
        // Should skip the next add instruction and go straight to halt.
        comp.loadWord(0, addInstr);     // R1 = R1 + 1
        comp.loadWord(1, branchInstr);  // BRnzp skip 1 line
        comp.loadWord(2, addInstr);     // R1 = R1 + 1
        comp.loadWord(3, haltInstr);    // HALT
        comp.execute();
        assertEquals(mRegisters[1].getValue(), 2, "executeBranch() has failed!");
        comp.reset();

    }
}






