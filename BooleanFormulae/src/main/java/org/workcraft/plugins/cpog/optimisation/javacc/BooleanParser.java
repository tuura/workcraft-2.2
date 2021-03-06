/* Generated By:JavaCC: Do not edit this line. BooleanParser.java */
package org.workcraft.plugins.cpog.optimisation.javacc;

import java.util.Collection;
import java.util.HashMap;
import java.io.Reader;
import java.io.StringReader;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.*;
import org.workcraft.plugins.cpog.optimisation.*;
import org.workcraft.util.Func;

@SuppressWarnings("all")
public class BooleanParser implements BooleanParserConstants {
  public BooleanParser(Reader reader, Func<String, ? extends BooleanFormula> vars)
  {
    this (reader);
    this.vars = vars;
  }
  BooleanWorker worker = new org.workcraft.plugins.cpog.optimisation.expressions.CleverBooleanWorker();
  Func<String, ? extends BooleanFormula> vars;
  BooleanFormula var(String name) throws ParseException
  {
        BooleanFormula result = vars.eval(name);
    if (result == null)
    throw new ParseException("Undefined variable: '" + name + "'");
    return result;
  }
  BooleanFormula constant(String value)
  {
    return "0".equals(value) ? worker.zero() : worker.one();
  }
  BooleanFormula and(BooleanFormula a, BooleanFormula b)
  {
    return worker.and(a, b);
  }
  BooleanFormula or(BooleanFormula a, BooleanFormula b)
  {
    return worker.or(a, b);
  }
  BooleanFormula eq(BooleanFormula a, BooleanFormula b)
  {
    return worker.iff(a, b);
  }
  BooleanFormula imply(BooleanFormula a, BooleanFormula b)
  {
    return worker.imply(a, b);
  }
  BooleanFormula xor(BooleanFormula a, BooleanFormula b)
  {
    return worker.xor(a, b);
  }
  BooleanFormula not(BooleanFormula a)
  {
    return worker.not(a);
  }

  public static BooleanFormula parse(String text, Func<String, ? extends BooleanFormula> vars) throws ParseException
  {
    return new BooleanParser(new StringReader(text), vars).formula();
  }

  public static BooleanFormula parse(String text, Collection <? extends BooleanVariable> variables) throws ParseException
  {
    final HashMap<String, BooleanVariable> map = new HashMap<String, BooleanVariable>();
    for(BooleanVariable var : variables)
          map.put(var.getLabel(), var);
    return parse(text, new Func<String, BooleanVariable>()
    {
      public BooleanVariable eval(String label)
      {
        return map.get(label);
      }
    });
  }

  public static void main(String [] args) throws Exception
  {
    System.out.println(FormulaToString.toString(new BooleanParser(System.in).formula()));
  }

  final public BooleanFormula formula() throws ParseException {
        BooleanFormula result;
        BooleanFormula op;
    result = eqOp();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case EQUALS:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      jj_consume_token(EQUALS);
      op = eqOp();
    result = eq(result, op);
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public BooleanFormula eqOp() throws ParseException {
        BooleanFormula result;
        BooleanFormula op;
    result = neqOp();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOTEQUALS:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      jj_consume_token(NOTEQUALS);
      op = neqOp();
    result = xor(result, op);
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public BooleanFormula neqOp() throws ParseException {
        BooleanFormula result;
        BooleanFormula op;
    result = implyOp();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IMPLIES:
      jj_consume_token(IMPLIES);
      op = implyOp();
    result = imply(result, op);
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public BooleanFormula implyOp() throws ParseException {
        BooleanFormula result;
        BooleanFormula op;
    result = orOp();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      jj_consume_token(OR);
      op = orOp();
    result = or(result, op);
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public BooleanFormula orOp() throws ParseException {
        BooleanFormula result;
        BooleanFormula op;
    result = xorOp();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case XOR:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_4;
      }
      jj_consume_token(XOR);
      op = xorOp();
    result = xor(result, op);
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public BooleanFormula xorOp() throws ParseException {
        BooleanFormula result;
        BooleanFormula op;
    result = andOp();
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_5;
      }
      jj_consume_token(AND);
      op = andOp();
    result = and(result, op);
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public BooleanFormula andOp() throws ParseException {
  boolean inverse = false;
  BooleanFormula result;
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_6;
      }
      jj_consume_token(NOT);
                   inverse = !inverse;
    }
    result = literal();
    label_7:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case POSTNOT:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_7;
      }
      jj_consume_token(POSTNOT);
                       inverse = !inverse;
    }
          {if (true) return inverse ? not(result) : result;}
    throw new Error("Missing return statement in function");
  }

  final public BooleanFormula literal() throws ParseException {
  Token t;
  BooleanFormula result;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case VARIABLE:
      t = jj_consume_token(VARIABLE);
          result = var(t.image);
      break;
    case CONSTANT:
      t = jj_consume_token(CONSTANT);
          result = constant(t.image);
      break;
    case 15:
      jj_consume_token(15);
      result = formula();
      jj_consume_token(16);
      break;
    default:
      jj_la1[8] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public BooleanParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[9];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x20,0x40,0x80,0x100,0x200,0x400,0x800,0x1000,0xe000,};
   }

  /** Constructor with InputStream. */
  public BooleanParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public BooleanParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new BooleanParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public BooleanParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new BooleanParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public BooleanParser(BooleanParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(BooleanParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 9; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[17];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 9; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 17; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
