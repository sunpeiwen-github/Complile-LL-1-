
import java.util.Scanner;
import java.util.*;
import java.io.*;
/*要求：
1.	文法使用产生式来定义；
2.	分别求出文法中每一个非终结符的FIRST 集、FOLLOW集和SELECT集；
3.	画出预测分析表；
4.	判定给定的文法是否是LL(1)文法；
5.	给定符号串判定是否是文法中的句子，分析过程用分析表格的方式打印出来。
*/

class GrammarElement {
    char[] formula;
    int length;
    Vn_set vnSet;
    Vt_set vtSet;
    HashMap<Character, Boolean> isEmpty_map;

    GrammarElement(String input, Vn_set vnSet, Vt_set vtSet, HashMap isEmpty) {
        formula = input.toCharArray();
        length = input.toCharArray().length;
        this.vnSet = vnSet;
        this.vtSet = vtSet;
        this.isEmpty_map = isEmpty;
    }

    void taverse()//遍历区别vn vt
    {
        vnSet.add_symbol(formula[0]);
        for (int i = 3; i < length; i++) {
            char a;
            a = formula[i];
//            if (a <= 'z' && a >= 'a') {//小写符号
//                vtSet.add_symbol(formula[i]);
//            }
            if (!(a <= 'Z' && a >= 'A') && a != '@') {//不是大写符号
                vtSet.add_symbol(formula[i]);
            }
        }
    }
    void print()
    {
        System.out.print(formula);
    }

}

class Grammar {
    GrammarElement[] grammar_c;
    int length;

    Grammar() {
    }

    void inital_grammar() {
        length = 0;
        grammar_c = new GrammarElement[100];
    }

    void add_grammar(GrammarElement grammarElement) {
        grammar_c[length] = grammarElement;
        length++;
    }

    char search_grammar(int grammarNum, int symbolNum)//输入产生式号和字符的位置返回字符
    {
        return grammar_c[grammarNum].formula[symbolNum];
    }

}

class Set {
    ArrayList<Character> set;

    Set() {
        set = new ArrayList<Character>();
    }

    void add_symbol(char a) {
        if (!isExist(a)) {
            set.add(a);
        }
    }

    boolean isExist(char a) {
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i) == a)
                return true;
        }
        return false;
    }
}

class Vn_set extends Set {

    Vn_set() {
        set = new ArrayList<Character>();
    }


}

class Vt_set extends Set {

    Vt_set() {
        set = new ArrayList<Character>();
    }
}

class First_set extends Set {
    Grammar grammar;
    HashMap<Character, Boolean> isEmpty_map;
    HashMap<Character, ArrayList<Character>> firstSet_map;
    Vn_set vnSet;
    Vt_set vtSet;

    First_set(Grammar grammar, HashMap firstSet, HashMap isEmpty, Vn_set vnSet, Vt_set vtSet) {
        set = new ArrayList<Character>();
        this.grammar = grammar;
        this.isEmpty_map = isEmpty;
        this.firstSet_map = firstSet;
        this.vnSet = vnSet;
        this.vtSet = vtSet;
    }

    void isEmpty_initial() {
        for (int i = 0; i < vnSet.set.size(); i++) {
            isEmpty_map.put(vnSet.set.get(i), false);
        }
        for (int i = 0; i < vtSet.set.size(); i++)//终结符也不能为空
        {
            isEmpty_map.put(vtSet.set.get(i), false);
        }
    }

