package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Collection;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	private boolean newarr = false;
	private ArrayList<Obj> lista = new ArrayList<>();
	private ArrayList<Integer> indeksi = new ArrayList<>();
	private int ind = 0;
	private boolean desop = false;
	private boolean arr = false;
	
	public int num_vars(Collection<Obj> o) {
		int n = 0;
		for (Obj obj : o) {
			if(!obj.getName().equals("main")) {
				n++;
			}
		}
		return n;
	}
	
	public void visit(Program program) {
		Code.dataSize = num_vars(program.getProgName().obj.getLocalSymbols());
	}
	
	public void visit(MethodDeclFPC md) {
		
		if(md.getMethodTypeName().obj.getName().equals("main")) {
			Code.mainPc = Code.pc;
		}
		Obj o = md.getMethodTypeName().obj;
		o.setAdr(Code.pc);
		
		Code.put(Code.enter);
		Code.put(o.getLevel());
		Code.put(num_vars(o.getLocalSymbols()));
	}
	
	public void visit(MethodeDeclC md) {
		Code.put(Code.exit); 
		Code.put(Code.return_);
	}
	
	public void visit(FactorNumConst factorNumConst) {
		Obj ob = new Obj(Obj.Con,"",Tab.intType);
		ob.setAdr(factorNumConst.getN());
		if(desop==false) {
			Code.load(ob);
		}else {
			lista.add(ob);
			ind++;
		}
	}
	
	public void visit(FactorCharConst factorCharConst) {
		Obj ob = new Obj(Obj.Con,"",Tab.charType);
		ob.setAdr(factorCharConst.getC());
		Code.load(ob);
		
	}
	
	public void visit(FactorBoolConst factorBoolConst) {
		int t = 0;
		if(factorBoolConst.getB().equals("true")) {
			t = 1;
		}
		Obj ob = new Obj(Obj.Con,"",new Struct(Struct.Bool),t,0);
		Code.load(ob);
	}
	
	public void visit(FactorDes factorDes) {
		Obj ob = factorDes.getDesignator().obj;
		if(desop==false) {
			Code.load(ob);
		}else {
			lista.add(ob);
			ind++;
		}
	}
	
	public void visit(FactorNewActPars factorNewActPars) {
		Code.put(Code.newarray);
		int t = 1;
        if ( factorNewActPars.getType().struct == Tab.charType ) {
			t = 0;
        }
		Code.put(t);
		newarr=true;
	}
	
	public void visit(DesignatorC designatorC) {
		//Obj ob = new Obj(Obj.Con,"",Tab.intType);
		//ob.setAdr(designatorC.ge);
		//Code.load(ob);
	}
	
	public void visit(DesignatorArrayC designatorArrayC) {
		///Code.load(new Obj(Obj.Elem,"",designatorArrayC.getExpr().struct));
	}
	
	public void visit(DesignatorStatementExpr designatorStatementExpr) {
		
		//System.out.println("USAO: "+designatorStatementExpr.getDesignator().obj.getKind());
		//if(designatorStatementExpr.getDesignator().obj.getType().getKind() == Struct.Array) System.out.println("DA");
		Code.store(designatorStatementExpr.getDesignator().obj);
		
	}
	
	private int pomNum = 0;
	
	public void visit(NumConstOpC numConstOpC) {
		pomNum = numConstOpC.getN1();
	}
	
	public void visit(PrintStm printStm) {
		
		Struct ps = printStm.getExpr().struct;
		
		if(printStm.getNumConstOp().getClass() == NoNumConstOp.class) {
					
			if(ps == Tab.intType || ps.getKind() == Struct.Bool) {
				Code.loadConst(5);
				Code.put(Code.print);
			}else {
				Code.loadConst(1); 
				Code.put(Code.bprint);
			}
			
		}else {
			Code.load(new Obj(Obj.Con, "", Tab.intType, pomNum, 0));
			if(ps == Tab.intType || ps.getKind() == Struct.Bool) {
				Code.put(Code.const_5);
				Code.put(Code.print);
				Code.load(new Obj(Obj.Con, "", Tab.intType, pomNum, 0));
				Code.put(Code.print);
			}else {
				Code.put(Code.bprint);
			}
		}
		
		/*Struct ps = printStm.getExpr().struct;
		int t = 1;
		if(ps == Tab.intType || ps.getKind() == Struct.Bool ) {
			t = 5;
		}
		Code.loadConst(t);
		Code.put(Code.print);*/
	}
	
	public void visit(DesignatorStatementInc designatorStatementInc) {
		
		Code.load(designatorStatementInc.getDesignator().obj);
		Code.put(Code.const_1);
		Code.put(Code.add);
		Code.store(designatorStatementInc.getDesignator().obj);
	}
	
	public void visit(DesignatorStatementDec designatorStatementDec) {
		
		Code.load(designatorStatementDec.getDesignator().obj);
		Code.put(Code.const_1);
		Code.put(Code.sub);
		Code.store(designatorStatementDec.getDesignator().obj);
	}
	
	public void visit(ExprC exprC) {
		if(AddopPlus.class == exprC.getAddop().getClass()) {
			Code.put(Code.add);
		}else if(AddopMinus.class == exprC.getAddop().getClass()){
			Code.put(Code.sub);
		}	
	}
	
	public void visit(TermC termC) {
		if(MulopMul.class == termC.getMulop().getClass()) {
			Code.put(Code.mul);
		}
		else if(MulopDiv.class == termC.getMulop().getClass()) {
			Code.put(Code.div);
		}
		else if(MulopPer.class == termC.getMulop().getClass()) {
			Code.put(Code.rem);
		}
	}
	
	public void visit(DesignatorExpr designatorExpr) {
		Obj ob = designatorExpr.obj;
		if(desop==true) {
			if(DesignatorC.class == designatorExpr.getIdentExprList().getClass()) {
				//lista.add(ob);
				//ind++;
			}
		}
	}
	
	public void visit(IdentExprListLsquare identExprListLsquare) {
		Obj ob = identExprListLsquare.getIdentExprList().obj;
		if(desop) {
			arr = true;
			lista.add(ob);
			ind++;
		}
		else
			Code.load(ob);
		
	}
	
	public void visit(DesignatorOptC designatorOptC) {
		//System.out.println("DesignatorOptC: "+ lista.size());
		if(arr==false) {
			lista.add(designatorOptC.obj);
		}
		if(ind==0)ind++;
		indeksi.add(ind);
		//desop = true;
		arr=false;
		ind=0;
	}
	
	public void visit(NoDesignatorOpt noDesignatorOpt) {
		//System.out.println("NoDesignatorOptC: "+lista.size());
		indeksi.add(1);
		lista.add(null);
		//desop = true;
		ind=0;
	}
	
	public void visit(DesignatorStatementAssign designatorStatementAssign) {
		//System.out.println(" "+ lista.size());
		desop = false;
		arr=false;
		ind=0;
		Obj niz = designatorStatementAssign.getDesignator().obj;
		
		for(int i=0;i<indeksi.size();i++) {
			//System.out.println(i+": "+indeksi.get(i)+", ");
		}
		
		for(int i = 0, k = 0;i<indeksi.size();i++) {
			
			if(lista.get(k)!=null) {			
				if(indeksi.get(i)==1) {
					Code.load(niz);
					Code.put(Code.const_n+i);
					Code.put(Code.aload);
					Code.store(lista.get(k++));
					//System.out.println(i+" : "+ lista.get(i).getAdr()+", "+lista.get(i).getKind());
				}else {
					//Code.load(niz);
					//Code.put(Code.const_n+i);
					//Code.put(Code.aload);
					//Code.store(lista.get(i));
					//System.out.println(i+" : "+ lista.get(i).getAdr()+", "+lista.get(i).getKind());
					
					for(int j = 0; j<indeksi.get(i);j++) {
						Code.load(lista.get(k++));
					}
					
					//for(int j = 0; j<indeksi.get(i);j++) {
						//Code.put(Code.aload);
					//}
					
					Code.load(niz);
					Code.put(Code.const_n+i);
					Code.put(Code.aload);
					
					Code.put(Code.astore);
				}
				
			}else {
				k++;
			}
		}
		
		lista = new ArrayList<>();
		indeksi= new ArrayList<>();
	}
	
	
	public void visit(DLsquareC dLsquare) {
		//System.out.println("DLsquare");
		desop = true;
		arr=false;
	}
	
	public void visit(SingleExprc singleExprc) {
		if(singleExprc.getMinusOp().getClass() == MinusOptional.class) {
			Code.put(Code.neg);
		}
	}
	
	public void visit(MinusOptional minusOptional) {
		//Code.put(Code.neg);
		
		/*
		 int num = f.getN1();
			int pcJeq, pcJmp;
			
			Code.loadConst(num); // 4
			Code.put(Code.dup); // 4 4
			Code.loadConst(0); // 4 4 0
			
			pcJmp = Code.pc;
			
			Code.putFalseJump(Code.ne, 0); ///skida sa steka 2 operanda pa je stek sada -> 4
			
			pcJeq = Code.pc-2;
			
			//not equal 0
			Code.put(Code.dup); // 4 4
			Code.loadConst(1); //4 4 1
			Code.put(Code.sub); // 4 3
			
			Code.put(Code.dup); // 4 3 3
			Code.loadConst(0); // 4 3 3 0
			
			Code.putJump(pcJmp);
			
			// equal 0
			Code.fixup(pcJeq); // 4 3 2 1 0
			
			Code.put(Code.pop); // 4 3 2 1
			
			for(int i = 0; i < num - 1; i++) {
				Code.put(Code.mul);
			}
		 */
	}
	
	public void visit(ReadStm readStm) {
		int t = readStm.getDesignator().obj.getType().getKind();
		///System.out.println(readStm.getDesignator().obj.getLevel());
		Obj ob = readStm.getDesignator().obj;
		if(t == Struct.Array) {
			Code.put(Code.read);
			Code.store(ob);
			//System.out.println("1readStm");
		}else if(t == Struct.Int || t == Struct.Bool) {
			Code.put(Code.read);
			Code.store(ob);
			//System.out.println("2readStm");
		}else {
			Code.put(Code.bread);
			Code.store(ob);
			//System.out.println("3readStm");
		}		
	}
	
	
}
