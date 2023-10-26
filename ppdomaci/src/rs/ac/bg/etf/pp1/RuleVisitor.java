package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;
import rs.ac.bg.etf.pp1.ast.*;

public class RuleVisitor extends VisitorAdaptor{

	int varDeclCount = 0;
	
	Logger log = Logger.getLogger(getClass());

	public void visit(VarDeclC VarDeclC){
		varDeclCount++;
		//log.info("prepoznata prom");
	}
	
	public void visit(AnotherVarDecl AnotherVarDecl){
		varDeclCount++;
		//log.info("prepoznata prom");
	}

}