    /*1找到该非终结符所在的产生式
     * 2右部第一个三种情况 vn vt @
     * 应该初始化都不为空 false 然后直到有一个推出空后才置true
     * vt 下一个
     * vn 递归
     * @ 设置指针x x++
     * x>当前产生式的length则为空*/
    void isEmpty_m(char a)//判断非终结符是否为空的方法
    {
        if (isEmpty_map.get(a))
            return;
        int x = 3;
        for (int i = 0; i < grammar.length; i++) {
            if (grammar.search_grammar(i, 0) == a) {
//                if (vtSet.isExist(grammar.search_grammar(i, x))) {
//                    continue;
//                } else
                if (vnSet.isExist(grammar.search_grammar(i, x))) {
                    isEmpty_m(grammar.search_grammar(i, x));
                    if (!isEmpty_map.get(grammar.search_grammar(i, x))) {
                        continue;
                    }
                }
                while (grammar.search_grammar(i, x) == '@' || isEmpty_map.get(grammar.search_grammar(i, x))) {
                    x++;
                    if (x >= grammar.grammar_c[i].formula.length) {
                        isEmpty_map.put(a, true);
                        return;
                    }
                    if (vtSet.isExist(grammar.search_grammar(i, x))) {
                        break;
                    } else if (vnSet.isExist(grammar.search_grammar(i, x))) {
                        isEmpty_m(grammar.search_grammar(i, x));
                        if (!isEmpty_map.get(grammar.search_grammar(i, x))) {
                            break;
                        }
                    }

                }
            }
        }
    }

    /*first是对于每个非终结符来讲的
     * 所以要能够遍历到每一个左部为A的产生式
     *
     * 右部首字符为终结符直接将其加如该非终结符A的hash表
     * 右部为非终结符时 先判断其first是否求好
     * 若求好则将其集合加入A的first
     * 若没求好则递归先求该非终结符的first
     *
     * 最后判断右部第一非终结符是否为空
     * 若是空则指针右移再循环以上步骤*/
    void get_first(char a) {
        if (firstSet_map.get(a) != null)  //需要通过这个判断是否以经有获得完整的first了
            return;

        for (int i = 0; i < grammar.length; i++) {
            int x = 3;
            char S = grammar.search_grammar(i, 0);//S为左部
            char r = grammar.search_grammar(i, x);//需要判断的右部
            if (S == a) {

                if (vtSet.isExist(r)) {
                    addSymbol(S, r);
//                    System.out.println("1");
                }
                if (vnSet.isExist(r)) {
//                    System.out.println("1.5");
                    if (firstSet_map.get(r) == null) {
                        get_first(r);
                    }

                    addSet(S, r);
//                    System.out.println("2");
                }
                while (r == '@' || isEmpty_map.get(r)) {
                    x++;
                    if (x >= grammar.grammar_c[i].formula.length)//超过当前产生式的长度 说明S可以推出空
                    {
                        addSymbol(S, '@');
//                        System.out.println("3");
                        break;
                    }
                    r = grammar.search_grammar(i, x);
                    if (vtSet.isExist(r)) {
                        addSymbol(S, r);
//                        System.out.println("4");
                    }
                    if (vnSet.isExist(r)) {
//                        System.out.println("1.5");
                        if (firstSet_map.get(r) == null) {
                            get_first(r);
                        }
                        addSet(S, r);
//                        System.out.println("6");
                    }
                }
//                System.out.println("7");
            }
        }
    }

    void addSymbol(char S, char a) {
        ArrayList<Character> list;
        list = new ArrayList();
        boolean flag = false;
        if (firstSet_map.get(S) != null) {
            for (int i = 0; i < firstSet_map.get(S).size(); i++) {
                list.add(firstSet_map.get(S).get(i));
                if (firstSet_map.get(S).get(i) == a)//运算符 '==' 不能应用于 'java.lang.Object', 'char'
                {
                    flag = true;
                }
            }

        }
        if (flag == false) {
            list.add(a);
        }
        firstSet_map.put(S, list);
    }

