package simulator;

/**
 * Computer class comprises of memory, registers, cc and
 * can execute the instructions based on PC and IR 
 * @author mmuppa
 * @author Eugene Oh
 */

public class Computer {

	private final static int MAX_MEMORY = 50;
	private final static int MAX_REGISTERS = 8;
	private final static int CC_LENGTH = 3;

	private BitString mRegisters[];
	private BitString mMemory[];
	private BitString mPC;
	private BitString mIR;
	private BitString mCC;

	/**
	 * Initializes all the memory to 0, registers to 0 to 7
	 * PC, IR to 16 bit 0s and CC to 000 
	 * Represents the initial state 
	 */
	public Computer() {
		mPC = new BitString();
		mPC.setValue(0);
		mIR = new BitString();
		mIR.setValue(0);
		mCC = new BitString();
		mCC.setBits(new char[] { '0', '0', '0' });
		mRegisters = new BitString[MAX_REGISTERS];
		for (int i = 0; i < MAX_REGISTERS; i++) {
			mRegisters[i] = new BitString();
			mRegisters[i].setValue(i);
		}

		mMemory = new BitString[MAX_MEMORY];
		for (int i = 0; i < MAX_MEMORY; i++) {
			mMemory[i] = new BitString();
			mMemory[i].setValue(0);
		}
	}

	/**
	 * Loads a 16 bit word into memory at the given address. 
	 * @param address memory address
	 * @param word data or instruction or address to be loaded into memory
	 */
	public void loadWord(int address, BitString word) {
		if (address < 0 || address >= MAX_MEMORY) {
			throw new IllegalArgumentException("Invalid address");
		}
		mMemory[address] = word;
	}

	/**
	 * Performs NOT operation by using the data from the register based on bits[7:9] 
	 * and inverting and storing in the register based on bits[4:6]
	 */
	public void executeNot() {
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		mRegisters[destBS.getValue()] = mRegisters[sourceBS.getValue()].copy();
		mRegisters[destBS.getValue()].invert();
		// Sets the CC.
		mCC.setBits(setConditionCode(mRegisters[destBS.getValue()].getValue2sComp()));
	}
	
    /**
     * Performs the ADD operation by getting the integer value from two sources and stores the
     * result into the destination register. Also sets the condition code depending on what the
     * result was.
     */
	public void executeAdd() {
	    BitString destBS = mIR.substring(4, 3);
	    BitString sourceBS1 = mIR.substring(7, 3);
	    BitString mode = mIR.substring(10, 1);
	    int result = 0;
	    if (mode.getValue() == 0) {
	        // Handles register mode.
    	    BitString sourceBS2 = mIR.substring(13, 3);
    	    result = mRegisters[sourceBS1.getValue()].getValue2sComp() + 
    	             mRegisters[sourceBS2.getValue()].getValue2sComp();
	    } else {
	        // Handles immediate mode.
	        result = mRegisters[sourceBS1.getValue()].getValue2sComp() +
	                 mIR.substring(11, 5).getValue2sComp();
	    }
        mRegisters[destBS.getValue()].setValue2sComp(result);
        mCC.setBits(setConditionCode(result));	    
	}
	
    /**
     * Performs the AND operation by comparing two bitstring values and outputting a new bitstring
     * based on this comparison. It puts this result into a destination register.
     */
	public void executeAnd() {
        BitString destBS = mIR.substring(4, 3);
        BitString sourceBS1 = mIR.substring(7, 3);
        BitString sourceBS2 = new BitString();
        BitString mode = mIR.substring(10, 1);
        char[] result = new char[16];
        if (mode.getValue() == 0) {
            // Handles register mode.
            sourceBS2 = mRegisters[mIR.substring(13, 3).getValue()];
        } else {
            // Handles immediate mode.
            sourceBS2.setValue2sComp(mIR.substring(11, 5).getValue2sComp());        
        }          
        // Compares the two bitstrings and creates a new array from this comparison.
        for (int i = 0; i < 16; i++) {
            if (mRegisters[sourceBS1.getValue()].getBits()[i] == '1' && sourceBS2.getBits()[i] == '1') {
                result[i] = '1';
            } else {
                result[i] = '0';
            }
        }
        mRegisters[destBS.getValue()].setBits(result);
        mCC.setBits(setConditionCode(mRegisters[destBS.getValue()].getValue2sComp()));
	}
	
    /**
     * Performs the LOAD operation by retrieving the destination register and the memory address.
     * It thens retrieves the value from the memory address and places it into the register.
     */
	public void executeLoad() {
        BitString destBS = mIR.substring(4, 3);
        int offset = mIR.substring(7, 9).getValue2sComp();
        if (mPC.getValue2sComp() + offset < 0 || mPC.getValue2sComp() + offset > 49) {
            throw new IllegalArgumentException("The offset is out of bounds.");
        }
        BitString data = mMemory[mPC.getValue2sComp() + offset];
        mRegisters[destBS.getValue()] = data.copy();
        mCC.setBits(setConditionCode(data.getValue2sComp()));
	}
		
