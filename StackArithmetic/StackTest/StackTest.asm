@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl0
D;JEQ
@SP
A=M
M=0
@EndGenHckLbl 0
0;JMP
(GenHckLbl0)
@SP
A=M
M=-1
(EndGenHckLbl 0)
@SP
M=M+1
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@16
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl1
D;JEQ
@SP
A=M
M=0
@EndGenHckLbl 1
0;JMP
(GenHckLbl1)
@SP
A=M
M=-1
(EndGenHckLbl 1)
@SP
M=M+1
@16
D=A
@SP
A=M
M=D
@SP
M=M+1
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl2
D;JEQ
@SP
A=M
M=0
@EndGenHckLbl 2
0;JMP
(GenHckLbl2)
@SP
A=M
M=-1
(EndGenHckLbl 2)
@SP
M=M+1
@892
D=A
@SP
A=M
M=D
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl3
D;JLT
@SP
A=M
M=0
@EndGenHckLbl 3
0;JMP
(GenHckLbl3)
@SP
A=M
M=-1
(EndGenHckLbl 3)
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@892
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl4
D;JLT
@SP
A=M
M=0
@EndGenHckLbl 4
0;JMP
(GenHckLbl4)
@SP
A=M
M=-1
(EndGenHckLbl 4)
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl5
D;JLT
@SP
A=M
M=0
@EndGenHckLbl 5
0;JMP
(GenHckLbl5)
@SP
A=M
M=-1
(EndGenHckLbl 5)
@SP
M=M+1
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl6
D;JGT
@SP
A=M
M=0
@EndGenHckLbl 6
0;JMP
(GenHckLbl6)
@SP
A=M
M=-1
(EndGenHckLbl 6)
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl7
D;JGT
@SP
A=M
M=0
@EndGenHckLbl 7
0;JMP
(GenHckLbl7)
@SP
A=M
M=-1
(EndGenHckLbl 7)
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
D=M-D
@GenHckLbl8
D;JGT
@SP
A=M
M=0
@EndGenHckLbl 8
0;JMP
(GenHckLbl8)
@SP
A=M
M=-1
(EndGenHckLbl 8)
@SP
M=M+1
@57
D=A
@SP
A=M
M=D
@SP
M=M+1
@31
D=A
@SP
A=M
M=D
@SP
M=M+1
@53
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
M=M+D
@SP
M=M+1
@112
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
M=M-D
@SP
M=M+1
@SP
M=M-1
@SP
A=M
M=-M
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
M=M&D
@SP
M=M+1
@82
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
M=M-1
A=M
D=M
@SP
M=M-1
@SP
A=M
M=M|D
@SP
M=M+1
@SP
M=M-1
@SP
A=M
M=!M
@SP
M=M+1
