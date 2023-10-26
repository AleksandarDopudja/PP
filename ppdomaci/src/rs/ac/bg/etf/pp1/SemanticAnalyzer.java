package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	Obj currentMethod = null;
	private Struct last_var_type = Tab.noType;
	private int last_const = 0;
	
	public int broj_promenljivih = 0;
	public int broj_konstanti = 0;
	public int broj_nizova = 0;
	public int broj_lokalnih_prom = 0;
	public int br_stm_main = 0;
	//public int br_metoda;
	
	private int line = 0;
	private String designator_name = "";
	public boolean err = false;
	
	private String des_type = "";
	
	private boolean main_ex = false;
	
	Logger log = Logger.getLogger(getClass());

	public boolean passed() {
		return !err;
	}
	
	public void report_error(String message, SyntaxNode info) {
		err = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
	
	public void report_info_ap(String message,String name,int kind, Struct type, int adr, int level, int lin) {
		StringBuilder msg = new StringBuilder(message); 
		
		msg.append("Pretraga na " + lin + "("+name+"), nadjeno ");
		
		String pom = "";
		switch (kind) {
		case Obj.Con:
			pom="Con ";
			break;
		case Obj.Var:
			pom="Var ";
			break;
		case Obj.Type:
			pom="Type ";
			break;
		case Obj.Meth:
			pom="Meth ";
			break;
		case Obj.Fld:
			pom="Fld ";
			break;
		case Obj.Prog:
			pom="Prog ";
			break;
		}
		
		msg.append(pom+""+name+": ");
		
		switch (type.getKind()) {
		case Struct.None:
			pom="notype";
			msg.append(pom);
			break;
		case Struct.Int:
			pom="int";
			msg.append(pom);
			break;
		case Struct.Char:
			pom="char";
			msg.append(pom);
			break;
		case Struct.Array:
			pom="Arr of ";
			msg.append(pom);
			switch (type.getElemType().getKind()) {
			case Struct.None:
				pom="notype";
				msg.append(pom);
				break;
			case Struct.Int:
				pom="int";
				msg.append(pom);
				break;
			case Struct.Char:
				pom="char";
				msg.append(pom);
				break;
			case Struct.Array:
				msg.append("Arr of ");
				switch (type.getElemType().getKind()) {
        		case Struct.None: msg.append("notype"); break;
        		case Struct.Int: msg.append("int"); break;
        		case Struct.Char: msg.append("char"); break;
        		case Struct.Array: msg.append("Arr of "); 
        		case Struct.Class: msg.append("Class"); break;
           }

				break;
			}
		}
		
		msg.append(", "+adr+", "+level);
		
		log.info(msg.toString());
	}
	
	public void visit(VarDC varDeclC){
		//varDeclCount++;
		//log.info("prepoznata prom");
		//log.info(""+varDeclC.getLine());
		//log.info(varDeclC.getType().struct.getKind());
		//log.info(varDeclC.getType().toString());
		
		Obj node = Tab.find(varDeclC.getVarName());
		
		if(node == Tab.noObj) {
			Obj varNode = Tab.insert(Obj.Var, varDeclC.getVarName(), varDeclC.getType().struct);
			if(varNode.getLevel()==0) {
				//report_info("Deklarisana globalna promenljiva "+ varDeclC.getVarName() + ", tip: "+ varDeclC.getType().struct.getKind(), varDeclC);
				broj_promenljivih++;
			}else if (varNode.getLevel()==1){
				//report_info("Deklarisana lokalna promenljiva "+ varDeclC.getVarName() + ", tip: "+ varDeclC.getType().struct.getKind(), varDeclC);
				broj_lokalnih_prom++;
			}
		}else{
			report_info("Greska na "+ varDeclC.getLine() + ": "+varDeclC.getVarName() + " vec deklarisano", null);
		}
	
	}
	
	public void visit(VarDArrayC varDArrayC) {
		Obj node = Tab.find(varDArrayC.getVarName());
		
		if(node == Tab.noObj) {

			Obj varNode = Tab.insert(Obj.Var, varDArrayC.getVarName(), new Struct(Struct.Array, varDArrayC.getType().struct));
			if(varNode.getLevel()==0) {
				//report_info("Deklarisana globalna promenljiva [niz,"+last_var_type.getKind()+"] : "+ varDArrayC.getVarName(), varDArrayC);
			}else if (varNode.getLevel()==1){
				//report_info("Deklarisana lokalna promenljiva [niz,"+last_var_type.getKind()+"] : "+ varDArrayC.getVarName(), varDArrayC);
			}
			broj_nizova++;
		}else{
			report_error("Greska na "+ varDArrayC.getLine() + ": "+varDArrayC.getVarName() + " vec deklarisano", null);
		}
	}
	
	public void visit(VarDTwoC anotherVarDecl){
		//varDeclCount++;
		//log.info("prepoznata prom");
		//log.info(""+anotherVarDecl.getLine());
		//log.info(last_var_type.getKind());
		
		Obj node = Tab.find(anotherVarDecl.getVarName());
		
		if(node == Tab.noObj) {
			Obj varNode = Tab.insert(Obj.Var, anotherVarDecl.getVarName(), last_var_type);
			if(varNode.getLevel()==0) {
				//report_info("Deklarisana globalna promenljiva "+ anotherVarDecl.getVarName() + ", tip: "+ last_var_type.getKind(), anotherVarDecl);
				broj_promenljivih++;
			}else if (varNode.getLevel()==1){
				//report_info("Deklarisana lokalna promenljiva "+ anotherVarDecl.getVarName() + ", tip: "+ last_var_type.getKind(), anotherVarDecl);
				broj_lokalnih_prom++;
			}
		}else {
			report_info("Greska na "+ anotherVarDecl.getLine() + ": "+anotherVarDecl.getVarName() + " vec deklarisano", null);
		}
	}
	
	public void visit(VarArrayDTwoC varDArrayC) {
		Obj node = Tab.find(varDArrayC.getVarName());
		
		if(node == Tab.noObj) {
			
			Obj varNode = Tab.insert(Obj.Var, varDArrayC.getVarName(), new Struct(Struct.Array, last_var_type));
			if(varNode.getLevel()==0) {
				//report_info("Deklarisana globalna promenljiva [niz,"+last_var_type.getKind()+"] : "+ varDArrayC.getVarName(), varDArrayC);
			}else if (varNode.getLevel()==1){
				//report_info("Deklarisana lokalna promenljiva [niz,"+last_var_type.getKind()+"] : "+ varDArrayC.getVarName(), varDArrayC);
			}
			broj_nizova++;
		}else{
			report_error("Greska na "+ varDArrayC.getLine() + ": "+varDArrayC.getVarName() + " vec deklarisano", null);
		}
	}
	
	public void visit(ProgName progName) {
		progName.obj = Tab.insert( Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
	}
	
	public void visit(Program program) {
		if(main_ex==false) report_error("Greska: program mora da sadrzi metod main", null);
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}
	
	public void visit(TypeC type){
		Obj typeNode = Tab.find(type.getTypeName());
		if(type.getTypeName().equals("bool")) {
			last_var_type = type.struct = new Struct(Struct.Bool);
			//last_type = "bool";
		}
		else if(typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola!", null);
			last_var_type = type.struct = Tab.noType;
		}else {
			if(Obj.Type == typeNode.getKind()) {
				last_var_type = type.struct = typeNode.getType();
				if(last_var_type.getKind()==1) {
					//last_type="int";
				}else if(last_var_type.getKind()==2) {
					//last_type="char";
				}
			}
			
			else {
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip", type);
				last_var_type = type.struct = Tab.noType;
			}
		}
	}
	
	public void visit(MethodType methodTypeName){
		currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMetName(), methodTypeName.getType().struct);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		//report_info("Obradjuje se funkcija " + methodTypeName.getMetName(), methodTypeName);
	}
	
	public void visit(MethodVoidType methodTypeName){
		if(methodTypeName.getMetName().equals("main"))main_ex=true;
		currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMetName(), Tab.noType);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		//report_info("Obradjuje se funkcija " + methodTypeName.getMetName(), methodTypeName);
	}
	
	public void visit(MethodeDeclC methodDecl) {
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		
		currentMethod = null;
	}
	
	public void visit(DesignatorC designator) {
		Obj obj = Tab.find(designator.getName());
		if(obj == Tab.noObj) {
			report_error("Greska na liniji: "+ designator.getLine() + " : ime " + designator.getName() + " nije deklarisano!", null);
			designator.obj = Tab.noObj;
		}else {
			designator.obj = obj;
			report_info_ap("", designator.getName(), designator.obj.getKind(), designator.obj.getType(), designator.obj.getAdr(), designator.obj.getLevel(), designator.getLine());
		}
		
		line = designator.getLine();
		//log.info("DesignatorC:"+obj.getName()+", tip: "+ obj.getType().getKind());
		//designator.obj = obj;
		designator_name = designator.getName();
		des_type="simple";
	}
	
	public void visit(ConstDeclC constDecl) {		
		Obj node = Tab.find(constDecl.getConstName());
		
		if(node == Tab.noObj) {
			Obj constNode = Tab.insert(Obj.Con, constDecl.getConstName(), constDecl.getType().struct);
			constNode.setAdr(last_const);
			//report_info("Deklarisana globalna konstanta "+ constDecl.getConstName() , constDecl);
			broj_konstanti++;
		}else {
			report_info("Greska na "+ constDecl.getLine() + ": "+constDecl.getConstName() + " vec deklarisano", null);
		}
	}
	
	public void visit(AnotherConstDecl constDecl) {
		Obj node = Tab.find(constDecl.getConstName());
		
		if(node == Tab.noObj) {
				Obj constNode = Tab.insert(Obj.Con, constDecl.getConstName(), last_var_type);
				constNode.setAdr(last_const);
				//report_info("Deklarisana globalna konstanta "+ constDecl.getConstName() , constDecl);
				broj_konstanti++;
		}else {
			report_info("Greska na "+ constDecl.getLine() + ": "+constDecl.getConstName() + " vec deklarisano", null);
		}
	}
	
	public void visit(NumVal val) {
		last_const = val.getN();
	}
	
	public void visit(CharVal val) {
		last_const = val.getC();
	}
	
	public void visit(BoolVal val) {
		//last_const = val.getB();
	}
	
	public void visit(DesignatorExpr designatorExpr) {
		designatorExpr.obj = designatorExpr.getIdentExprList().obj;
		//log.info("designatorExpr");
	}
	
	public void visit(DesignatorArrayC designatorArrayC) {
		//log.info("designatorArrayC");
		Obj id = designatorArrayC.getIdentExprListLsquare().obj;
		if (id.getType().getKind() != Struct.Array) {
              report_error("Greska u liniji " +designatorArrayC.getLine()+
                           ": Ocekivan niz",null);
              designatorArrayC.obj = Tab.noObj;
		}
		else {
			int tip = designatorArrayC.getExpr().struct.getKind();
			if(tip == Struct.Array ) {
				tip = designatorArrayC.getExpr().struct.getElemType().getKind();
			}
			if(tip != Struct.Int) {
				 report_error("Greska u liniji " +designatorArrayC.getLine()+
	                     ": Tip kod indeksiranja niza mora biti int",null);
			}
			designatorArrayC.obj = new Obj(Obj.Elem, "", id.getType().getElemType());
		}
		des_type = "array";
	}
	
	public void visit(IdentExprListLsquare identExprListLsquare) {
		//log.info("identExprListLsquare");
		identExprListLsquare.obj = identExprListLsquare.getIdentExprList().obj;
	}
	
	public void visit(FactorNumConst factorNumConst) {
		factorNumConst.struct = Tab.intType;
		//log.info("FactorNumConst:");
	}
	
	public void visit(FactorCharConst factorCharConst) {
		factorCharConst.struct = Tab.charType;
		//log.info("FactorCharConst:");
	}
	
	public void visit(FactorBoolConst factorBoolConst) {
		factorBoolConst.struct = new Struct(Struct.Bool);
		//log.info("FactorBoolConst:");
	}
	
	public void visit(FactorDes factorDes) {		
		factorDes.struct = factorDes.getDesignator().obj.getType();
		//log.info("FactorDes: KARA");
	}
	
	public void visit(FactorExpr factorExpr) {
		factorExpr.struct = factorExpr.getExpr().struct;
		//log.info("FactorExpr:");
	}
	
	public void visit(FactorNewActPars factorNewActPars) {
		int t = factorNewActPars.getExpr().struct.getKind();
		if(t == Struct.Array && des_type.equals("array")) {
			t = factorNewActPars.getExpr().struct.getElemType().getKind(); 
		}
		if(t != Struct.Int) {
			report_error("Greska na liniji "+ factorNewActPars.getLine() + 
						" : Tip u [] mora biti int.", null);
		}
		factorNewActPars.struct = factorNewActPars.getType().struct;
		//log.info("FactorNewActPars: "+factorNewActPars.getType().struct.getKind());
	}
	
	public void visit(ExprC exprC) {
		Struct e = exprC.getExpr().struct;
		Struct t = exprC.getTerm().struct;
		//log.info("AddopTermListC: "+(l==null)+","+(t==null));
		
		int t1 = e.getKind();
		int t2 = t.getKind();
		if(t1 == Struct.Array) {
			t1 = e.getElemType().getKind();
		}
		if(t2 == Struct.Array) {
			t2 = t.getElemType().getKind();
		}
		
		if(t1== Struct.None || t2== Struct.None) {
			exprC.struct = Tab.noType;
		}
		else if(t1!=t2) {
			report_error("Greska na "+ line + ": "+ " nekompatibilni tipovi u izrazu za sabiranje", null);
			exprC.struct = Tab.noType;
		}
		else if(t1!= Struct.Int || t2!= Struct.Int) {
			report_error("Greska na "+ exprC.getLine() + ": "+ " tipovi u izrazu moraju biti int kod sabiranja", null);
			exprC.struct = Tab.noType;
		}else {
			exprC.struct = Tab.intType;
		}
	}
	
	public void visit(SingleExprc singleExprc) {
		if(singleExprc.getTerm().struct.getKind()!=Struct.Int) {
			if(singleExprc.getMinusOp().getClass() == MinusOptional.class) {
				report_error("Greska na "+singleExprc.getLine()+": "+" '-' Term -> Term mora biti tipa int",null);		
				singleExprc.struct = Tab.noType;
			}
			else
				singleExprc.struct = singleExprc.getTerm().struct;
		}
		else {
			singleExprc.struct = singleExprc.getTerm().struct;
		}
		
		//log.info("NoAddopTermList: "+ singleExprc.struct.getKind());
	}
	
	public void visit(TermC termC) {
		
		Struct t = termC.getTerm().struct;
		Struct f = termC.getFactor().struct;
		
		int t1 = t.getKind();
		int t2 = f.getKind();
		if(t1 == Struct.Array) {
			t1 = t.getElemType().getKind();
		}
		if(t2 == Struct.Array) {
			t2 = f.getElemType().getKind();
		}
		
		if(t1== Struct.None || t2== Struct.None) {
			termC.struct = Tab.noType;
		}
		else if(t1!= Struct.Int || t2!= Struct.Int) {
			report_error("Greska na "+ termC.getLine() + ": "+ " tipovi u izrazu moraju biti int kod mnozenja", null);
			termC.struct = Tab.noType;
		}else {
			termC.struct = Tab.intType;
		}
	}
	
	public void visit(SingleTermC singleTermC) {
		singleTermC.struct = singleTermC.getFactor().struct;
		//log.info("SingleTermC: "+singleTermC.struct.getKind());
    }
	
	public void visit(DesignatorStatementExpr designatorStatementExpr) {
		
		//log.info("DesignatorStatementExpr: "+ (designatorStatementExpr.getExpr().struct.getElemType().getKind()));
		
		if(designatorStatementExpr.getDesignator().obj.getType().getKind() == Struct.None || designatorStatementExpr.getExpr().struct.getKind()== Struct.None) {
			
		}
		else if(designatorStatementExpr.getDesignator().obj.getType().getKind() == Struct.Array && (designatorStatementExpr.getDesignator().obj.getType().getElemType().getKind() == designatorStatementExpr.getExpr().struct.getKind())) {
			
		}
		else if(designatorStatementExpr.getDesignator().obj.getType().getKind() == Struct.Array && designatorStatementExpr.getExpr().struct.getKind() == Struct.Array && (designatorStatementExpr.getDesignator().obj.getType().getElemType().getKind() == designatorStatementExpr.getExpr().struct.getElemType().getKind())) {
			
		}
		else if(designatorStatementExpr.getDesignator().obj.getType().getKind() != designatorStatementExpr.getExpr().struct.getKind()) {
			report_error("Greska na "+ designatorStatementExpr.getLine() + ": "+ " ne kompatibilni tipovi ("+designatorStatementExpr.getDesignator().obj.getType().getKind()
					+","+designatorStatementExpr.getExpr().struct.getKind()+")", null);
		}
		
		//log.info("DesignatorStatementExpr: kraj");
	}
	
	public void visit(DesignatorStatementInc designatorStatementInc) {
		
		if(designatorStatementInc.getDesignator().obj == Tab.noObj) {
			//designatorStatement.obj
		}else if(designatorStatementInc.getDesignator().obj.getType().getKind() == Struct.Array && des_type.equals("array")) {
			if(designatorStatementInc.getDesignator().obj.getType().getElemType().getKind() != Struct.Int) {
				report_error("Greska na "+ designatorStatementInc.getLine() + ": "+designator_name+ " tip mora biti int za inkrement", null);
			}else {
				
			}
		}else if(designatorStatementInc.getDesignator().obj.getType().getKind() != Struct.Int) {
			report_error("Greska na "+ designatorStatementInc.getLine() + ": "+designator_name+ " tip mora biti int za ikrement", null);
		}
	}
	
	public void visit(DesignatorStatementDec designatorStatementDec) {
		
		if(designatorStatementDec.getDesignator().obj == Tab.noObj) {
			//designatorStatement.obj
		}else if(designatorStatementDec.getDesignator().obj.getType().getKind() == Struct.Array && des_type.equals("array")) {
			if(designatorStatementDec.getDesignator().obj.getType().getElemType().getKind() != Struct.Int) {
				report_error("Greska na "+ designatorStatementDec.getLine() + ": "+designator_name+ " tip mora biti int za dekrement", null);
			}else {
				
			}
		}else if(designatorStatementDec.getDesignator().obj.getType().getKind() != Struct.Int) {
			report_error("Greska na "+ designatorStatementDec.getLine() + ": "+designator_name+ " tip mora biti int za dekrement", null);
		}
	}
	
	public void visit(ReadStm readStm) {
		if(readStm.getDesignator().obj.getType().getKind() == Struct.Array && des_type.equals("array") && (readStm.getDesignator().obj.getType().getElemType().getKind()!=Struct.Int &&
				readStm.getDesignator().obj.getType().getElemType().getKind()!=Struct.Char)) {
			
			report_error("Greska na "+ readStm.getLine() + ": "+designator_name+ " , ne kompatibilan tip (ocekuje se int ili ch)", null);
			
		}else if (readStm.getDesignator().obj.getType().getKind() != Struct.Array && readStm.getDesignator().obj.getType().getKind()!=Struct.Int && readStm.getDesignator().obj.getType().getKind()!=Struct.Char){
			report_error("Greska na "+ readStm.getLine() + ": "+designator_name+ " , ne kompatibilan tip (ocekuje se int ili ch)", null);
		}
	}
	
	public void visit(PrintStm printStm) {
		if(printStm.getExpr().struct.getKind() == Struct.Array) {
			if(printStm.getExpr().struct.getElemType().getKind() == Struct.Int || printStm.getExpr().struct.getElemType().getKind() == Struct.Char) {
				
			}
			else {
				report_error("Greska na "+ printStm.getLine() + ": "+designator_name+ " , ne kompatibilan tip (ocekuje se int ili ch)", null);
			}
		}
		else if(printStm.getExpr().struct.getKind() == Struct.Int || printStm.getExpr().struct.getKind() == Struct.Char) {
			
		}else {
			report_error("Greska na "+ printStm.getLine() + ": "+designator_name+ " , ne kompatibilan tip (ocekuje se int ili ch)", null);
		}
	}
	
	public void visit(DesignatorOptC designatorOptC) {
		designatorOptC.obj = designatorOptC.getDesignator().obj;
	}
	
	public void visit(DesignatorOptListC designatorOptListC) {
		//designatorOptListC.obj = designatorOptListC.getDesignatorOpt().obj;
	}
	
	public void visit(NoDesignatorOpt noDesignatorOpt) {
		noDesignatorOpt.obj = Tab.noObj;
	}
	
	
}
