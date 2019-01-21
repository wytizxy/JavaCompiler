//name: Xiyuan Zheng assignment number: p5 date due: Nov. 5th

package cop5556fa18;

import java.util.*;
import cop5556fa18.PLPAST.*;

public class PLPSymbolTable {
	
	public class tableNode {
		Declaration dec;
		int scope;						//Scope of single node.
		public tableNode(Declaration dec, int scope) {
			this.dec = dec;
			this.scope = scope;
		}
	}
	
	private int nextScope = 0;
	private int currentScope = 0;			//Scope in symbolTable.
	private Stack<Integer> scopeStack = new Stack<Integer>();
	
	private Map<String, ArrayList<tableNode>> table = new HashMap<String, ArrayList<tableNode>>();
	
	public void insert(String name, Declaration declaration) {
		tableNode node = new tableNode(declaration, currentScope);
		if(table.get(name) == null) {
			ArrayList<tableNode> temp = new ArrayList<tableNode>();
			table.put(name, temp);
		}
		table.get(name).add(node);		
	}
	
	public void enterScope() {
		currentScope = nextScope++;
		scopeStack.push(currentScope);
	}
	
	public void leaveScope() {
		scopeStack.pop();
		if (!scopeStack.isEmpty())
			currentScope = scopeStack.peek();
	}
	
	public boolean existInScope(String name) {
		if(table.get(name) == null) {
			return false;
		}
		for (int i = 0; i < table.get(name).size(); i++) {
			if(table.get(name).get(i).scope == currentScope)		//currentScope or scopeStack.peek?
				return true;
		}
		return false;
	}
	
	public Declaration lookup(String name) {
		if(table.get(name) == null) {
			return null;
		}
		for (int i = table.get(name).size() - 1; i >= 0; i--) {
			for (int t = scopeStack.size() - 1; t >= 0; t--) {
				if(table.get(name).get(i).scope == scopeStack.get(t)) {
					return table.get(name).get(i).dec;
				}
			}
		}
		return null;
	}
	
}
