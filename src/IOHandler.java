import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

public class IOHandler{

    private static int labelCount = 0;
    private int conditionCount = 0;

    private String fileName;
    private String simpleFileName;
    private Scanner scanner;

    private String currentInstruction;

    private CommandType commandType;
    private String arg1;
    private Integer arg2;

    private List<String> outputContent;

    public IOHandler(String fileName, boolean write){
        this.fileName = fileName;
        String[] fileParts = this.fileName.split("\\\\");
        this.simpleFileName = fileParts[fileParts.length-1];

        try {
            File file = new File(this.fileName + ".vm");
            this.scanner = new Scanner(file);
        } catch (FileNotFoundException e){
            System.out.println(
                "The provided input file (" + fileName + ".vm) was not found."
            );
        }
        this.outputContent = new ArrayList<String>();

        if(write){
            writeInit();
        }

        readFile();

        if (write){
            writeToFile();
        }
    }

    public IOHandler(String fileName, String sysFile){
        IOHandler sysCaller = new IOHandler(sysFile, false);
        IOHandler fileCaller = new IOHandler(fileName, true);

        fileCaller.outputContent = sysCaller.outputContent;
        fileCaller.writeToFile();

    }

    public IOHandler(String fileName, String secondFileName, String sysFile){
        IOHandler sysCaller = new IOHandler(sysFile, false);
        IOHandler fileCaller2 = new IOHandler(secondFileName, false);
        IOHandler fileCaller = new IOHandler(fileName, true);

        fileCaller.outputContent = fileCaller2.outputContent;
        fileCaller.writeToFile();
        fileCaller.outputContent = sysCaller.outputContent;
        fileCaller.writeToFile();

    }

    public boolean hasMoreCommands(){
        return this.scanner.hasNextLine();
    }

    public void advance(){
        this.currentInstruction = scanner.nextLine();
    }

    public void readFile(){

        // Write all the data from the input file to the contents list
        try {
            String curr = "";
            while (hasMoreCommands()){
                advance();
                curr = sanitizeCommand(currentInstruction);

                if (!shouldAddCommand(curr)){
                    continue;
                }

                this.outputContent.add("// " + curr);
                writeSingle(curr);
            }

            scanner.close();
        } catch (Exception e){ // An unexpected error
            System.out.println(
                "Something went wrong reading the input file (" + fileName + ".vm)."
            );
            e.printStackTrace();
        }
    }

    public static String sanitizeCommand(String command){
        return command.replaceAll("\\s", " ")
            .replaceAll("//.*", "");
    }

    public static boolean shouldAddCommand(String command){
        return !(
            command.startsWith("//") ||
            command.length() == 0
        );
    }

    public void writeSingle(String command){
        String[] deconstructed = command.split(" ");

        this.commandType = getCommandType(deconstructed[0]);
        this.arg1 = getArg1(deconstructed);
        this.arg2 = getArg2(deconstructed);

        switch (this.commandType){
            case C_ARITHMETIC:
                writeArithmetic();
                break;

            case C_PUSH:
            case C_POP:
                writePushPop();
                break;

            case C_LABEL:
                writeLabel(this.arg1);
                break;

            case C_GOTO:
                writeGoto(this.arg1);
                break;

            case C_IF:
                writeIf(this.arg1);
                break;

            case C_FUNCTION:
                writeFunction(this.arg1, this.arg2);
                break;

            case C_CALL:
                writeCall(this.arg1, this.arg2);
                break;

            case C_RETURN:
                writeReturn();
                break;

            default:
                throw new RuntimeException(
                    "Unknown Command Type " + this.commandType
                );
        }

    }

    private void writeArithmetic(){

        switch (this.arg1.toLowerCase()){
            case "add":
                arithmeticRoutine("M=M+D");
                break;
            case "sub":
                arithmeticRoutine("M=M-D");
                break;
             case "and":
                arithmeticRoutine("M=M&D");
                break;
            case "or":
                arithmeticRoutine("M=M|D");
                break;

            case "neg":
                this.outputContent.add("D=0");
                specArithmetic("M=D-M");
                break;
            case "not":
                specArithmetic("M=!M");
                break;

            case "eq":
                branchRoutine("D;JNE");
                break;
            case "gt":
                branchRoutine("D;JLE");
                break;
            case "lt":
                branchRoutine("D;JGE");
                break;

            default:
                throw new RuntimeException(
                    "An unknown Arithmetic Command was passed " + this.arg1
                );
        }
    }

