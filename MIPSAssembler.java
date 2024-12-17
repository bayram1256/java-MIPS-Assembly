import java.io.*;
import java.util.*;

public class MIPSAssembler {

    public static final Map<String, String> OPCODES = new HashMap<>();//opcode of all instructions

    static {
        OPCODES.put("ADD", "000000");
        OPCODES.put("SUB", "000000");
        OPCODES.put("AND", "000000");
        OPCODES.put("OR", "000000");
        OPCODES.put("SLL", "000000");
        OPCODES.put("SRL", "000000");
        OPCODES.put("SLLV", "000000");
        OPCODES.put("SRLV", "000000");
        OPCODES.put("ADDI", "001000");
        OPCODES.put("ANDI", "001100");
        OPCODES.put("LW", "100011");
        OPCODES.put("SW", "101011");
        OPCODES.put("BEQ", "000100");
        OPCODES.put("BNE", "000101");
        OPCODES.put("BLEZ", "000110");
        OPCODES.put("BGTZ", "000111");
        OPCODES.put("J", "000010");
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String fileName;
        fileName = scanner.nextLine();
        String outFileName = fileName.replace(".asm", ".obj");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
        int address = 0x00400000;
        Map<String, Integer> labels = new HashMap<>();
        String line;
        String labelName = "";
        String instruction = "";
        writer.write("Address        Code\n");

        while ((line = reader.readLine()) != null) {
            // Skip lines containing ".text"
            if (line.contains(".text")) {
                continue;
            }
            // Check for labels
            if (line.contains(":")) {
                // Extract label name
                String label = line.substring(0, line.indexOf(':')).trim();
                // Store label and its address
                labels.put(label, address);
            }
            // Increment address for next instruction
            address += 0x4;
        }
        address = 0x00400000;
        reader = new BufferedReader(new FileReader(fileName));
        line = reader.readLine();

        // Main loop to read instruction, convett it to machine code and write it to the output file
        while (line != null) {
            if(line.contains(".text") ) {
                line = reader.readLine();
                continue;
            }

            if (!line.trim().isEmpty()) { // Skip empty lines
                instruction = line.trim();
                String machineCode = convertToMachineCode(instruction, address, labels);
                writer.write(String.format("%08X %s\n", address, machineCode)); // Write address and machine code
                address += 0x4;
            }
            line = reader.readLine();
        }

        // Close the reader and writer
        reader.close();
        writer.close();
    }

