package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
			FileReader r = new FileReader(args[0]); 		
			Yylex skener = new Yylex(r); 		
			MJParser p = new MJParser(skener);		
			Symbol s = p.parse(); 	
			Program prog = (Program)(s.value);
			
			Tab.init();
			
			// ispis sintaksnog stabla
	        System.out.println(prog.toString(""));
			System.out.println("============================================");

			
			// ispis prepoznatih programskih konstrukcija
			SemanticAnalyzer v = new SemanticAnalyzer();  		
			prog.traverseBottomUp(v); 
			
			System.out.println("===========================================");

			System.out.println(v.broj_promenljivih + "     global variables");
			System.out.println(v.broj_konstanti + "     global constants");
			System.out.println(v.broj_nizova + "     global arrays");
			System.out.println(v.broj_lokalnih_prom + "     local variables in main");
			
			Tab.dump();

			if(!v.err && v.passed()) {
				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);
				Code.write(new FileOutputStream(args[1]));
				System.out.println("Parsiranje uspesno zavrseno!");
			}
			else {
				System.out.println("Parsiranje NIJE uspesno zavrseno!");
			}
			
		}
		finally {
			
		}

	}
	
	
}
