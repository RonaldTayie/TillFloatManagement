import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static ArrayList<String> lines = new ArrayList<String>();
    private static ArrayList<String> result = new ArrayList<String>();
    private static String ChangeBreakdown = "";
    // Currency piece Value, Number of Pieces
    private static HashMap<Integer,Integer> Till = new HashMap<Integer,Integer>();
    private static int TillStart = 0;
    // Rewrite the result file
    private static void UpdateReslts(File file) {
        try {
            Scanner scan = new Scanner(file);
            while(scan.hasNextLine()) {
                String[] line = scan.nextLine().split(";");
                for(String l : line) {
                    result.add(l);
                }
            }
            // If results file does not exist the we create and write to the file
            File r = new File(getFileLocation()+"result.txt");
            if(!r.exists()) {
                r.createNewFile();
            }
            Writer w;
            w = new BufferedWriter( new FileWriter(r));
            for(String rs : result) {
                w.write(rs+"\n");
            }
            w.close();
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
    // Read Input file append to lines
    private static  void ReadFile(File file) {
        try {
            Scanner scan = new Scanner(file);
            while(scan.hasNextLine()) {
                lines.add(scan.nextLine());
            }
        }catch(Exception e) {
            System.out.println(e.toString());
        }
    }
    static String getFileLocation() throws IOException  {
        // File separator or delimiter
        String dl = System.getProperty("file.separator");
        // Current directory and add data folder location
        String pwd = new java.io.File(".").getCanonicalPath()+dl+"src"+dl+"data"+dl;
        return pwd;
    }

    private static String makeup_change(Integer c, ArrayList<Integer> pt ) {
        for(int p : pt) {
            // if a currency piece in the pieces tendered corresponds to any of the pieces in the HashMap we will increment that key by 1 with every occurrence
            if(Till.containsKey(p)) {
                int k = Till.get(p);
                Till.put(p, k+1);
            }else {
                // Considering that currency pieces change in value due to inflation, variations new pieces will be accommodated
                Till.put(p, 1);
            }
        }
        ArrayList<Integer> usable_pieces = new ArrayList<Integer>();
        for(int cp : Till.keySet()) {
            if(cp <= c) {
                usable_pieces.add(cp);
            }
        }
        usable_pieces.sort(Comparator.reverseOrder());
        /*
         * With the array of currency pieces smaller than the change arranged in descending order,
         * we will add up to the change going forward
         */
        ArrayList <Integer> used_pieces = new ArrayList<Integer>();
        int ch = 0;
        // total iteration sum
        int current_sum = 0;
        // Sum of i plus j item
        int itsum = 0;
        for(int i =0;i<usable_pieces.size();i++) {
            boolean parent_break = false;
            if(usable_pieces.get(i)==c) {
                used_pieces.add(usable_pieces.get(i));
                parent_break=true;
                break;
            }else {
                for(int j =0;j<usable_pieces.size();j++) {
                    itsum = usable_pieces.get(i)+ usable_pieces.get(j);
                    if(c==itsum){
                        used_pieces.add(usable_pieces.get(i));
                        used_pieces.add(usable_pieces.get(j));
                        parent_break = true;
                        break;
                    }
                    // To avoid ArrayOutOfBounce exception ill check add the previous item and the next item will be
                    // worked with on the next iteration
                    if(j>1&itsum+usable_pieces.get(j>0?j-1:usable_pieces.size()-1)==c){
                        used_pieces.add(usable_pieces.get(i));
                        used_pieces.add(usable_pieces.get(j));
                        used_pieces.add(usable_pieces.get(j-1));
                        parent_break = true;
                        break;
                    }else if(j==0& itsum+usable_pieces.get(usable_pieces.size()-1)==c ){
                        used_pieces.add(usable_pieces.get(0));
                        used_pieces.add(usable_pieces.get(usable_pieces.size()));
                        parent_break = true;
                        break;
                    }
                    if(current_sum==c) {
                        // Add all previous pieces to the used_pieces
                        for(int k = 0;k<j;k++) {
                            used_pieces.add(usable_pieces.get(k));
                        }
                        parent_break = true;
                        break;
                    }
                    current_sum += usable_pieces.get(j);
                }
            }
            if(parent_break) {
                break;
            }
        }
        if(!used_pieces.isEmpty()) {
            ArrayList<String> f = new ArrayList<String>();
            for(int n: used_pieces){
                f.add("R"+n);
            }
            StringBuilder str = new StringBuilder("");
            for(String s : f) {
                str.append(s).append("-");
            }
            return str.toString().substring(0,str.length()-1);
        }
        return "";
    }

    private static void Transact() {
        result.add("Till Start, Transaction Total, Paid, Change Total, Change Breakdown");
        for (String line : lines) {
            // Split line into two parts [ Item_&_Price, Tendered Cash ]
            String [] transaction = line.split(",");
            String[] items = transaction[0].split(";");
            //List of currency pieces tendered
            ArrayList<Integer> pieces_tendered = new ArrayList<Integer>();
            int total_tendered = 0;
            int change = 0;
            int item_value = 0;
            // Compute value of items
            for(String item : items) {
                String[] i = item.split(" ");
                int iv = Integer.parseInt(i[i.length-1].substring(1));
                item_value += iv;
            }
            // Compute currency pieces tendered
            for(String p:transaction[1].split("-")) {
                int pt= Integer.parseInt(p.substring(1));
                pieces_tendered.add(pt);
                total_tendered += pt;
            }
            change = (total_tendered-item_value);
            ChangeBreakdown = change==0? "R0" : makeup_change(change, pieces_tendered);
            result.add("R" + TillStart + ", R" + item_value + ", R" + total_tendered + ", R" +change  + ", " + ChangeBreakdown);
            TillStart += item_value-change;
        }
    }

    private static void InitTill() {
        // Currency piece Value, Number of Pieces
        String defaultFloats = "5 x R50,5 x R20, 6 x R10,12 x R5, 10 x R2, 10 x R1";
        String[] notes = defaultFloats.split(",");
        for(String n : notes) {
            String[] i = n.split("x");
            String N = i[0].trim();
            int num_of_peices = Integer.parseInt(N);
            int currency_peice = Integer.parseInt(i[1].split("R")[1].trim());
            TillStart += (num_of_peices*currency_peice);

            Till.put(currency_peice, num_of_peices);
        }
    }

    public static void main(String[] args) throws IOException {
        InitTill();
        String filename = getFileLocation()+"input.txt";
        File file = new File(filename);
        File resultFile = new File(getFileLocation()+"result.txt");
        //if the file exists proceed
        if(file.exists()) {
            ReadFile(file);
            if(lines.size() > 0) {
                Transact();
                UpdateReslts(resultFile);
            }else {
                System.err.println("No transaction Data Available..");
                System.exit(0);
            }
        }
    }

}