    private void writePushPop(){
        String segment = this.arg1.toLowerCase();
        Integer index = this.arg2;

        switch (this.commandType){
            case C_PUSH:
                switch (segment){
                    case "constant":
                        loadAddress(Integer.toString(index), "D=A");
                        break;

                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                    case "temp":
                    case "pointer":
                        this.outputContent.add(getSegmentSymbol(segment, index));
                        this.outputContent.add("D=M");

                        if (segment.equals("temp") ||
                            segment.equals("pointer")){
                                break;
                        }

                        loadAddress(Integer.toString(index), "A=D+A");
                        this.outputContent.add("D=M");
                        break;

                    case "static":
                        loadAddress(
                            this.simpleFileName + ".vm." + Integer.toString(index),
                            "D=M"
                        );

                        break;

                    default:
                        throw new RuntimeException(
                            "An unknown Segment was passed " + segment
                        );
                }
                push();
                break;

            case C_POP:
                switch(segment){
                    case "constant":
                        loadAddress(Integer.toString(index));
                        break;

                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                    case "temp":
                    case "pointer":
                        this.outputContent.add(getSegmentSymbol(segment, index));

                        if (segment.equals("temp") ||
                        segment.equals("pointer")){
                            break;
                        }

                        this.outputContent.add("D=M");
                        loadAddress(Integer.toString(index), "A=D+A");
                        break;

                    case "static":
                        loadAddress(
                            this.simpleFileName + ".vm." + Integer.toString(index)
                        );
                        break;

                    default:
                        throw new RuntimeException(
                            "An unknown Segment was passed " + segment
                        );
                }

                this.outputContent.add("D=A");
                this.outputContent.add("@R13");
                this.outputContent.add("M=D");
                pop();
                this.outputContent.add("@R13");
                this.outputContent.add("A=M");
                this.outputContent.add("M=D");
                break;

            default:
                throw new RuntimeException(
                    "An unknown Push/Pop was passed " + this.commandType
                );

        }
    }

    private void writeInit(){
        this.outputContent.add("@256");
        this.outputContent.add("D=A");
        this.outputContent.add("@SP");
        this.outputContent.add("M=D");
        writeCall("Sys.init", 0);
    }

    private void writeLabel(String label){
        this.outputContent.add("(" + label + ")");
    }

    private void writeGoto(String label){
        this.outputContent.add("@" + label);
        this.outputContent.add("0;JMP");
    }

    private void writeIf(String label){
        pop();
        this.outputContent.add("A=A-1");
        this.outputContent.add("@" + label);
        this.outputContent.add("D;JNE");
    }

    private void writeCall(String functionName, int numArgs){
        String uniqueReturn = "GenHckRETLbl" + labelCount++;

        this.outputContent.add("@" + uniqueReturn);
        this.outputContent.add("D=A");
        push();

        setSegment("@LCL");
        setSegment("@ARG");
        setSegment("@THIS");
        setSegment("@THAT");

        this.outputContent.add("@SP");
        this.outputContent.add("D=M");
        this.outputContent.add("@5");
        this.outputContent.add("D=D-A");
        this.outputContent.add("@" + numArgs);
        this.outputContent.add("D=D-A");
        this.outputContent.add("@ARG");
        this.outputContent.add("M=D");
        this.outputContent.add("@SP");
        this.outputContent.add("D=M");
        this.outputContent.add("@LCL");
        this.outputContent.add("M=D");
        this.outputContent.add("@" + functionName);
        this.outputContent.add("0;JMP");
        this.outputContent.add("(" + uniqueReturn + ")");
    }

    private void writeReturn(){
        this.outputContent.add("@LCL");
        this.outputContent.add("D=M");
        this.outputContent.add("@R11");
        this.outputContent.add("M=D");

        this.outputContent.add("@5");
        this.outputContent.add("A=D-A");
        this.outputContent.add("D=M");
        this.outputContent.add("@R12");
        this.outputContent.add("M=D");

        this.outputContent.add("@ARG");
        this.outputContent.add("D=M");
        this.outputContent.add("@0");
        this.outputContent.add("D=D+A");
        this.outputContent.add("@R13");
        this.outputContent.add("M=D");
        this.outputContent.add("@SP");
        this.outputContent.add("AM=M-1");
        this.outputContent.add("D=M");
        this.outputContent.add("@R13");
        this.outputContent.add("A=M");
        this.outputContent.add("M=D");

        this.outputContent.add("@ARG");
        this.outputContent.add("D=M");
        this.outputContent.add("@SP");
        this.outputContent.add("M=D+1");

        String[] addresses = {"@THAT", "@THIS", "@ARG", "@LCL"};
        for(String address : addresses){
            saveSegment(address);
        }

        this.outputContent.add("@R12");
        this.outputContent.add("A=M");
        this.outputContent.add("0;JMP");
    }

    private void writeFunction(String functionName, int numLocals){
        this.outputContent.add("(" + functionName + ")");

        for(int i = 0; i < numLocals; i++){
            this.outputContent.add("@" + 0);
            this.outputContent.add("D=A");
            push();
        }
    }

    private void arithmeticRoutine(String toWrite){
        pop();
        this.outputContent.add("A=A-1");
        this.outputContent.add(toWrite);
    }