    public static String convertToMachineCode(String instruction, int address, Map<String,Integer> labels) {
        String result = "";
        if (instruction.contains("addi")) {
            String[] registers = getRegistersForImmediate(instruction, labels);
            result = "001000" + registers[1] + registers[0] + registers[2];
        } else if (instruction.contains("andi")) {
            String[] registers = getRegistersForImmediate(instruction, labels);
            result = "001100" + registers[1] + registers[0] + registers[2];
        } else if (instruction.contains("add")) {
            String[] registers = getThreeRegisters(instruction);
            result = "000000" + registers[1] + registers[2] + registers[0] + "00000100000";
        } else if (instruction.contains("sub")) {
            String[] registers = getThreeRegisters(instruction);
            result = "000000" + registers[1] + registers[2] + registers[0] + "00000100010";
        } else if (instruction.contains("and")) {
            String[] registers = getThreeRegisters(instruction);
            result = "000000" + registers[1] + registers[2] + registers[0] + "00000100100";
        } else if (instruction.contains("or")) {
            String[] registers = getThreeRegisters(instruction);
            result = "000000" + registers[1] + registers[2] + registers[0] + "00000100101";
        } else if (instruction.contains("sllv")) {
            String[] registers = getThreeRegisters(instruction);
            result = "000000" + registers[1] + registers[2] + registers[0] + "00000000100";
        } else if (instruction.contains("srlv")) {
            String[] registers = getThreeRegisters(instruction);
            result = "000000" + registers[1] + registers[2] + registers[0] + "00000000110";
        } else if (instruction.contains("sll")) {
            String[] registers = getRegistersForImmediate(instruction, labels);
            result = "000000" + "00000" + registers[1] + registers[0] + convertTo5BitBinary(Integer.parseInt(registers[2])) + "000000";
        } else if (instruction.contains("srl")) {
            String[] registers = getRegistersForImmediate(instruction, labels);
            result = "000000" + "00000" + registers[1] + registers[0] + convertTo5BitBinary(Integer.parseInt(registers[2])) + "000010";
        } else if (instruction.contains("sw")) {
            String[] registers = new String[3];
            String tmp = "";
            for (int i = 0; i < instruction.length(); i++) {
                if(instruction.charAt(i) == '$') {
                    tmp += instruction.charAt(i+1);
                    if ((Character.isDigit(instruction.charAt(i + 2)))) {
                        tmp += instruction.charAt(i + 2);
                    }
                    if(registers[0] == null){
                        registers[0] = tmp;
                        tmp = "";
                    }
                    else{
                        registers[2] = tmp;
                        tmp = "";
                    }
                }
                else if (instruction.charAt(i) == ',') {
                    int j = i+1;
                    while (Character.isDigit(instruction.charAt(j))){
                        tmp += instruction.charAt(j);
                        j++;
                    }
                    registers[1] = tmp;
                    tmp = "";
                }
            }
            result = "101011" + padRegisterNumber(registers[0]) + padRegisterNumber(registers[1]) + padImmediateValue(registers[2]);
        }
        else if (instruction.contains("lw")) {
            String[] registers = new String[3];
            String tmp = "";
            for (int i = 0; i < instruction.length(); i++) {
                if(instruction.charAt(i) == '$') {
                    tmp += instruction.charAt(i+1);
                    if ((Character.isDigit(instruction.charAt(i + 2)))) {
                        tmp += instruction.charAt(i + 2);
                    }
                    if(registers[0] == null){
                        registers[0] = tmp;
                        tmp = "";
                    }
                    else{
                        registers[2] = tmp;
                        tmp = "";
                    }
                }
                else if (instruction.charAt(i) == ',') {
                    int j = i+1;
                    while (Character.isDigit(instruction.charAt(j))){
                        tmp += instruction.charAt(j);
                        j++;
                    }
                    registers[1] = tmp;
                    tmp = "";
                }
            }
            result = "100011" + padRegisterNumber(registers[0]) + padRegisterNumber(registers[1]) + padImmediateValue(registers[2]);
        }
        else if (instruction.contains("j")) {
            String label = instruction.split("\\s")[1]; // Get the label from the instruction
            int labelAddress = labels.get(label); // address of the label
            int immediate = labelAddress >>> 2; //shift right by 2 bits to get 26 bit address
            String immediateStr = Integer.toBinaryString(immediate);

            // pad until line becomes 26 bits long with leading zeros, if necessary
            while (immediateStr.length() < 26) {
                immediateStr = "0" + immediateStr;
            }

            result = "000010" + immediateStr;
        }
        else if (instruction.contains("beq")) {
            String[] parts = new String[3];
            int regCount = 0;
            String tmp = "";
            for (int i = 0; i < instruction.length(); i++) {
                if(instruction.charAt(i) == '$'){
                    tmp += instruction.charAt(i+1);
                    if(Character.isDigit(instruction.charAt(i+2))){
                        tmp+= instruction.charAt(i+2);
                    }
                    if(regCount == 0){
                        parts[0] = tmp;
                        tmp = "";
                        regCount++;
                    }
                    else if (regCount == 1) {
                        parts[1] = tmp;
                        tmp = "";
                        regCount++;
                    }
                }
                else if (instruction.charAt(i) == ',' && regCount == 2) {
                    i++;
                    tmp = "";
                    while(i < instruction.length()){
                        tmp += instruction.charAt(i);
                        i++;
                    }
                    parts[2] = Integer.toString(labels.get(tmp) - 0x4);
                }
            }
            parts = getRegistersForBranch(parts);
            result = "000100" + parts[0] + parts[1] + parts[2];
        }
        else if (instruction.contains("bne")) {
            String[] parts = new String[3];
            int regCount = 0;
            String tmp = "";
            for (int i = 0; i < instruction.length(); i++) {
                if(instruction.charAt(i) == '$'){
                    tmp += instruction.charAt(i+1);
                    if(Character.isDigit(instruction.charAt(i+2))){
                        tmp+= instruction.charAt(i+2);
                    }

                    if(regCount == 0){
                        parts[0] = tmp;
                        tmp = "";
                        regCount++;
                    }
                    else if (regCount == 1) {
                        parts[1] = tmp;
                        tmp = "";
                        regCount++;
                    }
                }
                else if (instruction.charAt(i) == ',' && regCount == 2) {
                    i++;
                    tmp = "";
                    while(i < instruction.length()){
                        tmp += instruction.charAt(i);
                        i++;
                    }
                    parts[2] = Integer.toString(labels.get(tmp) - 0x4);
                }
            }
            parts = getRegistersForBranch(parts);
            result = "000101" + parts[0] + parts[1] + parts[2];
        }
        else if (instruction.contains("blez")) {
            String[] parts = new String[2];
            String tmp = "";
            for (int i = 0; i < instruction.length(); i++) {
                if(instruction.charAt(i) == '$'){
                    tmp += instruction.charAt(i+1);
                    if(Character.isDigit(instruction.charAt(i+2))){
                        tmp+= instruction.charAt(i+2);
                    }
                    parts[0] = tmp;
                    tmp = "";
                }
                else if (instruction.charAt(i) == ',') {
                    i++;
                    tmp = "";
                    while(i < instruction.length()){
                        tmp += instruction.charAt(i);
                        i++;
                    }
                    parts[1] = Integer.toString(labels.get(tmp));
                }
            }
            parts[0] = padRegisterNumber(parts[0]);
            result = "000110" + parts[0] + "00000" + calculateAddress(parts[1], address);
        }
        else if (instruction.contains("bgtz")) {
            String[] parts = new String[3];
            String tmp = "";
            for (int i = 0; i < instruction.length(); i++) {
                if(instruction.charAt(i) == '$'){
                    tmp += instruction.charAt(i+1);
                    if(Character.isDigit(instruction.charAt(i+2))){
                        tmp+= instruction.charAt(i+2);
                    }
                    parts[0] = tmp;
                    tmp = "";
                }
                else if (instruction.charAt(i) == ',') {
                    i++;
                    tmp = "";
                    while(i < instruction.length()){
                        tmp += instruction.charAt(i);
                        i++;
                    }
                    parts[1] = Integer.toString(labels.get(tmp));
                }
            }
            parts[0] = padRegisterNumber(parts[0]);
            result = "000111" + parts[0] + "00000" + calculateAddress(parts[1], address);
        }
        long decimal = Long.parseLong(result, 2);
        result = Long.toHexString(decimal);
        while (result.length() < 8) {
            result = "0" + result;
        }
        return result;
    }//done