    void addSet(char S, char a)//用之前需要确定a不为空
    {
        ArrayList<Character> list;
        list = new ArrayList();
        if (firstSet_map.get(S) != null) {
            for (int i = 0; i < firstSet_map.get(S).size(); i++) {
                list.add(firstSet_map.get(S).get(i));
            }
            for (int i = 0; i < firstSet_map.get(a).size(); i++) {
                boolean flag = false;
                for (int j = 0; j < firstSet_map.get(S).size(); j++) {
                    if (firstSet_map.get(S).get(j) == firstSet_map.get(a).get(i)) {
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    list.add(firstSet_map.get(a).get(i));
                }
            }
        } else //如果S是空的话直接加
        {
            for (int i = 0; i < firstSet_map.get(a).size(); i++) {
                list.add(firstSet_map.get(a).get(i));
            }

        }

        firstSet_map.put(S, list);
    }

}

class Follow_set extends Set {
    Grammar grammar;
    HashMap<Character, Boolean> isEmpty_map;
    HashMap<Character, ArrayList<Character>> followSet_map;
    HashMap<Character, ArrayList<Character>> firstSet_map;
    HashMap<Integer, ArrayList<Character>> selectSet_map;
    HashMap<Character,Integer> vn_map;
    HashMap<Character,Integer> vt_map;
    Vn_set vnSet;
    Vt_set vtSet;

    Follow_set(Grammar grammar, HashMap followSet, HashMap firstSet_map,HashMap selectSet_map, HashMap isEmpty, Vn_set vnSet, Vt_set vtSet) {
        set = new ArrayList<Character>();
        this.grammar = grammar;
        this.isEmpty_map = isEmpty;
        this.followSet_map = followSet;
        this.firstSet_map = firstSet_map;
        this.selectSet_map = selectSet_map;
        this.vnSet = vnSet;
        this.vtSet = vtSet;
    }

    void get_follow(char a) {
        if (followSet_map.get(a) != null) {
            return;
        }
        int next = 0;
        for (int i = 0; i < grammar.length; i++) {
            for (int j = 3; j < grammar.grammar_c[i].formula.length; j++) {
                if (grammar.search_grammar(i, j) == a) {
                    char[] formula = grammar.grammar_c[i].formula;
                    next = j + 1;
                    if (next >= formula.length) {
                        if (followSet_map.get(formula[0]) == null) {
                            get_follow(formula[0]);
                        }
                        addSet(a, formula[0]);
                    } else if (vtSet.isExist(formula[next])) {
                        addSymbol(a, formula[next]);
                    } else if (vnSet.isExist(formula[next])) {
                        addFirsttoFollow(a, formula[next]);
                        while (isEmpty_map.get(formula[next])) {
                            next++;
                            if (next >= formula.length) {
                                if (followSet_map.get(formula[0]) == null) {
                                    get_follow(formula[0]);
                                }
                                addSet(a, formula[0]);
                                break;//超过length要跳出循环 否则出错
                            } else if (vtSet.isExist(formula[next])) {
                                addSymbol(a, formula[next]);
                            } else if (vnSet.isExist(formula[next])) {
                                addFirsttoFollow(a, formula[next]);
                            }
                        }
                    }
                }
            }
        }
    }

    void addSymbol(char S, char a) {
        ArrayList<Character> list;
        list = new ArrayList();
        boolean flag = false;
        if (followSet_map.get(S) != null) {
            for (int i = 0; i < followSet_map.get(S).size(); i++) {
                list.add(followSet_map.get(S).get(i));
                if (followSet_map.get(S).get(i) == a)//运算符 '==' 不能应用于 'java.lang.Object', 'char'
                {
                    flag = true;
                }
            }

        }
        if (flag == false) {
            list.add(a);
        }
        followSet_map.put(S, list);
    }