    /**
     * Performs the BR operation by checking to see if there is a match between the BR and the CC.
     * If so, then IR will be updated to the next set of instructions offset by a specified
     * amount in the last instruction given.
     */
	public void executeBranch() {
	    BitString nzp = mIR.substring(4, 3);
	    char[] nzpBits  = nzp.getBits();
	    char[] conditionCodeBits = mCC.getBits();
	    boolean check = false;
	    for (int i = 0; i <= 2; i++) {
	        if (nzpBits[i] == '1' && conditionCodeBits[i] == '1') {
	            check = true;
	        }
	    }
	    // Skips lines of instructions if the BR and CC match.
	    if (check) {
	        BitString lineSkip = mIR.substring(7, 9);
	        int lineSkipValue = lineSkip.getValue2sComp();
	        // This is wrong.
	        mPC.setValue2sComp(mPC.getValue2sComp() + lineSkipValue);
	    }
	}
	
	/**
	 * Prints out the current ASCII value in R0.
	 */
	public void executeTrapOut() {
	    BitString text = mRegisters[0].substring(8, 8);
	    int textValue = text.getValue();
	    System.out.println((char) textValue);    
	}
	
	/**
     * Sets the condition code based on the given value.
     * @param theValue The integer to compare against.
     */
    private char[] setConditionCode(int theValue) {
        char[] conditionCode = new char[CC_LENGTH];
        if (theValue < 0) {
            conditionCode[0] = '1';
            conditionCode[1] = '0';
            conditionCode[2] = '0';
        } else if (theValue > 0) {
            conditionCode[0] = '0';
            conditionCode[1] = '0';
            conditionCode[2] = '1';
        } else {
            conditionCode[0] = '0';
            conditionCode[1] = '1';
            conditionCode[2] = '0';
        }
        return conditionCode;
    }
    
    /**
     * Gets the condition code.
     * @return The condition code.
     */
    public BitString getConditionCode() {
        return mCC;
    }
    
    /**
     * Gets the current array of registers.
     * @return The array of registers.
     */
    public BitString[] getRegisters() {
        return mRegisters;
    }

    /**
     * Converts the registers and memory back to their initial states. Used primarily for 
     * testing purposes.
     */
    public void reset() {
           mRegisters = new BitString[MAX_REGISTERS];
            for (int i = 0; i < MAX_REGISTERS; i++) {
                mRegisters[i] = new BitString();
                mRegisters[i].setValue(i);
            }

            mMemory = new BitString[MAX_MEMORY];
            for (int i = 0; i < MAX_MEMORY; i++) {
                mMemory[i] = new BitString();
                mMemory[i].setValue(0);
            }
    }
    
    /**
     * Returns the value at the given memory location.
     * @param theAddress The address containing the desired value.
     * @return A BitString representing the value at the specified address.
     */
    public BitString getMemory(int theAddress) {
        return mMemory[theAddress];
    }
    
    /**
     * Sets the decimal value at the given address to be the given decimal value.
     * @param theAddress The address the program will change.
     * @param theValue The decimal value that will be set at the specified address.
     */
    public void setMemory(int theAddress, int theValue) {
        BitString valueToBit = new BitString();
        valueToBit.setValue2sComp(theValue);
        mMemory[theAddress] = valueToBit;
    }
        
	/**
	 * This method will execute all the instructions starting at address 0 
	 * till HALT instruction is encountered. 
	 */
	public void execute() {
		BitString opCodeStr;
		int opCode;

		while (true) {
			// Fetch the instruction
			mIR = mMemory[mPC.getValue()];
			mPC.addOne();

			// Decode the instruction's first 4 bits 
			// to figure out the opcode
			opCodeStr = mIR.substring(0, 4);
			opCode = opCodeStr.getValue();

			// What instruction is this?
			if (opCode == 9) { // NOT
				executeNot();			
			} else if (opCode == 1) { // ADD
			    executeAdd();
			} else if (opCode == 5) { // AND
			    executeAnd();
			} else if (opCode == 0) { // BRANCH
			    executeBranch();
			} else if (opCode == 2) { // LD
			    executeLoad();
			} else if (opCode == 15) {
			    BitString trapCode = mIR.substring(10, 6);
			    String hex = Integer.toHexString(trapCode.getValue());
			    int parsedHex = (int) Long.parseLong(hex);
			    if (parsedHex == 25) { // HALT
			        break;
			    } else if (parsedHex == 21) { // OUT
	                executeTrapOut();			        
			    }
			}
		}
	}

	/**
	 * Displays the computer's state
	 */
	public void display() {
		System.out.print("\nPC ");
		mPC.display(true);
		System.out.print("   ");

		System.out.print("IR ");
		mPC.display(true);
		System.out.print("   ");

		System.out.print("CC ");
		mCC.display(true);
		System.out.println("   ");

		for (int i = 0; i < MAX_REGISTERS; i++) {
			System.out.printf("R%d ", i);
			mRegisters[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();

		for (int i = 0; i < MAX_MEMORY; i++) {
			System.out.printf("%3d ", i);
			mMemory[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();
	}
}
