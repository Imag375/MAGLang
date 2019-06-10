# MAGLang
Грамматика языка MAGLang
lang -> expr*
expr -> do_while_expr | for_expr | if_expr | while_expr | PRINT_KW |
        (INT_KW? VAR  stmt) | 
        (VAR POINT FUNCTION L_RB stmt R_RB) |
        (LIST_KW | HESH_SET_KW POINT CREATE_KW L_RB VAR R_RB)
while_expr -> WHILE_KW cond_expr body
do_while_expr -> DO_KW body WHILE_KW cond_expr
for_expr -> FOR_KW L_RB INT_KW VAR ASSIGN_P VAR | NUMBER COLON VAR | NUMBER R_RB body
if_expr -> IF_KW cond_expr body (ELSE_KW body)?
body -> L_B expr R_B
cond_expr -> L_RB bool_expr R_RB
bool_expr -> comp_expr (BOOL_OP comp_expr)*
comp_expr -> (stmt COMP_OP stmt) | (NOT_KW L_RB comp_expr R_RB)
stmt -> value (OP value)*
value -> b_stmt | VAR | NUMBER | (VAR POINT (GET_KW L_RB stmt R_RB) | SIZE_KW)
b_stmt -> stmt | (L_RB stmt R_RB)

INT_KW -> ^"int"$
IF_KW -> ^"if"$
ELSE_KW -> ^"else"$
WHILE_KW -> ^"while"$
PRINT_KW -> ^"print"$
DO_KW -> ^"do"$
FOR_KW -> ^"for"$
NOT_KW -> ^"not"$
LIST_KW -> ^"List"$
HESH_SET_KW -> ^"HashSet"$
CREATE_KW -> ^"create"$
GET_KW -> ^"get"$
SIZE_KW -> ^"size"$
POINT -> ^"."$
FUNCTION -> ^"add" | "remove"$
VAR -> ^[a-z]+$
NUMBER -> ^0 | ([1-9][0-9]*)$
COLON -> ^":"$
ASSIGN_OP -> ^"="$
BOOL_OP -> ^"and" | "or"$
COMP_OP -> ^">" | "<" | ">=" | "<=" | "!=" | "=="$
OP -> ^"+" | "-" | "*" | "/"$
L_RB -> ^"("$
R_RB -> ^")"$
L_B -> ^"{"$
R_B -> ^"}"$