    private void specArithmetic(String toWrite){
        this.outputContent.add("@SP");
        this.outputContent.add("A=M-1");
        this.outputContent.add(toWrite);

    }

    private void branchRoutine(String toWrite){
        pop();
        this.outputContent.add("A=A-1");
        this.outputContent.add("D=M-D");
        this.outputContent.add("@FALSE" + conditionCount);
        this.outputContent.add(toWrite);
        this.outputContent.add("@SP");
        this.outputContent.add("A=M-1");
        setTrue();
        this.outputContent.add("@CONTINUE" + conditionCount);
        this.outputContent.add("0;JMP");
        this.outputContent.add("(FALSE" + conditionCount + ")");
        this.outputContent.add("@SP");
        this.outputContent.add("A=M-1");
        setFalse();
        this.outputContent.add("(CONTINUE" + conditionCount++ + ")");
    }

    private void pop(){
        decremenet();
        this.outputContent.add("A=M");
        this.outputContent.add("D=M");
    }

    private void push(){
        set();
        this.outputContent.add("M=D");
        increment();
    }

    private void decremenet(){
        this.outputContent.add("@SP");
        this.outputContent.add("M=M-1");
    }

    private void increment(){
        this.outputContent.add("@SP");
        this.outputContent.add("M=M+1");
    }

    private void set(){
        this.outputContent.add("@SP");
        this.outputContent.add("A=M");
    }

    private void setSegment(String segment){
        this.outputContent.add(segment);
        this.outputContent.add("D=M");
        push();
    }

    private void saveSegment(String segment){
        this.outputContent.add("@R11");
        this.outputContent.add("D=M-1");
        this.outputContent.add("AM=D");
        this.outputContent.add("D=M");
        this.outputContent.add(segment);
        this.outputContent.add("M=D");
    }

    private void setTrue(){
        this.outputContent.add("M=-1");
    }

    private void setFalse(){
        this.outputContent.add("M=0");
    }

    public void loadAddress(String address, String set){
        this.outputContent.add("@" + address);
        this.outputContent.add(set);
    }

    public void loadAddress(String address){
        this.outputContent.add("@" + address);
    }

    public void writeToFile(){
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(fileName + ".asm", true));

            // Write all the data from the contents list to the file
            for(String command : this.outputContent){
                writer.println(command);
            }

            writer.close();
        } catch (FileNotFoundException e){
            System.out.println(
                "The provided output file (" + fileName + ".asm) could not be created."
            );
        } catch (Exception e){ // An unexpected error
            System.out.println(
                "Something went wrong writing to the output file (" + fileName + ".asm)."
            );
            e.printStackTrace();
        }
    }

    public static CommandType getCommandType(String command){

        switch (command.toLowerCase()){
            case "add":
            case "sub":
            case "neg":
            case "eq":
            case "gt":
            case "lt":
            case "and":
            case "or":
            case "not":
                return CommandType.C_ARITHMETIC;

            case "push":
                return CommandType.C_PUSH;
            case "pop":
                return CommandType.C_POP;

            case "label":
                return CommandType.C_LABEL;
            case "goto":
                return CommandType.C_GOTO;
            case "if-goto":
                return CommandType.C_IF;

            case "function":
                return CommandType.C_FUNCTION;
            case "return":
                return CommandType.C_RETURN;
            case "call":
                return CommandType.C_CALL;

            default:
                throw new RuntimeException(
                    "An unknown Command Type was passed " + command
                );
        }

    }

    public static String getSegmentSymbol(String segment, int index){
        // index = 0;
        switch (segment.toLowerCase()){
            case "local":
                return "@LCL";
            case "argument":
                return "@ARG";
            case "this":
                return "@THIS";
            case "that":
                return "@THAT";
            case "constant":
                return "";
            case "static":
                return "";
            case "pointer":
                return "@R" + Integer.toString(3 + index);
            case "temp":
                return "@R" + Integer.toString(5 + index);
            default:
                throw new RuntimeException(
                    "An unknown Symbol was passed " + segment
                );
        }
    }

    public static String getSegmentSymbol(String segment){
        return getSegmentSymbol(segment, 0);
    }

    public String getArg1(String[] deconstructed){
        if (this.commandType == CommandType.C_RETURN){
            return null;
        }

        if (this.commandType == CommandType.C_ARITHMETIC){
            return deconstructed[0];
        }

        return deconstructed[1];
    }

    public Integer getArg2(String[] deconstructed){
        if (!(this.commandType == CommandType.C_PUSH ||
                this.commandType == CommandType.C_POP ||
                this.commandType == CommandType.C_FUNCTION ||
                this.commandType == CommandType.C_CALL)){
                    return null;
        }

        return Integer.parseInt(deconstructed[2]);
    }
}

enum CommandType {
    C_ARITHMETIC,
    C_PUSH, C_POP,
    C_LABEL, C_GOTO, C_IF,
    C_FUNCTION, C_RETURN, C_CALL
}