    void addSet(char S, char a)
    {
        ArrayList<Character> list;
        list = new ArrayList();
        if (followSet_map.get(S) != null) {
            for (int i = 0; i < followSet_map.get(S).size(); i++) {
                list.add(followSet_map.get(S).get(i));
            }
            for (int i = 0; i < followSet_map.get(a).size(); i++) {
                boolean flag = false;
                for (int j = 0; j < followSet_map.get(S).size(); j++) {
                    if (followSet_map.get(S).get(j) == followSet_map.get(a).get(i)) {
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    list.add(followSet_map.get(a).get(i));
                }
            }
        } else //如果S是空的话直接加
        {
            for (int i = 0; i < followSet_map.get(a).size(); i++) {
                list.add(followSet_map.get(a).get(i));
            }

        }

        followSet_map.put(S, list);
    }

    void addFirsttoFollow(char S, char a)//将a的first加入S的follow中 且剔除first中的@
    {
        ArrayList<Character> list;
        list = new ArrayList();
        if (followSet_map.get(S) != null) {
            for (int i = 0; i < followSet_map.get(S).size(); i++) {
                list.add(followSet_map.get(S).get(i));
            }
            for (int i = 0; i < firstSet_map.get(a).size(); i++) {
//                if (firstSet_map.get(a).get(i) != '@') {//好像没有发挥作用
                    boolean flag = false;
                    for (int j = 0; j < followSet_map.get(S).size(); j++) {
                        if (followSet_map.get(S).get(j) == firstSet_map.get(a).get(i)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false) {
                        list.add(firstSet_map.get(a).get(i));
                    }
//                }
            }
        } else //如果S是空的话直接加
        {
            for (int i = 0; i < firstSet_map.get(a).size(); i++) {
                list.add(firstSet_map.get(a).get(i));
            }
        }
        followSet_map.put(S, list);
    }
    void followSet_endOperate()//剔除@ 加上#
    {
        for (int i = 0; i < vnSet.set.size(); i++) {
            addSymbol(vnSet.set.get(i),'#');
            ArrayList<Character> nowList=followSet_map.get(vnSet.set.get(i));
           for(int j=0;j<nowList.size();j++)
           {
               if(nowList.get(j)=='@')
               {
                  nowList.remove(j);
                  followSet_map.put(vnSet.set.get(i),nowList);
               }
           }
        }
    }

    void getSelect()
    {
        for(int i=0;i<grammar.length;i++)
        {
            char[] formula=grammar.grammar_c[i].formula;
            int x=3;
            if(formula[3]=='@')
            {
                addFollowToSelect(i,formula[0]);
            }
            else if(vtSet.isExist(formula[x]))
            {
                addSymbolToSelect(i,formula[x]);
            }
            else if(vnSet.isExist(formula[x]))
            {
                addFirstToSelect(i,formula[x]);
                while(isEmpty_map.get(formula[x]))
                {
                    x++;
                    if(x>=formula.length)
                    {
                        addFollowToSelect(i,formula[0]);
                        break;
                    }
                    if(vtSet.isExist(formula[x]))
                    {
                        addSymbolToSelect(i,formula[x]);
                    }
                    if(vnSet.isExist(formula[x]))
                    {
                        addFirstToSelect(i,formula[x]);
                    }

                }
            }

        }
    }
    void addFirstToSelect(int key, char a)//a的first加入key对应的select
    {
        ArrayList<Character> list;
        list = new ArrayList();
        if (selectSet_map.get(key) != null) {
            for (int i = 0; i < selectSet_map.get(key).size(); i++) {
                list.add(selectSet_map.get(key).get(i));
            }
            for (int i = 0; i < firstSet_map.get(a).size(); i++) {
//                if (firstSet_map.get(a).get(i) != '@') {//好像没有发挥作用
                boolean flag = false;
                for (int j = 0; j < selectSet_map.get(key).size(); j++) {
                    if (selectSet_map.get(key).get(j) == firstSet_map.get(a).get(i)) {
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    list.add(firstSet_map.get(a).get(i));
                }
//                }
            }
        } else //如果S是空的话直接加
        {
            for (int i = 0; i < firstSet_map.get(a).size(); i++) {
                list.add(firstSet_map.get(a).get(i));
            }
        }
        selectSet_map.put(key, list);
    }
    void addFollowToSelect(int key, char a)//a的follow加入key对应的select
    {
        ArrayList<Character> list;
        list = new ArrayList();
        if (selectSet_map.get(key) != null) {
            for (int i = 0; i < selectSet_map.get(key).size(); i++) {
                list.add(selectSet_map.get(key).get(i));
            }
            for (int i = 0; i < followSet_map.get(a).size(); i++) {
//                if (firstSet_map.get(a).get(i) != '@') {//好像没有发挥作用
                boolean flag = false;
                for (int j = 0; j < selectSet_map.get(key).size(); j++) {
                    if (selectSet_map.get(key).get(j) == followSet_map.get(a).get(i)) {
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    list.add(followSet_map.get(a).get(i));
                }
//                }
            }
        } else //如果S是空的话直接加
        {
            for (int i = 0; i < followSet_map.get(a).size(); i++) {
                list.add(followSet_map.get(a).get(i));
            }
        }
        selectSet_map.put(key, list);
    }
    void addSymbolToSelect(int key, char a) {
        ArrayList<Character> list;
        list = new ArrayList();
        boolean flag = false;
        if (selectSet_map.get(key) != null) {
            for (int i = 0; i < selectSet_map.get(key).size(); i++) {
                list.add(selectSet_map.get(key).get(i));
                if (selectSet_map.get(key).get(i) == a)//运算符 '==' 不能应用于 'java.lang.Object', 'char'
                {
                    flag = true;
                }
            }
        }
        if (flag == false) {
            list.add(a);
        }
        selectSet_map.put(key, list);
    }
    void createVnHash()
    {
        vn_map=new HashMap<>();
        for(int i=0;i<vnSet.set.size();i++)
        {
            vn_map.put(vnSet.set.get(i),i);
        }
    }
    void createVtHash()
    {
        vt_map=new HashMap<>();
        for(int i=0;i<vtSet.set.size();i++)
        {
            vt_map.put(vtSet.set.get(i),i);
        }
    }
    GrammarElement[][] get_table() throws Exception
    {
        vtSet.set.add('#');//+1去掉
        GrammarElement[][] table=new GrammarElement[vnSet.set.size()][vtSet.set.size()];
        GrammarElement empty=new GrammarElement("error", vnSet,vtSet, isEmpty_map);
//        vtSet.set.add('#');//需要+1
        createVnHash();
        createVtHash();

        for(int i=0;i<vnSet.set.size();i++)
        {
            for(int j=0;j< vtSet.set.size();j++)
            {
                table[i][j]=empty;
            }
        }
        for(int i=0;i<grammar.length;i++)
        {
            GrammarElement gElement=grammar.grammar_c[i];
            int count=vn_map.get(gElement.formula[0]);//找到行号
            ArrayList<Character> list=selectSet_map.get(i);//找到对应的select集合
            for(int j=0;j<vtSet.set.size();j++)
            {
                if(list.contains(vtSet.set.get(j)))
                {
                    if(compare(table[count][j].formula,("error")))
                    { table[count][j]=gElement;}
                    else
                    {
                        System.out.println(gElement.formula[0]+"的select集合有冲突");
                        System.out.println("当遇到vt"+vtSet.set.get(j)+"会出现");
                        System.out.print(gElement.formula);
                        System.out.print("和");
                        System.out.println(table[count][j].formula);
                        throw new Exception() ;
                    }
                }
            }
        }
        System.out.print("    ");
        for(int i=0;i<vtSet.set.size();i++) {
            System.out.print(vtSet.set.get(i));
            System.out.print("     ");
        }
        System.out.println();
        int k=0;
        for(int i=0;i<vnSet.set.size();i++)
        {
            System.out.print(vnSet.set.get(k));
            System.out.print(" ");
            k++;
            for(int j=0;j< vtSet.set.size();j++)
            {
                table[i][j].print();
                System.out.print(" ");
            }
            System.out.println("");
        }
        return table;
    }
    boolean compare(char[]c,String s)
    {
        char[] str=s.toCharArray();
        for(int i=0;i<c.length;i++)
        {
            if(c[i]!=str[i])
                return false;
        }
        return true;
    }
}
class AnalyseTable
{
    Grammar grammar;
    GrammarElement[][] table;
    Vt_set vtSet;
    Vn_set vnSet;
    HashMap<Character,Integer> vn_map;
    HashMap<Character,Integer> vt_map;
    char[] input;
    Stack1 stack;
    AnalyseTable(Grammar grammar,  Vn_set vnSet, Vt_set vtSet,GrammarElement[][] table)
    {
        this.grammar = grammar;
        this.vnSet = vnSet;
        this.vtSet = vtSet;
        this.table=table;
    }
    void initial()
    {
        createVnHash();
        createVtHash();
        stack=new Stack1();
        Scanner s = new Scanner(System.in);
        System.out.println("输入要分析的字符串");
        String str=s.next();
        input=str.toCharArray();
        stack.push('#');
        stack.push(grammar.grammar_c[0].formula[0]);//开始符号
        System.out.print("栈"+"        ");//10个半格
        System.out.print("待判断字串"+"        ");
        System.out.println("操作");
    }
    void analyse() throws Exception
//      void analyse()
    {
        while(true)
        {
            stack.printAllStack();
            char a=stack.pop();
            System.out.print("        ");
            System.out.print(input);
            System.out.print("        ");
            if(vnSet.isExist(a))
            {
                char[] buff=table[vn_map.get(a)][vt_map.get(input[0])].formula;
                System.out.println(buff);
                if(buff.equals("error"))
                {
                    System.out.println("error");
                    throw new Exception();
                }
                else
                {
                    if(buff[3]!='@') {
                        for (int i = buff.length - 1; i > 2; i--) {
                            stack.push(buff[i]);
                        }
                    }
                }
            }
            if(vtSet.isExist(a))
            {
                if(a==input[0]&&a!='#')
                {
                    System.out.println(input[0]+"匹配");
                    input=leftArray(input);
                }
                else if(a==input[0]&&a=='#')
                {
                    System.out.println("接受");
                    break;
                }
                else if(a!=input[0])
                {
                    System.out.println("终结符不匹配");
                    throw new Exception();
                }
            }
        }
    }
    char[] leftArray(char[]c)
    {
        char[]b=new char[c.length-1];
        for(int i=1;i<c.length;i++)
        {
            b[i-1]=c[i];
        }
        return b;
    }
    void createVnHash()
    {
        vn_map=new HashMap<>();
        for(int i=0;i<vnSet.set.size();i++)
        {
            vn_map.put(vnSet.set.get(i),i);
        }
    }
    void createVtHash()
    {
        vt_map=new HashMap<>();
        for(int i=0;i<vtSet.set.size();i++)
        {
            vt_map.put(vtSet.set.get(i),i);
        }
    }

}
class Stack1
{
    char[] stack;
    int top=-1;
    Stack1()
    {
        stack=new char[50];
        top=-1;
    }
    void push(char a)
    {
        stack[++top]=a;
    }
    char pop()
    {
        return stack[top--];
    }

    void printAllStack()
    {
        char[]a=new char[top+1];
        for(int i=0;i<top+1;i++)
        {
            a[i]=stack[i];
        }
        System.out.print(a);
    }

}
class Io {
    File file_grammar;
    File file_output;
    BufferedReader br;
    BufferedWriter bw;
    Vn_set vnSet;
    Vt_set vtSet;
    HashMap<Character, Boolean> isEmpty;

    Io(File file_grammar, File file_output, Vn_set vnSet, Vt_set vtSet, HashMap isEmpty) {
        this.file_grammar = file_grammar;
        this.file_output = file_output;
        try {
            file_grammar.createNewFile();
            file_output.createNewFile();
        } catch (IOException e) {
            System.out.println("grammar文件是否存在" + file_grammar.exists());
            System.out.println("output文件是否存在" + file_output.exists());
            System.out.println("文件创建问题");
        }
        this.vnSet = vnSet;
        this.vtSet = vtSet;
        this.isEmpty = isEmpty;
    }

    Grammar get_grammar() {
        Grammar grammar = new Grammar();
        grammar.inital_grammar();
        try {
            br = new BufferedReader(new FileReader(file_grammar));
            String line = null;

            while (((line = br.readLine()) != null)) {
//                System.out.println("io");
//                System.out.println(line);
                grammar.add_grammar(new GrammarElement(line, vnSet, vtSet, isEmpty));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grammar;
    }

}
public class test {

    public static Grammar grammar;
    public static Vn_set vnSet;
    public static Vt_set vtSet;
    public static First_set firstSet;
    public static Follow_set followSet;
    public static HashMap<Character, Boolean> isEmpty_map;
    public static HashMap<Character, ArrayList<Character>> firstSet_map;
    public static HashMap<Character, ArrayList<Character>> followSet_map;
    public static HashMap<Integer, ArrayList<Character>> selectSet_map;//select是对于单个产生式来说的
    public static GrammarElement[][] table;
    public static AnalyseTable analyseTable;
    public static void main(String[] args) {
        File file_grammar;
        file_grammar = new File("D://Learn//编译原理ll1(2)//grammar.txt");
        File file_output;
        file_output = new File("D://Learn//编译原理ll1(2)//output.txt");

        vnSet = new Vn_set();
        vtSet = new Vt_set();
        isEmpty_map = new HashMap<Character, Boolean>();
        firstSet_map = new HashMap<Character, ArrayList<Character>>();
        followSet_map = new HashMap<Character, ArrayList<Character>>();
        selectSet_map=new HashMap<Integer,ArrayList<Character>>();
        Io io = new Io(file_grammar, file_output, vnSet, vtSet, isEmpty_map);
        grammar = io.get_grammar();

        firstSet = new First_set(grammar, firstSet_map, isEmpty_map, vnSet, vtSet);
        followSet = new Follow_set(grammar, followSet_map, firstSet_map, selectSet_map,isEmpty_map, vnSet, vtSet);



        for (int i = 0; i < grammar.length; i++) {
            grammar.grammar_c[i].taverse();
        }
        System.out.print("终结符：");
        System.out.println(vtSet.set);//数组直接打印输出中不能加字符串
        System.out.print("非终结符：");
        System.out.println(vnSet.set);
        System.out.println("产生式");
        for (int i = 0; i < grammar.length; i++) {
            System.out.println(grammar.grammar_c[i].formula);
        }
        System.out.println("能推出空的符号");
        firstSet.isEmpty_initial();
        for (int i = 0; i < vnSet.set.size(); i++) {
            firstSet.isEmpty_m(vnSet.set.get(i));
            if (isEmpty_map.get(vnSet.set.get(i)))//vnSet.set.get(i)
                System.out.println(vnSet.set.get(i));
        }
//        for (int i = 0; i < vnSet.set.size(); i++) {
//            if (firstSet.isEmpty_m(vnSet.set.get(i)))//vnSet.set.get(i)
//                System.out.println(vnSet.set.get(i));
//        }
        System.out.println("first集合");
        for (int i = 0; i < vnSet.set.size(); i++) {
            firstSet.get_first(vnSet.set.get(i));
            System.out.print(vnSet.set.get(i));
            System.out.println(firstSet_map.get(vnSet.set.get(i)));
        }
        System.out.println("follow集合");
        for (int i = 0; i < vnSet.set.size(); i++) {
            followSet.get_follow(vnSet.set.get(i));
        }
        followSet.followSet_endOperate();
        for (int i = 0; i < vnSet.set.size(); i++) {
            System.out.print(vnSet.set.get(i));
            System.out.println(followSet_map.get(vnSet.set.get(i)));
        }
        System.out.println("Select集合");
        followSet.getSelect();
        for(int i=0;i< grammar.length;i++)
        {
            System.out.print(grammar.grammar_c[i].formula);
            System.out.println(selectSet_map.get(i));
        }
        System.out.println("预测分析表");
        try {
            table=followSet.get_table();
        }
        catch (Exception e)
        {
            System.out.println("该文法不是LL1文法");
        }
        analyseTable=new AnalyseTable(grammar,vnSet,vtSet,table);
        analyseTable.initial();
        try{
            analyseTable.analyse();
        }
        catch(Exception  e)
        {
            System.out.println("该输入串不符合该文法");
        }
    }
}
