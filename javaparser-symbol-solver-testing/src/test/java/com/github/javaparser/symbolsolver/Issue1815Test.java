package com.github.javaparser.symbolsolver;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.resolution.AbstractResolutionTest;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

class Issue1815Test extends AbstractResolutionTest {

  @BeforeEach
  void setUp() throws IOException {
    // configure symbol solver before parsing
    CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(true));

    //This jar has been obfuscated, but the method signatures are still readable
    typeSolver.add(new JarTypeSolver(adaptPath("src/test/resources/issue1815/obfuscated.jar")));
    StaticJavaParser.getConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
  }

  @Test
  void errorcase_javassist_arrayIndexOutOfBounds() throws IOException {
    CompilationUnit cu = parseSample("issue1815/issue1815");

//    VoidVisitor visitor = createDebugVisitor(); // Print the exception
    VoidVisitor visitor = createTestVisitor();

    visitor.visit(cu, null);
  }

  private VoidVisitor<?> createDebugVisitor() {
    return new VoidVisitorAdapter() {
      @Override
      public void visit(MethodCallExpr n, Object arg) {
        System.out.println("Resolving: " + n.toString());
        try {
          n.resolve();
        } catch (ArrayIndexOutOfBoundsException e) {
          //e.printStackTrace();
          System.err.println(e.getMessage());
          System.err.println("\t@\t" +  e.getStackTrace()[0]);
          System.err.println("\t@\t" +  e.getStackTrace()[1]);
          System.err.println("\t@\t" +  e.getStackTrace()[2]);
          System.err.println("\t@\t" +  e.getStackTrace()[3]);
        }
        System.out.println();
      }
    };
  }

  private VoidVisitor<?> createTestVisitor() {
    return new VoidVisitorAdapter() {
      @Override
      public void visit(MethodCallExpr n, Object arg) {
        n.resolve();
      }
    };
  }
}
