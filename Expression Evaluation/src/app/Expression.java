package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	
    	for (int i = 0; i < expr.length(); i++) {
    		String getName = "";
    		while(i < expr.length() && Character.isLetter(expr.charAt(i))) {
    			getName += expr.charAt(i);
    			if(i == expr.length() - 1) {
    				break;
    			}else i++;
    			
    		}
    		
    		if(!getName.equals("")) {
    			if(expr.charAt(i) == '[') {
    				if(arrays.contains(new Array(getName))) {
    					continue;
    				}
    				arrays.add(new Array(getName));
        		}else if(!(expr.charAt(i)=='[')) {
        			if(vars.contains(new Variable(getName))) {
    					continue;
    				}
        			vars.add(new Variable(getName));
        		}
    		}
    	}
    	
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	expr = expr.replaceAll("\\s","");
    	if(expr.length() == 1 && Character.isDigit(expr.charAt(0))) {
    		return Float.parseFloat(expr);
    	}else if(expr.length() == 1 && Character.isLetter(expr.charAt(0))){
    		return (float) vars.get(0).value;
    	}
    	expr =  ReplaceWithVals(expr, vars,arrays);
    	expr = evaluate(expr);
    	return Float.parseFloat(expr);
    } 
    private static String ReplaceWithVals(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	String replacedExpr = "";	
    	for(int i = 0; i < expr.length(); i++) {
    		String VarName = "";
    		while(i < expr.length() && Character.isLetter(expr.charAt(i))) {
    			VarName += expr.charAt(i);
    			i++;
    		}
    		if(vars.size()==0) {
    			replacedExpr+=VarName;
    		}
    		for(int j = 0; j < vars.size(); j++) {
    			if(!VarName.equals("")&& vars.get(j).name.equals(VarName)) {
    				replacedExpr+= vars.get(j).value;
    				break;
    			}else if(!(vars.get(j).name.equals(VarName)) && i < expr.length() && expr.charAt(i) == '['){
    				replacedExpr+= VarName;
    				 break;
    			}    			
    			
    		}
    		if(i < expr.length() && !(Character.isLetter(expr.charAt(i)))) {
    			replacedExpr+= expr.charAt(i);
    		}
    	}
    	if(replacedExpr.contains("[")) {
    		int firstOccur =replacedExpr.indexOf("[");
    		int lastOccur =replacedExpr.indexOf("]");
    		String arrName = "";
    		String prev = "";
    		String post= "";
    		int indOfBrack = -1;
    		while(firstOccur < lastOccur) {
    			indOfBrack = firstOccur;
    			int occurIndex = replacedExpr.substring(firstOccur + 1).indexOf('[');
    			firstOccur = indOfBrack + 1 + occurIndex;
    			if(occurIndex == -1) {
    				break;
    			}
    		}
    		int i = indOfBrack - 1;
    		while(i > -1 && Character.isLetter(replacedExpr.charAt(i))) {
    			i--;
    		}
    		arrName = replacedExpr.substring(i+1,indOfBrack);
    		prev = replacedExpr.substring(0, i+1);
    		if(lastOccur < replacedExpr.length() - 1) {
    			post = replacedExpr.substring(lastOccur + 1);
    		}else {
    			post = "";
    		}
    		int BracketEval = (int)Double.parseDouble(evaluate(replacedExpr.substring(indOfBrack+1, lastOccur)));
    		String solvedVer = "";
    		
    		for(int o = 0; o < arrays.size() ; o++) {
    			if(arrays.get(o).name.equals(arrName)) {
    				solvedVer = "" + arrays.get(o).values[BracketEval];
    			}
    		}
    		return  ReplaceWithVals(prev+solvedVer+post, vars, arrays);
    	}
    	return replacedExpr;
    }
    private static String SolveParentheses(String s) {
    	int indexOfPar = s.indexOf('(');
    	if(indexOfPar == -1) {
    		return evaluate(s);
    	}else {
    		int closeIndex = s.indexOf(')');
    		int locInStr= -1;
    		String start = "";
    		String end = "";
    		String evalPart = "";
    		while(indexOfPar < closeIndex) {
    			locInStr = indexOfPar;
    			int a = s.substring(indexOfPar + 1).indexOf('(');
    			indexOfPar = a + s.substring(0,locInStr).length() + 1;
    			if(a == -1) {
    				break;
    			}
    			
    		}
    		start = s.substring(0,locInStr);
    		evalPart = evaluate(s.substring(locInStr+1, closeIndex));
    		if(closeIndex < s.length() - 1) {
    			end = s.substring(closeIndex + 1);
    		}else {
    			end = "";
    		}
    		return SolveParentheses(start +evalPart + end);
    	}
    }
   
    private static String evaluate(String expr1) {
    	if(expr1 == null || expr1.length() == 0) {
    		return " ";
    	}
    	if(!(expr1.contains("["))){
    		Stack<String> stack1 = new Stack<>();
        	if(!(expr1.contains("("))){
        		for(int i = 0; i < expr1.length(); i++) {
        			
        			if(expr1.charAt(i) == '*' || expr1.charAt(i) == '/') {
        				String current1 = "";
        				int o = i + 1;
        				boolean check = expr1.charAt(o) == '-';
        				while(o < expr1.length() && (Character.isDigit(expr1.charAt(o)) || expr1.charAt(o) == '.' || check)) {
        				current1 += expr1.charAt(o);
        					check = false;
        					o++;
        				}
        				
        				if(expr1.charAt(i) == '*') {
        					float second = Float.parseFloat(stack1.pop());
        					stack1.push(String.valueOf(Float.parseFloat(current1)*second));
        				}else {
        				
        					stack1.push(String.valueOf(Float.parseFloat(stack1.pop())/Float.parseFloat(current1)));
        				}
        				i = o - 1;
        			}else if(expr1.charAt(i) == '+' || expr1.charAt(i) == '-' && i != 0) {
        				stack1.push(Character.toString(expr1.charAt(i)));
        				String current2 = "";
        				int o = i + 1;
        				boolean check = expr1.charAt(o) == '-';
        				while(o < expr1.length() && (Character.isDigit(expr1.charAt(o)) || expr1.charAt(o) == '.' || check)) {
        					current2 += expr1.charAt(o);
        					check = false;
        					o++;
        				}
        				stack1.push(current2);
        				i = o - 1;
        			}else {
        				String current3 = "";
        				int o = i;
        				boolean check = expr1.charAt(0) == '-';
        				
        				while(o < expr1.length() && (Character.isDigit(expr1.charAt(o)) || expr1.charAt(o) == '.' || check)) {
        					current3 += expr1.charAt(o);
        					check = false;
        					o++;
        				}
        				stack1.push(current3);
        				i = o - 1;
        			}    			
        		}
        		Stack <String> invertedStack = new Stack<>();
        		while(!(stack1.isEmpty())) {
        			invertedStack.push(stack1.pop());
        		}
        		stack1 = invertedStack;
        		while(!(stack1.size() == 1)) {
        			
            		String pop2 = stack1.pop();
            		if(stack1.peek().equals("-")) {
            			stack1.pop();
            			String pop1 = stack1.pop();
            			if(pop1.charAt(0) == '-') {
            				
            				pop1 = pop1.substring(1);
            				
            				stack1.push(String.valueOf((Float.parseFloat(pop2) + Float.parseFloat(pop1))));
            				
            			}else {
            				
            				stack1.push( String.valueOf((Float.parseFloat(pop2) - Float.parseFloat(pop1))));
            			}
            		}else {
            			
            			stack1.pop();
            			stack1.push(String.valueOf((Float.parseFloat(stack1.pop()) + Float.parseFloat(pop2))));
            		}
            	}
            	return stack1.pop();
        	}else {
        		return SolveParentheses(expr1);
        	}
        	
    	}
    	return "";
    	
    }
}
