import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import java.util.List;
import java.util.ArrayList;

public class IOHandler{

    private int labelCount = 0;

    private String fileName;
    private Scanner scanner;

    private String currentInstruction;

    private CommandType commandType;
    private String arg1;
    private Integer arg2;

    private List<String> outputContent;

    public IOHandler(String fileName){
        this.fileName = fileName;

        try {
            File file = new File(this.fileName + ".vm");
            this.scanner = new Scanner(file);
        } catch (FileNotFoundException e){
            System.out.println(
                "The provided input file (" + fileName + ".vm) was not found."
            );
        }
        this.outputContent = new ArrayList<String>();

        readFile();
        writeToFile();
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

        if (this.commandType == CommandType.C_ARITHMETIC){
            writeArithmetic();
        } else if (
            this.commandType == CommandType.C_PUSH || this.commandType == CommandType.C_POP
            ) {
                writePushPop();
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
                arithmeticSubroutine("M=-M");
                break;
            case "not":
                arithmeticSubroutine("M=!M");
                break;

            case "eq":
                branchRoutine("D;JEQ");
                break;
            case "gt":
                branchRoutine("D;JGT");
                break;
            case "lt":
                branchRoutine("D;JLT");
                break;

            default:
                throw new RuntimeException(
                    "An unknown Arithmetic Command was passed " + this.arg1
                );
        }
    }

    private void arithmeticRoutine(String toWrite){
        pop();
        arithmeticSubroutine(toWrite);
    }

    private void arithmeticSubroutine(String toWrite){
        decremenet();
        set();
        this.outputContent.add(toWrite);
        increment();
    }

    private void branchRoutine(String toWrite){
        pop();
        decremenet();
        set();
        conditionalJump("GenHckLbl" + labelCount);
        this.outputContent.add(toWrite);
        endLabel();
        increment();
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

    private void conditionalJump(String label){
        this.outputContent.add("D=M-D");
        this.outputContent.add("@" + label);
    }

    private void endLabel(){
        set();
        setFalse();
        this.outputContent.add("@EndGenHckLbl " + labelCount);
        this.outputContent.add("0;JMP");
        this.outputContent.add("(GenHckLbl" + labelCount + ")");
        set();
        setTrue();
        this.outputContent.add("(EndGenHckLbl " + labelCount++ + ")");
    }

    private void setTrue(){
        this.outputContent.add("M=-1");
    }

    private void setFalse(){
        this.outputContent.add("M=0");
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
                        String[] fileParts = this.fileName.split("\\\\");
                        loadAddress(
                            fileParts[fileParts.length-1] + ".vm." + Integer.toString(index),
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
                        String[] fileParts = this.fileName.split("\\\\");
                        loadAddress(
                            fileParts[fileParts.length-1] + ".vm." + Integer.toString(index)
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

    public void loadAddress(String address, String set){
        this.outputContent.add("@" + address);
        this.outputContent.add(set);
    }

    public void loadAddress(String address){
        this.outputContent.add("@" + address);
    }

    public void writeToFile(){
        try {
            PrintWriter writer = new PrintWriter(fileName + ".asm");

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