    public static String[] getThreeRegisters(String instruction) {
        String[] results = new String[3]; // for three registers

        String rd = "";
        String rs = "";
        String rt = "";

        // Extract register numbers from the instruction string
        int startIndex = instruction.indexOf('$');
        int endIndex = instruction.indexOf(',', startIndex);
        rd = padRegisterNumber(instruction.substring(startIndex + 1, endIndex));

        startIndex = instruction.indexOf('$', endIndex);
        endIndex = instruction.indexOf(',', startIndex);
        rs = padRegisterNumber(instruction.substring(startIndex + 1, endIndex));

        rt = padRegisterNumber(instruction.substring(instruction.lastIndexOf('$') + 1));

        results[0] = rd;
        results[1] = rs;
        results[2] = rt;

        return results;
    } //done

    public static String[] getRegistersForBranch(String[] instruction) {

        String rs = "";
        String rt = "";
        String immediate = "";

        // Split the instruction by whitespace to extract the arguments

        // Extract register numbers from the argument parts
        rs = padRegisterNumber(instruction[0]); // Extracting rs
        rt = padRegisterNumber(instruction[1]); // Extracting rt
        immediate = padImmediateValue(instruction[2]);


        instruction[0] = rs;
        instruction[1] = rt;
        instruction[2] = immediate;

        return instruction;
    } //done

    public static String[] getRegistersForImmediate(String instruction, Map<String, Integer> labels) {
        String[] results = new String[3]; // for two registers and immediate value

        String rd = "";
        String rs = "";
        String immediate = "";

        // Split the instruction by commas to extract the arguments
        String[] args = instruction.split(",");

        // Extract register numbers from the argument parts
        rd = padRegisterNumber(args[0].substring(args[0].indexOf('$') + 1).trim());
        rs = padRegisterNumber(args[1].substring(args[1].indexOf('$') + 1).trim());

        // Extract immediate value

        immediate = padImmediateValue(args[2].trim());


        results[0] = rd;
        results[1] = rs;
        results[2] = immediate;

        return results;
    } //done

    public static String padRegisterNumber(String register) {
        int regNum = Integer.parseInt(register);
        String binary = Integer.toBinaryString(regNum);
        return String.format("%05d", Integer.parseInt(binary));
    } //done

    public static String padImmediateValue(String immediate) {
        int value = Integer.parseInt(immediate);
        if (value < 0) {
            value = (1 << 16) + value; //add 2^16 to the value if its negative
        }
        String binary = Integer.toBinaryString(value);
        //if necessary to ensure its 16 bits long pad with leading zeros
        while (binary.length() < 16) {
            binary = "0" + binary;
        }
        //if the binary string is longer than 16 bits, truncate the leading bits
        if (binary.length() > 16) {
            binary = binary.substring(binary.length() - 16);
        }
        return binary;
    }//done

    public static String convertTo5BitBinary(int num) {
        // Convert the integer to binary
        String binary = Integer.toString(num);
        // Pad with leading zeros to ensure it is 5 bits long
        while (binary.length() < 5) {
            binary = "0" + binary;
        }
        if(binary.length() > 5) {
            binary = binary.substring(binary.length() - 10);
        }
        return binary;
    } //done

    public static String calculateAddress(String curentAddress, int address){
        int current = Integer.parseInt(curentAddress);
        String result = Integer.toBinaryString((((current - address - 0x4)/4)));
        while (result.length() < 32) {
            result = "0" + result;
        }
        // Take the last 16 bits
        String last16Bits = result.substring(16);
        return last16Bits;
    } //done

}
