import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TruthTable {

    private class TruthCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (((JLabel)c).getText().equals("true")) {
                c.setBackground(trueColor);
            } else if (((JLabel)c).getText().equals("false")) {
                c.setBackground(falseColor);
            }
            return c;
        }
    }


    Color trueColor, falseColor;
    JTable table;
    String[][] data;
    MainWindow parent;
    JScrollPane scrollPane;
    public TruthTable(MainWindow parent) {
        this.parent = parent;
        scrollPane = new JScrollPane();
        this.trueColor = new Color(187,255,221);
        this.falseColor = new Color(255, 227, 227);

    }


    public void setData(String[][] data, String[] headers) {
        this.data = data;
        parent.remove(scrollPane);
        table = new JTable(data, headers);
        scrollPane = new JScrollPane(table);
        parent.add(scrollPane, BorderLayout.CENTER);
        for (int x=0; x<table.getColumnCount(); x++)
            table.getColumnModel().getColumn(x).setCellRenderer(new TruthCellRenderer());
        parent.revalidate();

    }

    public void setDataAt(int x, int y, String to) {
        data[y][x] = to;
    }

    public void calculate(String data) throws Exception {
        String modifiedData = "("+data+")";
        //System.out.println("\n\nInitial: "+data);
        //System.out.println("Initial Length: "+data.length());

        //get deepest set of parenthesis (?:\([^\(\)]*?\))
        HashMap<String, ArrayList<int[]>> vars = regexHelper("(?:[a-zA-Z]*)", modifiedData);
        HashMap<String, ArrayList<int[]>> deepest =  regexHelper("(?:\\([^\\(\\)]*?\\))", modifiedData);

        ArrayList<String> groups = new ArrayList<>();
        //id based negative lookback (ie, 0 solve first then 1, etc.)

        //System.out.println("Vars: "+printableSet(vars));

        //System.out.println("\Tree:");
        while (!deepest.isEmpty()) {
            //System.out.println("\t" + printableSet(deepest));
            for (String group : deepest.keySet()) {
                groups.add(group);
                for (int[] location : deepest.get(group)) {
                    modifiedData = modifiedData.substring(0, location[0]) + "{" + (groups.size() - 1) + "}" +
                            modifiedData.substring(location[1]);
                    for (String a : deepest.keySet()) {
                        for (int[]  b : deepest.get(a)) {
                            if (b[0] > location[0]) {
                                int newDiffStringSize = (location[1]-location[0])-(2+(""+(groups.size()-1)).length());
                                b[0] -= newDiffStringSize;
                                b[1] -= newDiffStringSize;
                            }
                        }
                    }
                }
                //System.out.println("\t" + " newData:" + modifiedData);
            }
            //System.out.println();
            deepest = regexHelper("(?:\\([^\\(\\)]*?\\))", modifiedData);
        }
        //System.out.println("Final Groups: "+groups);

        //System.out.println("Starting Evaluations:");
        ArrayList<TreeMap<String, Boolean>> tableData = eval(groups, vars);
        String[][] tableData2d = new String[tableData.size()][groups.size()+ vars.size()];
        ArrayList<String> headers = new ArrayList<>();
        boolean grabbedHeaders = false;
        int y = 0;
        for (TreeMap<String, Boolean> curMap :  tableData) {
            int x =0;
            for (String h : curMap.keySet()) {
                if (!grabbedHeaders) headers.add(h);
                tableData2d[y][x] = String.valueOf(curMap.get(h));
                x++;
            }
            grabbedHeaders = true;
            y++;
        }

        String[] arrHeaders = new String[headers.size()];
        for (int i = 0; i < arrHeaders.length; i++) arrHeaders[i] = headers.get(i);
        setData(tableData2d, arrHeaders);
    }

    public String printableSet(HashMap<String, ArrayList<int[]>>  data) {
        StringBuilder output = new StringBuilder();
        output.append("{");
        int kIndex=0;
        for (String k : data.keySet()){
            output.append(k).append(" = {");
            int sIndex = 0;
            for (int[] s : data.get(k)) {
                output.append(Arrays.toString(s));
                sIndex++;
                if (sIndex != data.get(k).size()) output.append(", ");
            }
            kIndex++;
            output.append("}");
            if (kIndex != data.size()) output.append(", ");
        }
        output.append("}");
        return output.toString();
    }


    public static int factorial(int num) {
        return (num>1)?factorial(num-1)*num:num;
    }

    public int npr(int n,int r) {
        return factorial(n) / factorial(n-r);
    }

    public ArrayList<TreeMap<String, Boolean>> eval(ArrayList<String> groups, HashMap<String, ArrayList<int[]>> varList) throws Exception {
        ArrayList<TreeMap<String, Boolean>> output = new ArrayList<>();
        Set<String> stringSet = generatePermutations(varList.size());
        //System.out.println(stringSet);
        for (String permutation : stringSet) {
            TreeMap<String, Boolean> tempMap = new TreeMap<>();
            int index = 0;
            for (String var: varList.keySet()) {
                tempMap.put(var, permutation.charAt(index) != '0');
                index++;
            }

            //now calculate all the values of each object in group and add it to the equation:
            evalHelper(tempMap, groups);
            output.add(tempMap);
        }

        for (TreeMap<String, Boolean> f : output) System.out.println(f);
        return output;
    }


    private static void swap(String[] elements, int a, int b) {
        String tmp = elements[a];
        elements[a] = elements[b];
        elements[b] = tmp;
    }

    private Set<String> generatePermutations(int nOfVars) {
        Set<String> output = new HashSet<String>();
        int[] indexes = new int[nOfVars];

        String[] elements = new String[nOfVars];
        Arrays.fill(elements, "1");
        output.add(String.join("", elements));

        for (int y =0;y<nOfVars; y++) {
            Arrays.fill(elements, "0");
            for (int x =0;x<y; x++) elements[x] = "1";
            int i = 0;
            while (i < nOfVars) {
                if (indexes[i] < i) {
                    swap(elements, i % 2 == 0 ? 0 : indexes[i], i);
                    output.add(String.join("", elements));
                    indexes[i]++;
                    i = 0;
                } else {
                    indexes[i] = 0;
                    i++;
                }
            }
        }

        if (nOfVars==2) output.add("10"); //replace this later as it should not need it

        //System.out.println(output);
        return output;
    }

    //where most of the computation is actually done
    private void evalHelper(TreeMap<String,Boolean> varlist, ArrayList<String> groups) throws Exception {
        for (String group : groups) {
            String modifiedGroup = group;
            int cI; // {"⋀", "⋁", "¬", "∃", "∀","∈","∉","→","←","↔",">","<","⊻"};
            for (String var : varlist.keySet()) {
                while ((cI=modifiedGroup.indexOf(var)) >-1) {
                    modifiedGroup = modifiedGroup.substring(0,cI) + (varlist.get(var)?1:0)+modifiedGroup.substring(cI+var.length());
                }
            }

            //remove leading parenthesis
            if (modifiedGroup.startsWith("(")) modifiedGroup = modifiedGroup.substring(1);
            if (modifiedGroup.endsWith(")")) modifiedGroup = modifiedGroup.substring(0,modifiedGroup.length()-1);

            //if contains subgroup remove that first i.e. {0} gets replaced with groups[0] bool value;
            HashMap<String,ArrayList<int[]>> subgroupLocations = regexHelper("\\{[0-9]*?\\}", modifiedGroup);
            for (String match: subgroupLocations.keySet()) {
                //I'm not a huge fan of this, but I'm in too deep at this point
                //this should only fail if the parsing order get messed up somehow.
                Boolean replacement = varlist.get(groups.get(Integer.parseInt(match.substring(1,match.length()-1))));
                for (int[] loc : subgroupLocations.get(match)) {
                    //System.out.print("Replacing group with group.get(id); Original: "+modifiedGroup);
                    modifiedGroup = modifiedGroup.substring(0, loc[0])+(replacement?'1':'0')+modifiedGroup.substring(loc[1]);
                    //System.out.print(" Replaced: "+modifiedGroup);
                    for (String m: subgroupLocations.keySet()) {
                        for (int[] loc2 : subgroupLocations.get(m)) {
                            if (loc2[0] > loc[0]) {
                                int newDiffStringSize = (loc[1] - loc[0]) - 1;
                                loc2[0] -= newDiffStringSize;
                                loc2[1] -= newDiffStringSize;
                            }
                        }
                    }
                }
            }

            while ((cI=modifiedGroup.indexOf(' ')) > -1) modifiedGroup = modifiedGroup.substring(0,cI)+modifiedGroup.substring(cI+1);
            //System.out.println("\nOriginal group: "+group+" With replaced vars: "+modifiedGroup+" Data:"+varlist);
            //assuming beyond this point that there are no parenth/spaces

            //handle not
            while ((cI=modifiedGroup.indexOf('¬'))>-1) {
                modifiedGroup = modifiedGroup.substring(0,cI)+((modifiedGroup.charAt(cI+1)=='1')?'0':'1')+modifiedGroup.substring(cI+2);
                //System.out.println("Remove Not at index: " +cI+" group: "+modifiedGroup);
            }

            //handle and
            while ((cI=modifiedGroup.indexOf('⋀'))>-1) {
                modifiedGroup = modifiedGroup.substring(0,cI-1)+
                        ((modifiedGroup.charAt(cI-1)=='1' && modifiedGroup.charAt(cI+1)=='1')?'1':'0')+modifiedGroup.substring(cI+2);
                //System.out.println("Remove And at index: " +cI+" group: "+modifiedGroup);
            }

            //handle or
            while ((cI=modifiedGroup.indexOf('⋁'))>-1) {
                modifiedGroup = modifiedGroup.substring(0,cI-1)+
                        ((modifiedGroup.charAt(cI-1)=='1' || modifiedGroup.charAt(cI+1)=='1')?'1':'0')+modifiedGroup.substring(cI+2);
                //System.out.println("Remove Or at index: " +cI+" group: "+modifiedGroup);
            }

            //handle xor
            while ((cI=modifiedGroup.indexOf('⊕'))>-1) {
                modifiedGroup = modifiedGroup.substring(0,cI-1)+
                        (((modifiedGroup.charAt(cI-1)=='1' || modifiedGroup.charAt(cI+1)=='1')&&
                                (modifiedGroup.charAt(cI-1)!=modifiedGroup.charAt(cI+1)))?
                                '1':'0')+modifiedGroup.substring(cI+2);
                //System.out.println("Remove Or at index: " +cI+" group: "+modifiedGroup);
            }

            //handle conditional ->
            while ((cI=modifiedGroup.indexOf('→'))>-1) {
                modifiedGroup = modifiedGroup.substring(0,cI-1)+
                        ((modifiedGroup.charAt(cI-1)=='1' && modifiedGroup.charAt(cI+1)=='0')?'0':'1')+modifiedGroup.substring(cI+2);
                //System.out.println("Remove Or at index: " +cI+" group: "+modifiedGroup);
            }

            //handle bi conditional <->
            while ((cI=modifiedGroup.indexOf('↔'))>-1) {
                modifiedGroup = modifiedGroup.substring(0,cI-1)+
                        ((modifiedGroup.charAt(cI-1)==modifiedGroup.charAt(cI+1))?'1':'0')+modifiedGroup.substring(cI+2);
                //System.out.println("Remove Or at index: " +cI+" group: "+modifiedGroup);
            }

            //handle NAND ↑
            while ((cI=modifiedGroup.indexOf('↑'))>-1) {
                modifiedGroup = modifiedGroup.substring(0,cI-1)+
                        (((!(modifiedGroup.charAt(cI - 1) == '1') ||
                                !(modifiedGroup.charAt(cI + 1) == '1'))?'1':'0')+modifiedGroup.substring(cI+2));
            }


            //System.out.println("Solved Group: "+modifiedGroup);
            if (modifiedGroup.equals("1")) {
                varlist.put(group, true);
            } else if (modifiedGroup.equals("0")) {
                varlist.put(group, false);
            }
            else throw new Exception("SOLVED GROUP FAILED TO BE EITHER TRUE OR FALSE");
        }
    }


    public static void assertPermutations(String permutations) {
        //throw new Exception("Permutations were not calculated correctly.");
    }

    private HashMap<String, ArrayList<int[]>> regexHelper(String pattern, String d) {
        HashMap<String, ArrayList<int[]>> temp = new HashMap<>();
        Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(d);
        boolean matchFound = matcher.find();
        while (matchFound) {
            String match = matcher.group();
            if (!match.isEmpty()) {
                if (!temp.containsKey(match)) {
                    temp.put(match, (new ArrayList<>()));
                    temp.get(match).add(new int[]{matcher.start(), matcher.end()});
                }
                else temp.get(match).add(new int[]{matcher.start(), matcher.end()});
            }
            matchFound = matcher.find();
        }
        return temp;
    }

}
