import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodTokenizer {

    public static Set<String> ALL_TOKENS = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println(tokenize("src/main/java/MethodTokenizer.java"));
    }

    public static List<Pair<MethodDeclaration, String>> tokenize(String filePath) {
        return new MethodTokenizer().doTokenize(filePath);
    }

    private List<Pair<MethodDeclaration, String>> methods = new ArrayList<>();

    private List<Pair<MethodDeclaration, String>> doTokenize(String filePath) {
        // creates an input stream for the file to be parsed
        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // parse it
        CompilationUnit cu = JavaParser.parse(in);

        // visit and print the methods names
        cu.accept(new MethodVisitor(), null);

        return this.methods;
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration method, Void arg) {
            final StringBuilder ast = new StringBuilder();

            VoidVisitor<Integer> v = new TreeVisitior() {
                @Override
                public void in(Node n, Integer indentLevel) {
                    String token = n.getClass().getSimpleName();
                    ALL_TOKENS.add(token);
                    ast.append("(" + token);
                }

                @Override
                public void out(Node n, Integer indentLevel) {
                    ast.append(")");
                }
            };

            method.accept(v, 0);

            String buildAst = ast.toString();//.replaceAll("\\(([^\\)]+)\\)", "$1");

            methods.add(new Pair<>(method, buildAst));

            super.visit(method, arg);
        }
    }
}
