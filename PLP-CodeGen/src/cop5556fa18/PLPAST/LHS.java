package cop5556fa18.PLPAST;

import cop5556fa18.PLPTypes;
import cop5556fa18.PLPScanner.Token;

public class LHS extends PLPASTNode {
	
	public PLPTypes.Type type;
	public Declaration dec;
	public final String identifier;

	public LHS(Token firstToken, String name) {
		super(firstToken);
		this.identifier = name;
	}

	@Override
	public Object visit(PLPASTVisitor v, Object arg) throws Exception {
		return v.visitLHS(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LHS other = (LHS) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}
	
	

}
